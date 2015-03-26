/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.jersey;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
@Path("removeTrack")
public class RemoveTrackResource {

    private static final Logger logger = LogManager.getLogger ();
   
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    public Response removeTrack(){
        
        return Response.ok().build();
    }
}
