/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.util.IntervalTools;

/**
 *
 * @author sol
 */
public class LocalAlignment {

    String fromSequenceName;
    String toSequenceName;

    boolean toNegativeStrand;

    List<Pair<Integer, Integer>> fromBlocks;
    List<Pair<Integer, Integer>> toBlocks;

    // the sizes of the aligned blocks
    List<Integer> blockSizes;

    // the boundaries of the regions spanned by the aligned blocks
    private int fromStart;
    private int fromEnd;
    private int toStart;
    private int toEnd;

    public LocalAlignment(String fromSequenceName, String toSequenceName, boolean toNegativeStrand, List<Pair<Integer, Integer>> fromBlocks, List<Pair<Integer, Integer>> toBlocks) {
        this.fromSequenceName = fromSequenceName;
        this.toSequenceName = toSequenceName;
        this.toNegativeStrand = toNegativeStrand;
        this.fromBlocks = fromBlocks;
        this.toBlocks = toBlocks;

        blockSizes = fromBlocks.stream().map(pair -> pair.getValue() - pair.getKey() + 1).collect(Collectors.toList());

        assert fromBlocks.size() == toBlocks.size() : "Expect lists of aligned blocks to have the same length";
        assert blockSizes.equals(toBlocks.stream().map(pair -> pair.getValue() - pair.getKey() + 1).collect(Collectors.toList())) : "Expect query/target block lists to contains blocks with equal sizes";

        fromStart = fromBlocks.get(0).getKey();
        fromEnd = fromBlocks.get(fromBlocks.size() - 1).getValue();
        toStart = toNegativeStrand ? toBlocks.get(fromBlocks.size() - 1).getKey() : toBlocks.get(0).getKey();
        toEnd = toNegativeStrand ? toBlocks.get(0).getValue() : toBlocks.get(fromBlocks.size() - 1).getValue();
    }

    public int getNBlocks() {
        return blockSizes.size();
    }

    public int getBlockSize(int i) {
        return blockSizes.get(i);
    }

    public String getFromSequenceName() {
        return fromSequenceName;
    }

    public String getToSequenceName() {
        return toSequenceName;
    }

    public boolean getToNegativeStrand() {
        return toNegativeStrand;
    }

    public Pair<Integer, Integer> getFromBlock(int i) {
        return fromBlocks.get(i);
    }

    public Pair<Integer, Integer> getToBlock(int i) {
        return toBlocks.get(i);
    }

    public LocalAlignment trim(GenomeSpan fromInterval, GenomeSpan toInterval) {

        assert fromInterval.getChr().equals(fromSequenceName) :
                String.format("Provided fromInterval.getChr() does not match fromSequenceName, "
                        + "'%s' != '%s'", fromInterval.getChr(), fromSequenceName);
        assert toInterval.getChr().equals(toSequenceName) :
                String.format("Provided toInterval.getChr() does not match toSequenceName, "
                        + "'%s' != '%s'", toInterval.getChr(), toSequenceName);

        List<Pair<Integer, Integer>> trimmedFromBlocks = new ArrayList<>();
        List<Pair<Integer, Integer>> trimmedToBlocks = new ArrayList<>();

        for (int i = 0; i < fromBlocks.size(); i++) {

            Pair<Integer, Integer> fromBlock = fromBlocks.get(i);
            Pair<Integer, Integer> toBlock = toBlocks.get(i);

            assert IntervalTools.isContained(fromBlock.getKey(), fromBlock.getValue(), fromInterval.getStart(), fromInterval.getEnd()) : "it is assumed that all blocks in query will be contained in the query interval";

            // if this block overlaps it is either OK as is, or needs to be trimmed
            if (IntervalTools.overlaps(toBlock.getKey(), toBlock.getValue(), toInterval.getStart(), toInterval.getEnd())) {
                if (IntervalTools.isContained(toBlock.getKey(), toBlock.getValue(), toInterval.getStart(), toInterval.getEnd())) {
                    trimmedFromBlocks.add(fromBlock);
                    trimmedToBlocks.add(toBlock);
                } else {
                    int offsetTargetStart = toBlock.getKey() < toInterval.getStart() ? toInterval.getStart() - toBlock.getKey() : 0;
                    int offsetTargetEnd = toBlock.getValue() > toInterval.getEnd() ? toBlock.getValue() - toInterval.getEnd() : 0;

                    Pair<Integer, Integer> offsetToBlock = new Pair<>(toBlock.getKey() + offsetTargetStart, toBlock.getValue() - offsetTargetEnd);
                    Pair<Integer, Integer> offsetFromBlock
                            = toNegativeStrand
                                    ? new Pair<>(fromBlock.getKey() + offsetTargetEnd, fromBlock.getValue() - offsetTargetStart)
                                    : new Pair<>(fromBlock.getKey() + offsetTargetStart, fromBlock.getValue() - offsetTargetEnd);

                    trimmedFromBlocks.add(offsetFromBlock);
                    trimmedToBlocks.add(offsetToBlock);
                }
            }
        }

        return new LocalAlignment(fromSequenceName, toSequenceName, toNegativeStrand, trimmedFromBlocks, trimmedToBlocks);
    }

    /**
     * @return the fromStart
     */
    public int getFromStart() {
        return fromStart;
    }

    /**
     * @return the fromEnd
     */
    public int getFromEnd() {
        return fromEnd;
    }

    /**
     * @return the toStart
     */
    public int getToStart() {
        return toStart;
    }

    /**
     * @return the toEnd
     */
    public int getToEnd() {
        return toEnd;
    }
}
