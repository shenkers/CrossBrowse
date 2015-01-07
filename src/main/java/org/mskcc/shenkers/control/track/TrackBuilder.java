/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.io.File;
import java.net.URI;

/**
 *
 * @author sol
 */
public interface TrackBuilder {

    public Track load(FileType type, String resource);
}
