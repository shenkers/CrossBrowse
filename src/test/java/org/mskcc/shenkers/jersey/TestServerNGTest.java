/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.concurrent.Worker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
//import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
//import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.mskcc.shenkers.jersey.TestResource.abc;
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
//        try {
//            System.in.read();
//        } catch (IOException ex) {
//            Logger.getLogger(TestServerNGTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
        final Map<String, String> namespacePrefixMapper = new HashMap<String, String>();
        namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

//        final MoxyJsonConfig moxyJsonConfig = new MoxyJsonConfig()
//                .setNamespacePrefixMapper(namespacePrefixMapper)
//                .setNamespaceSeparator(':');
//
//        final ContextResolver<MoxyJsonConfig> jsonConfigResolver = moxyJsonConfig.resolver();
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target("http://localhost:12356/setCoordinate");
        abc post = null;
        try {
            post = target.request(MediaType.APPLICATION_JSON)
//                    .get(abc.class);
            .post(Entity.entity(new abc("def",0), MediaType.APPLICATION_JSON), abc.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
//                
        System.out.println("result " + post);
        try {
            System.in.read();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testReduce(){
        Worker.State s = Worker.State.SUCCEEDED;
        
        Boolean reduce = Stream.concat(Stream.of(),Stream.of(s)).map(state ->
                state != Worker.State.SUCCEEDED
                        && state != Worker.State.CANCELLED
                        && state != Worker.State.FAILED)
                .reduce(false, (b1, b2) -> b1 || b2);
        System.out.println("REDUCE "+reduce);
    }
}
