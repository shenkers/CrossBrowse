/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.rest;

import org.mskcc.shenkers.control.track.gene.*;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public interface RestIntervalProvider {
    
    public GenomeSpan query();
}
