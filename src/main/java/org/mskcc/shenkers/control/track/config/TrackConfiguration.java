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
import org.mskcc.shenkers.control.track.bigwig.BigWigContext;
import org.mskcc.shenkers.control.track.bigwig.BigWigView;
import org.mskcc.shenkers.control.track.gene.GeneModelContext;
import org.mskcc.shenkers.control.track.gene.GeneModelView;

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
        // BAM
        bind(new TypeLiteral<TrackFactory<BamContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<BamContext>>() {});
        // BIGWIG
        bind(new TypeLiteral<TrackFactory<BigWigContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<BigWigContext>>() {});
        
        // GTF
        bind(new TypeLiteral<TrackFactory<GeneModelContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<GeneModelContext>>() {});
        
        // For each data type, bind all available views to be injected into the factory
        // BAM
        TypeLiteral<View<BamContext>> bamViewType = new TypeLiteral<View<BamContext>>() {};
        Multibinder<View<BamContext>> bamViews = Multibinder.newSetBinder(binder(), bamViewType);
//        bamViews.addBinding().to(BamView1.class);
        bamViews.addBinding().to(BamView2.class);
        //BIGWIG
        TypeLiteral<View<BigWigContext>> bigWigViewType = new TypeLiteral<View<BigWigContext>>() {};
        Multibinder<View<BigWigContext>> bigWigViews = Multibinder.newSetBinder(binder(), bigWigViewType);
        bigWigViews.addBinding().to(BigWigView.class);
        
        //GTF
        TypeLiteral<View<GeneModelContext>> gtfViewType = new TypeLiteral<View<GeneModelContext>>() {};
        Multibinder<View<GeneModelContext>> gtfViews = Multibinder.newSetBinder(binder(), gtfViewType);
        gtfViews.addBinding().to(GeneModelView.class);
    }

}
