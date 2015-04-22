/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.data.interval;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class RangeTools {

    private static final Logger logger = LogManager.getLogger ();
    
    public static RangeSet<Integer> asClosed(RangeSet<Integer> s) {
        RangeSet<Integer> closedIntervalSet = TreeRangeSet.create();
        for (Range<Integer> r : s.asRanges()) {
            closedIntervalSet.add(asClosed(r));
        }
        return closedIntervalSet;
    }
    
    public static <T> RangeMap<Integer,T> asClosed(RangeMap<Integer,T> s) {
        RangeMap<Integer,T> closedIntervalSet = TreeRangeMap.create();
        Map<Range<Integer>, T> map = s.asMapOfRanges();
        for (Map.Entry<Range<Integer>,T> r : map.entrySet()) {
            closedIntervalSet.put(asClosed(r.getKey()), r.getValue());
        }
        return closedIntervalSet;
    }
    
    public static Range<Integer> asClosed(Range<Integer> r) {
        return Range.closed(
                    r.lowerBoundType() == BoundType.OPEN 
                            ? r.lowerEndpoint() + 1 
                            : r.lowerEndpoint(),
                    r.upperBoundType() == BoundType.OPEN 
                            ? r.upperEndpoint() - 1 
                            : r.upperEndpoint()
            );
    }

    public static int length(Range<Integer> i) {
        return i.upperEndpoint()-i.lowerEndpoint()+1;
    }
    
    public static double lengthReal(Range<Double> i) {
        return i.upperEndpoint()-i.lowerEndpoint();
    }  
}
