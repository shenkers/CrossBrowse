/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
@Path("setCoordinate")
public class TestResource {

    Logger log = LogManager.getLogger();

    @GET
    public Response setCoordinate(
            @QueryParam("chr") String chr,
            @QueryParam("start") int start
    ) {
        log.info("request delegated");
        log.info("chr='{}'", chr);
        log.info("start='{}'", start);
        
        return Response.ok().build();
    }
}
