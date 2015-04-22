/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.control.track.rest.RestIntervalContext;
import org.mskcc.shenkers.control.track.rest.RestIntervalProvider;
import org.mskcc.shenkers.control.track.rest.RestIntervalProviderImpl;
import org.mskcc.shenkers.control.track.rest.RestIntervalView;
import org.mskcc.shenkers.controller.FXMLController;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
@Singleton
@Path("namedIntervalTrack")
public class NamedIntervalResource {

    Logger log = LogManager.getLogger();

    ModelSingleton model = ModelSingleton.getInstance();
    
    Executor executor = Executors.newSingleThreadExecutor();

    Map<String, RestIntervalProviderImpl> namedTrackIntervals = new HashMap<>();
    Map<String, Track> namedTracks = new HashMap<>();

    @GET
    public Response addInterval(
            @QueryParam("trackName") String trackName,
            @QueryParam("genomeId") String genomeId,
            @QueryParam("chr") String chr,
            @QueryParam("start") int start,
            @QueryParam("end") int end,
            @QueryParam("toNegativeStrand") boolean toNegativeStrand
    ) {

        if (namedTrackIntervals.get(trackName) == null) {
            log.info("track '{}' doesn't exist, creating a new one", trackName);
            RestIntervalProviderImpl rip = namedTrackIntervals.computeIfAbsent(trackName, t -> new RestIntervalProviderImpl());
            Track trck = new Track<>(new RestIntervalContext(rip), Arrays.asList(new RestIntervalView()), executor);
            namedTracks.put(trackName, trck);

            Genome g;
            if (genomeId != null) {
                int indexOf = model.getGenomes().indexOf(new Genome(genomeId, null));
                log.info("genomes {}", model.getGenomes());
                log.info("index {}", indexOf);
                g = model.getGenomes().get(indexOf);
            } else {
                g = model.getGenomes().get(0);
            }
            Platform.runLater(() -> {
                model.addTrack(g, trck);
                trck.update();
            });
        } else {
            log.info("track '{}' exists", trackName);
        }

        RestIntervalProviderImpl rip = namedTrackIntervals.get(trackName);
        rip.addInterval(chr, start, end);
        Platform.runLater(() -> {
            namedTracks.get(trackName).update();
        });

        return Response.ok().build();
    }

    @DELETE
    public Response removeInterval(
            @QueryParam("trackName") String trackName,
            @QueryParam("genomeId") String genomeId,
            @QueryParam("chr") String chr,
            @QueryParam("start") int start,
            @QueryParam("end") int end,
            @QueryParam("toNegativeStrand") boolean toNegativeStrand
    ) {

        if (namedTrackIntervals.get(trackName) == null) {
            log.info("track '{}' doesn't exist, nothing to remove", trackName);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            log.info("track '{}' exists", trackName);
        }

        RestIntervalProviderImpl rip = namedTrackIntervals.get(trackName);
        rip.removeInterval(chr, start, end);
        Platform.runLater(() -> {
            namedTracks.get(trackName).update();
        });

        return Response.ok().build();
    }

//    @GET
////    @Produces (MediaType.APPLICATION_JSON)
//    public Response setCoordinate(
//            @QueryParam("genomeId") String genomeId,
//            @QueryParam("chr") String chr,
//            @QueryParam("start") int start,
//            @QueryParam("end") int end,
//            @QueryParam("toNegativeStrand") boolean toNegativeStrand
//    ) {
//        log.info("id={} {} {} {} neg={}",genomeId,chr,start,end,toNegativeStrand);
//        log.info("genomes {}",model.getGenomes());
//        if(genomeId!=null){
//        int indexOf = model.getGenomes().indexOf(new Genome(genomeId, null));
//        log.info("index {}",indexOf);
//        Genome genome = model.getGenomes().get(indexOf);
//        log.info("genome {}",genome);
//        Platform.runLater(()->{
//        model.setSpan(genome, Optional.of(new GenomeSpan(chr, start, end, toNegativeStrand)));
//        });
//        }
//        else{
//        Genome genome = model.getGenomes().get(0);
//        log.info("genome {}",genome);
//        Platform.runLater(()->{
//        model.setSpan(genome, Optional.of(new GenomeSpan(chr, start, end, toNegativeStrand)));
//        });
//        }
//        return Response.ok().build();
//    }
}
