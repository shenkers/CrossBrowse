/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.rest;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import htsjdk.tribble.annotation.Strand;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.mskcc.shenkers.data.interval.GIntervalTree;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;


public class RestIntervalProviderImpl implements RestIntervalProvider {

    GIntervalTree intervals = new GIntervalTree();
    
    @Override
    public List<Pair<Integer,Integer>> query(String chr, int start, int end) {
        List<Pair<Integer, Integer>> ranges = 
                intervals.streamOverlaps(chr, start, end)
                        .map(i -> new Pair<>(i.getStart(), i.getEnd()))
                        .collect(Collectors.toList());
        return ranges;
    }
    
    public void addInterval(String chr, int start, int end){
        intervals.add(chr, start, end, Strand.NONE);
    }
    
    public void removeInterval(String chr, int start, int end){
        intervals.remove(chr, start, end, Strand.NONE);
    }
    
}
