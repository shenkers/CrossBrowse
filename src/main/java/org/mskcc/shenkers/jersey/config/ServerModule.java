/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.mskcc.shenkers.jersey.AnnotateIntervalResource;
import org.mskcc.shenkers.jersey.CoordinatesResource;
import org.mskcc.shenkers.jersey.NamedIntervalResource;
import org.mskcc.shenkers.jersey.SnapshotResource;
import org.mskcc.shenkers.jersey.TestResource;

/**
 *
 * @author sol
 */
public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
       
        bind(new TypeLiteral<Property<HttpServer>>() {})
                .toInstance(new SimpleObjectProperty<>());
        
        final ResourceConfig resourceConfig = new ResourceConfig(
                new Class[]{
                    TestResource.class,
                    CoordinatesResource.class,
                    SnapshotResource.class,
                    AnnotateIntervalResource.class,    
                    NamedIntervalResource.class,
                }
        );
        
        bind(ResourceConfig.class).toInstance(resourceConfig);
       
    }

}
