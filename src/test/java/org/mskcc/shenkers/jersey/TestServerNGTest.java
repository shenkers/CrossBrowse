/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class TestServerNGTest {

    static HttpServer server;

    public TestServerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        ResourceConfig config = new ResourceConfig();
        final ResourceConfig resourceConfig = new ResourceConfig(TestResource.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:12356/"), resourceConfig);

//        server = HttpServer.createSimpleServer("abc", 12356);
        server.start();
        server.getServerConfiguration().addHttpHandler(new HttpHandler() {

            @Override
            public void service(Request request, Response response) throws Exception {
                response.setContentType("text/plain");
                String text = "hi!";
                response.setContentLength(text.length());
                response.getWriter().write(text);
//                request.get
            }
        }, "/testit");
//        server = GrizzlyHttpServerFactory.createHttpServer(new URI("localhost"),true);        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.shutdown();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        try {
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(TestServerNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
