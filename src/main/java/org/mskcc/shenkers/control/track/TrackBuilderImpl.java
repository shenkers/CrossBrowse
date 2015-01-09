/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import org.mskcc.shenkers.control.track.bam.BamContext;
import com.google.inject.Inject;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
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
                SamReader reader = SamReaderFactory.makeDefault().open(new File(resource));
                BamContext context = new BamContext(reader);
                return btf.create(context);
            }
            default: {
                throw new RuntimeException("No handler configured for "+type);
            }
        }
    }
}
