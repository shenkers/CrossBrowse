/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

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

/**
 *
 * @author sol
 */
@Path("setCoordinate")
public class TestResource {

    Logger log = LogManager.getLogger();

    ModelSingleton model = ModelSingleton.getInstance();

//    @GET
//    @Produces (MediaType.APPLICATION_JSON)
    public Response setCoordinate(
            @QueryParam("chr") String chr,
            @QueryParam("start") int start
    ) {
        log.info("request delegated");
        log.info("chr='{}'", chr);
        log.info("start='{}'", start);
        log.info(model.getGenomes());

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public abc setCoordinate() {
        log.info("request delegated");
        log.info(model.getGenomes());

        abc abc = new abc("xyz",-1);
        return abc;
    }

    static class abc {

        private String s;
        private int i;

        private abc() {
        }

        public abc(String xyz, int i) {
            this.s=xyz;
            this.i=i;
        }

        /**
         * @return the s
         */
        public String getS() {
            return s;
        }

        /**
         * @param s the s to set
         */
        public void setS(String s) {
            this.s = s;
        }

        /**
         * @return the i
         */
        public int getI() {
            return i;
        }

        /**
         * @param i the i to set
         */
        public void setI(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
        return String.format("s=%s i=%d", s,i);
        }
        
        

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public abc doPost(abc i) {
        log.info("posting");
        abc abc = new abc(i.s + "!", i.i+1);
        return abc;
    }
}
