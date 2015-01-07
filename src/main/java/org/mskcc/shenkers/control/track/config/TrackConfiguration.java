/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.mskcc.shenkers.control.track.bam.BamView1;
import org.mskcc.shenkers.control.track.bam.BamView2;
import org.mskcc.shenkers.control.track.TrackBuilder;
import org.mskcc.shenkers.control.track.TrackBuilderImpl;
import org.mskcc.shenkers.control.track.TrackFactory;
import org.mskcc.shenkers.control.track.TrackFactoryImpl;
import org.mskcc.shenkers.control.track.View;

/**
 *
 * @author sol
 */
public class TrackConfiguration extends AbstractModule {

    @Override
    protected void configure() {
        // Configure the builder implementation
        bind(TrackBuilder.class).to(TrackBuilderImpl.class);
        
        // For each data type, bind a single factory
        bind(new TypeLiteral<TrackFactory<BamContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<BamContext>>() {});
        
        // For each data type, bind all available views to be injected into the factory
        TypeLiteral<View<BamContext>> bamViewType = new TypeLiteral<View<BamContext>>() {};
        Multibinder<View<BamContext>> bamViews = Multibinder.newSetBinder(binder(), bamViewType);
        bamViews.addBinding().to(BamView1.class);
        bamViews.addBinding().to(BamView2.class);
    }

}
