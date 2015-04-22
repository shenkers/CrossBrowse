/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.interval;

import org.mskcc.shenkers.control.track.gene.*;
import org.mskcc.shenkers.data.interval.IntervalFeature;

/**
 *
 * @author sol
 */
public interface IntervalProvider {
    
    public Iterable<IntervalFeature> query(String chr, int start, int end);
}
