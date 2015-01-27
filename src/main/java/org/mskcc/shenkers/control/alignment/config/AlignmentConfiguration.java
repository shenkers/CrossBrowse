/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.mskcc.shenkers.control.alignment.io.AlignmentFileLoader;
import org.mskcc.shenkers.control.alignment.io.AlignmentLoader;
import org.mskcc.shenkers.control.track.TrackBuilder;
import org.mskcc.shenkers.control.track.TrackBuilderImpl;
import org.mskcc.shenkers.control.track.TrackFactory;
import org.mskcc.shenkers.control.track.TrackFactoryImpl;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.mskcc.shenkers.control.track.bam.BamView1;
import org.mskcc.shenkers.control.track.bam.BamView2;

/**
 *
 * @author Soma
 */
public class AlignmentConfiguration extends AbstractModule {

    @Override
    protected void configure() {
        // Configure the builder implementation
        bind(AlignmentLoader.class).to(AlignmentFileLoader.class);
    }

}
