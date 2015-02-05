/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import htsjdk.samtools.util.Interval;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.util.IntervalTools;

/**
 *
 * @author sol
 */
public class NucleotideMapping {

    static Logger logger = LogManager.getLogger();

    GenomeSpan fromInterval;
    GenomeSpan toInterval;

    List<Optional<Integer>> fromRelativeOffset;
    List<Optional<Integer>> fromAbsoluteOffset;
    List<Optional<Integer>> toRelativeOffset;
    List<Optional<Integer>> toAbsoluteOffset;

    public NucleotideMapping(GenomeSpan fromInterval, GenomeSpan toInterval) {
        this.fromInterval = fromInterval;
        this.toInterval = toInterval;
//            List<Optional<Integer>> fromRelativeOffset =
        fromRelativeOffset = Stream.iterate(0, i -> i + 1)
                .limit(fromInterval.length())
                .map(j -> {
                    Optional<Integer> o = Optional.empty();
                    return o;
                })
                .collect(Collectors.toList());
        fromAbsoluteOffset = Stream.iterate(0, i -> i + 1)
                .limit(fromInterval.length())
                .map(j -> {
                    Optional<Integer> o = Optional.empty();
                    return o;
                })
                .collect(Collectors.toList());
        toRelativeOffset = Stream.iterate(0, i -> i + 1)
                .limit(toInterval.length())
                .map(j -> {
                    Optional<Integer> o = Optional.empty();
                    return o;
                })
                .collect(Collectors.toList());
        toAbsoluteOffset = Stream.iterate(0, i -> i + 1)
                .limit(toInterval.length())
                .map(j -> {
                    Optional<Integer> o = Optional.empty();
                    return o;
                })
                .collect(Collectors.toList());

    }

    private NucleotideMapping(GenomeSpan fromInterval, GenomeSpan toInterval, List<Optional<Integer>> fromRelativeOffset, List<Optional<Integer>> fromAbsoluteOffset, List<Optional<Integer>> toRelativeOffset, List<Optional<Integer>> toAbsoluteOffset) {
        this.fromInterval = fromInterval;
        this.toInterval = toInterval;
        this.fromRelativeOffset = fromRelativeOffset;
        this.fromAbsoluteOffset = fromAbsoluteOffset;
        this.toRelativeOffset = toRelativeOffset;
        this.toAbsoluteOffset = toAbsoluteOffset;
    }

    public void add(LocalAlignment alignment) {

        List<Pair<Integer, Integer>> fromBlocks = alignment.fromBlocks;
        List<Pair<Integer, Integer>> toBlocks = alignment.toBlocks;

        int fromStart = alignment.fromStart;
        int fromEnd = alignment.fromEnd;

        int toStart = alignment.toStart;
        int toEnd = alignment.toEnd;

        assert alignment.fromSequenceName.equals(fromInterval.getChr()) : String.format("alignment 'from' interval name (%s) should have the same name as constructor 'from' interval (%s)",fromInterval.getChr(), alignment.fromSequenceName);
        assert alignment.toSequenceName.equals(toInterval.getChr()) : String.format("alignment 'to' interval should have the same name as constructor 'to' interval",fromInterval.getChr(), alignment.fromSequenceName);
        assert IntervalTools.isContained(fromStart, fromEnd, fromInterval.getStart(), fromInterval.getEnd()) : "Alignment should be trimmed to the queried interval";
        assert IntervalTools.isContained(toStart, toEnd, toInterval.getStart(), toInterval.getEnd()) : "Alignment should be trimmed to the queried interval";

        logger.info("nBlocks {}", alignment.getNBlocks());
        for (int i = 0; i < alignment.getNBlocks(); i++) {
            Pair<Integer, Integer> fromBlock = fromBlocks.get(i);
            Pair<Integer, Integer> toBlock = toBlocks.get(i);

            int toGenomeOffset = alignment.toNegativeStrand ? toBlock.getValue() : toBlock.getKey();
            int toInc = alignment.toNegativeStrand ? -1 : 1;
            for (int fromGenomeOffset = fromBlock.getKey(); fromGenomeOffset <= fromBlock.getValue(); fromGenomeOffset++, toGenomeOffset += toInc) {
                logger.info("{} <-> {}", fromGenomeOffset, toGenomeOffset);
                int fromRelativeIndex = fromGenomeOffset - fromInterval.getStart();
                int toRelativeIndex = toGenomeOffset - toInterval.getStart();

                logger.info("{} <-> {}\n", fromRelativeIndex, toRelativeIndex);

                fromAbsoluteOffset.set(fromRelativeIndex, Optional.of(toGenomeOffset));
                fromRelativeOffset.set(fromRelativeIndex, Optional.of(toRelativeIndex));

                toAbsoluteOffset.set(toRelativeIndex, Optional.of(fromGenomeOffset));
                toRelativeOffset.set(toRelativeIndex, Optional.of(fromRelativeIndex));
            }
        }
        logger.info("fil {} {}", fromInterval.length(), fromRelativeOffset.size());
        logger.info("til {}", toInterval.length(), toRelativeOffset.size());
        logger.info("from {}", fromRelativeOffset);
        logger.info("to {}", toRelativeOffset);
    }
    
    public NucleotideMapping inverse(){
        return new NucleotideMapping(toInterval, fromInterval, toRelativeOffset, toAbsoluteOffset, fromRelativeOffset, fromAbsoluteOffset);
    }
}
