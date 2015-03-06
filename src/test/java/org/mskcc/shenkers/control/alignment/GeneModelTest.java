/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author Soma
 */
public class GeneModelTest {

    Logger log = LogManager.getLogger();
      @Test
  public void constructGeneIntervals() {

        Range<Integer> g = Range.closed(1, 10);
        RangeSet<Integer> exons = TreeRangeSet.create();
        exons.add(Range.open(1, 3));
        exons.add(Range.open(5, 6));
        exons.add(Range.open(8, 10));
        Range<Integer> cds = Range.open(3, 9);
        
        RangeSet<Integer> codingSubset = exons.subRangeSet(cds);
        RangeSet<Integer> introns = exons.complement().subRangeSet(g);
        RangeSet<Integer> untranslated = TreeRangeSet.create(exons);
        untranslated.removeAll(codingSubset);
        log.info("UTRs {}",untranslated);
        log.info("introns {}",introns);
        log.info("CDSs {}",codingSubset);
        for(Range<Integer> r : introns.asRanges()){
            log.info("intron {}-{}",r.lowerEndpoint(), r.upperEndpoint());
        }
    }
}
