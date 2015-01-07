/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import org.mskcc.shenkers.control.track.bam.BamContext;
import com.google.inject.Inject;
import java.io.File;
import java.net.URI;

/**
 *
 * @author sol
 */
public class TrackBuilderImpl implements TrackBuilder {

    TrackFactory<BamContext> btf;

    @Inject
    public TrackBuilderImpl(TrackFactory<BamContext> btf) {
        this.btf = btf;
    }    

    public Track load(FileType type, String resource) {
        switch(type) {
            case BAM: {
                System.out.println("loading "+type+" "+resource);
                BamContext context = new BamContext();
                return btf.create(context);
            }
            default: {
                throw new RuntimeException("No handler configured for "+type);
            }
        }
    }
}
