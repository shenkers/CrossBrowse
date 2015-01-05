/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import com.google.inject.Inject;
import java.io.File;
import org.mskcc.shenkers.imodel.Track;

/**
 *
 * @author sol
 */
public class TrackBuilderImpl implements TrackBuilder {

    BamTrackFactory btf;

    @Inject
    public TrackBuilderImpl(BamTrackFactory btf) {
        this.btf = btf;
    }    

    public Track load(FileType type, File file) {
        switch(type) {
            case BAM: {
                return btf.create(file.getName(), file);
            }
            default: {
                throw new RuntimeException("No handler configured for "+type);
            }
        }
    }
}
