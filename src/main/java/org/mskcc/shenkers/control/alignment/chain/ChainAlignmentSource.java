/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.SAMException;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import java.io.File;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ChainAlignmentSource implements AlignmentSource {

    ChainParser instance;

    public ChainAlignmentSource(String uri) {
        instance = new ChainParser(new File(uri));
    }

    @Override
    public List<LocalAlignment> getAlignment(GenomeSpan span1, GenomeSpan span2) {
        Interval interval = new Interval(span1.getChr(), span1.getStart(), span1.getEnd());

        List<LocalAlignment> chainIntersections = instance.getChainIntersections(interval);
        for (LocalAlignment blocks : chainIntersections) {
            StringBuilder gapped1 = new StringBuilder();
            StringBuilder gapped2 = new StringBuilder();

            // TODO need to make sure there are not zero blocks
            gapped1.append(StringUtils.repeat("X", blocks.getBlockSize(0)));
            gapped2.append(StringUtils.repeat("X", blocks.getBlockSize(0)));
            int last1 = blocks.getFromBlock(0).getValue();
            int last2 = blocks.getToNegativeStrand() ? blocks.getToBlock(0).getKey() : blocks.getToBlock(0).getValue();
            for (int i = 1; i < blocks.getNBlocks(); i++) {
                System.out.println(String.format("%s:%d-%d", blocks.getFromSequenceName(), blocks.getFromBlock(i).getKey(), blocks.getFromBlock(i).getValue()));
                System.out.println(String.format("%s:%d-%d", blocks.getToSequenceName(), blocks.getToBlock(i).getKey(), blocks.getToBlock(i).getValue()));
                System.err.println("");

                gapped1.append(StringUtils.repeat("X", blocks.getFromBlock(i).getKey() - last1 - 1));
                gapped2.append(StringUtils.repeat("-", blocks.getFromBlock(i).getKey() - last1 - 1));
                int gap2 = blocks.getToNegativeStrand() ? last2 - blocks.getToBlock(i).getValue() - 1 : blocks.getToBlock(i).getKey() - last2 - 1;
                gapped2.append(StringUtils.repeat("X", gap2));
                gapped1.append(StringUtils.repeat("-", gap2));

                gapped1.append(StringUtils.repeat("X", blocks.getBlockSize(i)));
                gapped2.append(StringUtils.repeat("X", blocks.getBlockSize(i)));

                last1 = blocks.getFromBlock(i).getValue();
                last2 = blocks.getToNegativeStrand() ? blocks.getToBlock(i).getKey() : blocks.getToBlock(i).getValue();

            }
            System.out.println(gapped1.toString());
            System.out.println(gapped2.toString());
            System.out.println(gapped1.toString().replaceAll("-", "").length());
            System.out.println(gapped2.toString().replaceAll("-", "").length());
            System.out.println(74869 - 74548 + 1);
            System.out.println(42438 - 42085 + 1);
        }
        return chainIntersections;
    }

}
