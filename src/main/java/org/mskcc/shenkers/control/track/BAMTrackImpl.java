/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.mskcc.shenkers.imodel.Track;
import org.mskcc.shenkers.view.BamView;

/**
 *
 * @author sol
 */
public class BAMTrackImpl extends BAMTrack {

    String name;
    File bamFile;
    BamView currentView;
    List<BamView> views;
            
    @Inject
    public BAMTrackImpl(@Assisted String name, @Assisted File bamFile, Set<BamView> views) {
        this.name=name;
        this.bamFile=bamFile;
        this.views=new ArrayList<>(views);
    }

    @Override
    public String getName() {
        return name;
    }
    
    public void setCurrentView(String viewName){
        views.stream().filter((BamView v) -> v.getName().equals(viewName));
    }
    
}
