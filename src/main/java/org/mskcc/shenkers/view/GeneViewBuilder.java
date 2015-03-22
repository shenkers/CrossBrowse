/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.DomainFlippable;

/**
 *
 * @author sol
 */
public class GeneViewBuilder<T extends Pane & DomainFlippable> {

    private static final Logger log = LogManager.getLogger();

    Range<Integer> gene;
    RangeSet<Integer> codingSubset;
    RangeSet<Integer> introns;
    RangeSet<Integer> untranslated;

    public GeneViewBuilder(Range<Integer> transcript, Optional<RangeSet<Integer>> exons, Optional<Range<Integer>> cds) {

        gene = transcript;
        codingSubset = cds.map(c -> exons.map(e -> asClosed(e.subRangeSet(c))).orElse(ImmutableRangeSet.of())).orElse(ImmutableRangeSet.of());
        introns = TreeRangeSet.create();
        introns.add(transcript);
        exons.ifPresent(e -> introns.removeAll(e));
        RangeSet<Integer> utr = exons.map(e ->(RangeSet<Integer>) TreeRangeSet.create(e)).orElse(TreeRangeSet.create());
        utr.removeAll(codingSubset);
        untranslated = utr;
        
        log.info("UTRs {}", untranslated);
        log.info("introns {}", introns);
        log.info("CDSs {}", codingSubset);
       
    }
    
    public T getView(int start, int end){
        GenericIntervalView<T> giv = new GenericIntervalView<>(gene.lowerEndpoint(), gene.upperEndpoint());
        List<Pair<Integer,Integer>> ranges = new ArrayList<>();
        List<T> panes = new ArrayList<>();
        
        for (Range<Integer> r : codingSubset.subRangeSet(Range.closed(start, end)).asRanges()) {
            ranges.add(new Pair<>(r.lowerEndpoint(),r.upperEndpoint()));
            
            panes.add((T) new GenePartIntervalNode<T>(1., (T) new RectangleIntervalNode()));
        }
        for (Range<Integer> r : untranslated.subRangeSet(Range.closed(start, end)).asRanges()) {
            ranges.add(new Pair<>(r.lowerEndpoint(),r.upperEndpoint()));
            panes.add( (T) new GenePartIntervalNode<T>(.5, (T) new RectangleIntervalNode()));
        }
        for (Range<Integer> r : introns.subRangeSet(Range.closed(start, end)).asRanges()) {
            ranges.add(new Pair<>(r.lowerEndpoint(),r.upperEndpoint()));
            panes.add( (T) new GenePartIntervalNode<T>(.1, (T) new RectangleIntervalNode()));
        }
        giv.setData(ranges, panes);
        return (T) giv;
    }

    public RangeSet<Integer> asClosed(RangeSet<Integer> s) {
        RangeSet<Integer> exons = TreeRangeSet.create();
        for (Range<Integer> r : s.asRanges()) {
            exons.add(Range.closed(
                    r.lowerBoundType() == BoundType.OPEN ? r.lowerEndpoint() + 1 : r.lowerEndpoint(),
                    r.upperBoundType() == BoundType.OPEN ? r.upperEndpoint() - 1 : r.upperEndpoint()
            ));
        }
        return exons;
    }

}
