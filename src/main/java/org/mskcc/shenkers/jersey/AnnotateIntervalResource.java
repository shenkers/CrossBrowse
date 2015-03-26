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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.mskcc.shenkers.control.track.rest.RestIntervalView;
import org.mskcc.shenkers.controller.FXMLController;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
@Path("addInterval")
public class AnnotateIntervalResource {

    Logger log = LogManager.getLogger();

    ModelSingleton model = ModelSingleton.getInstance();

    @GET
    public Response addInterval(
            @QueryParam("genomeId") String genomeId,
            @QueryParam("chr") String chr,
            @QueryParam("start") int start,
            @QueryParam("end") int end,
            @QueryParam("toNegativeStrand") boolean toNegativeStrand
    ) {
        Track trck = new Track<>(new RestIntervalContext(new RestIntervalProvider() {
            @Override
            public GenomeSpan query() {
                return new GenomeSpan(chr,start,end,toNegativeStrand);
            }
        }), Arrays.asList(new RestIntervalView()));
        
        int indexOf = model.getGenomes().indexOf(new Genome(genomeId, null));
        log.info("genomes {}",model.getGenomes());
        log.info("index {}",indexOf);
        Genome g = model.getGenomes().get(indexOf);
        Platform.runLater(()->{model.addTrack(g, trck);});
        
        return Response.ok().build();
    }

}
