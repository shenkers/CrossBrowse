/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

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
public abstract class BAMTrack extends Track{  
    
    public abstract void setCurrentView(String viewName);
    
}
