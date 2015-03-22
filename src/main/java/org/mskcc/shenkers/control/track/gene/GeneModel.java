/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.control.track.gene;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class GeneModel {

    private static final Logger logger = LogManager.getLogger ();
    
    Range<Integer> span;
    private Optional<RangeSet<Integer>> exons;
    private Optional<Range<Integer>> cds;

    public GeneModel(Range<Integer> span, Optional<RangeSet<Integer>> exons, Optional<Range<Integer>> cds) {
        this.span = span;
        this.exons = exons;
        this.cds = cds;
    }
    
    public Range<Integer> getSpan(){
        return span;
    }

    /**
     * @return the exons
     */
    public Optional<RangeSet<Integer>> getExons() {
        return exons;
    }

    /**
     * @return the cds
     */
    public Optional<Range<Integer>> getCds() {
        return cds;
    }
   
}
