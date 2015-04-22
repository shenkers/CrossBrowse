/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.data.interval;

import htsjdk.samtools.tabix.ChainContext;
import htsjdk.tribble.Feature;
import java.util.Comparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class CoordinateOrderComparator implements Comparator<Feature> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public int compare(Feature o1, Feature o2) {
        int compareChr = o1.getChr().compareTo(o2.getChr());
        if (compareChr == 0) {
            return o1.getStart() - o2.getStart();
        } else {
            return compareChr;
        }
    }

}
