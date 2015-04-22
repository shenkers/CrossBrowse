/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.data.interval;

import htsjdk.tribble.Feature;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.FullBEDFeature;
import java.awt.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public interface IntervalFeature<T> extends Feature {
    
    Strand getStrand();
    
    T getValue();
}
