/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.io.File;
import org.mskcc.shenkers.imodel.Track;

/**
 *
 * @author sol
 */
public interface TrackBuilder {

    public Track load(FileType type, File file);
}
