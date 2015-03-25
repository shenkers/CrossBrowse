/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.model;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.TrackFactory;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

/**
 *
 * @author sol
 */

public class CoordinateChangeModule extends AbstractModule{

    private static final Logger logger = LogManager.getLogger ();

    @Override
    protected void configure() {
        logger.info("configuring coordinate change module");
//        bind(EventSource.class).annotatedWith(CoordinateChange.class).toInstance(new EventSource());
        bind(new TypeLiteral<EventSource<CoordinateChangeEvent>>() {}).annotatedWith(CoordinateChange.class).toInstance(new EventSource<>());
    }
   
}
