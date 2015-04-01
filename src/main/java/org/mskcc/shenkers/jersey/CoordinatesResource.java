/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import java.util.Optional;
import javafx.application.Platform;
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
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
@Path("setCoordinates")
public class CoordinatesResource {

    Logger log = LogManager.getLogger();

    ModelSingleton model = ModelSingleton.getInstance();

    @GET
//    @Produces (MediaType.APPLICATION_JSON)
    public Response setCoordinate(
            @QueryParam("genomeId") String genomeId,
            @QueryParam("chr") String chr,
            @QueryParam("start") int start,
            @QueryParam("end") int end,
            @QueryParam("toNegativeStrand") boolean toNegativeStrand
    ) {
        log.info("id={} {} {} {} neg={}",genomeId,chr,start,end,toNegativeStrand);
        log.info("genomes {}",model.getGenomes());
        if(genomeId!=null){
        int indexOf = model.getGenomes().indexOf(new Genome(genomeId, null));
        log.info("index {}",indexOf);
        Genome genome = model.getGenomes().get(indexOf);
        log.info("genome {}",genome);
        Platform.runLater(()->{
        model.setSpan(genome, Optional.of(new GenomeSpan(chr, start, end, toNegativeStrand)));
        });
        }
        else{
        Genome genome = model.getGenomes().get(0);
        log.info("genome {}",genome);
        Platform.runLater(()->{
        model.setSpan(genome, Optional.of(new GenomeSpan(chr, start, end, toNegativeStrand)));
        });
        }
        return Response.ok().build();
    }
//    @GET
////    @Produces (MediaType.APPLICATION_JSON)
//    public Response setCoordinate(
//            @QueryParam("genomeId") String genomeId,
//            @QueryParam("neg") boolean toNegativeStrand
//    ) {
//        log.info("id={} neg={}",genomeId,toNegativeStrand);
//        log.info("recieved get");
//        return Response.ok().build();
//    }

}
