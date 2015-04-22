/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
import org.mskcc.shenkers.control.track.bigwig.BigWigZoomView;
import org.mskcc.shenkers.control.track.fasta.FastaContext;
import org.mskcc.shenkers.control.track.fasta.FastaViewBuilder;
import org.mskcc.shenkers.control.track.gene.GeneModelContext;
import org.mskcc.shenkers.control.track.gene.GeneModelView;
import org.mskcc.shenkers.control.track.interval.IntervalContext;
import org.mskcc.shenkers.control.track.interval.IntervalView;

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
        
        // interval
        bind(new TypeLiteral<TrackFactory<IntervalContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<IntervalContext>>() {});
        
        // fasta
        bind(new TypeLiteral<TrackFactory<FastaContext>>() {})
                .to(new TypeLiteral<TrackFactoryImpl<FastaContext>>() {});
        
        // For each data type, bind all available views to be injected into the factory
        // BAM
        TypeLiteral<View<BamContext>> bamViewType = new TypeLiteral<View<BamContext>>() {};
        Multibinder<View<BamContext>> bamViews = Multibinder.newSetBinder(binder(), bamViewType);
//        bamViews.addBinding().to(BamView1.class);
        bamViews.addBinding().to(BamView2.class);
        //BIGWIG
        TypeLiteral<View<BigWigContext>> bigWigViewType = new TypeLiteral<View<BigWigContext>>() {};
        Multibinder<View<BigWigContext>> bigWigViews = Multibinder.newSetBinder(binder(), bigWigViewType);
        bigWigViews.addBinding().to(BigWigZoomView.class);
        bigWigViews.addBinding().to(BigWigView.class);
        
        //GTF
        TypeLiteral<View<GeneModelContext>> gtfViewType = new TypeLiteral<View<GeneModelContext>>() {};
        Multibinder<View<GeneModelContext>> gtfViews = Multibinder.newSetBinder(binder(), gtfViewType);
        gtfViews.addBinding().to(GeneModelView.class);
        
        //interval
        TypeLiteral<View<IntervalContext>> intervalViewType = new TypeLiteral<View<IntervalContext>>() {};
        Multibinder<View<IntervalContext>> intervalViews = Multibinder.newSetBinder(binder(), intervalViewType);
        intervalViews.addBinding().to(IntervalView.class);
        
        //fasta
        TypeLiteral<View<FastaContext>> fastaViewType = new TypeLiteral<View<FastaContext>>() {};
        Multibinder<View<FastaContext>> fastaViews = Multibinder.newSetBinder(binder(), fastaViewType);
        fastaViews.addBinding().to(FastaViewBuilder.class);
        
        bind(Executor.class).toInstance(Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).build()));
    }

}
