/*
 * The MIT License
 *
 * Copyright (c) 2009 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package htsjdk.samtools.liftover;

import org.mskcc.shenkers.control.alignment.LocalAlignment;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.util.Pair;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 * Java port of UCSC liftOver. Only the most basic liftOver functionality is
 * implemented. Internally coordinates are 0-based, half-open. The API is
 * standard Picard 1-based, inclusive.
 *
 * @author alecw@broadinstitute.org
 */
public class ChainParser {

    private final OverlapDetector<Chain> chains;

    ChainValidationStringency stringency = ChainValidationStringency.silent;
    
    /**
     * Load UCSC chain file in order to lift over Intervals.
     */
    public ChainParser(File chainFile) {
        IOUtil.assertFileIsReadable(chainFile);
        chains = Chain.loadChains(chainFile, stringency);
    }

    /**
     * Throw an exception if all the "to" sequence names in the chains are not
     * found in the given sequence dictionary.
     */
    public void validateToSequences(final SAMSequenceDictionary sequenceDictionary) {
        for (final Chain chain : chains.getAll()) {
            if (sequenceDictionary.getSequence(chain.toSequenceName) == null) {
                throw new SAMException("Sequence " + chain.toSequenceName + " from chain file is not found in sequence dictionary.");
            }
        }

    }
    
    public List<LocalAlignment> getChainIntersections(GenomeSpan interval) {
        return getChainIntersections(new Interval(interval.getChr(), interval.getStart(), interval.getEnd()));
    }

    /**
     * Lift over the given interval to the new genome build.
     *
     * @param interval Interval to be lifted over.
     * @param liftOverMinMatch Minimum fraction of bases that must remap.
     * @return Interval in the output build coordinates, or null if it cannot be
     * lifted over.
     */
    public List<LocalAlignment> getChainIntersections(final Interval interval) {
        if (interval.length() == 0) {
            throw new IllegalArgumentException("Zero-length interval cannot be lifted over.  Interval: "
                    + interval.getName());
        }

        List<LocalAlignment> hits = new ArrayList<>();

        // Find the appropriate Chain, and the part of the chain corresponding to the interval to be lifted over.
        for (final Chain chain : chains.getOverlaps(interval)) {
            getAlignedBlocks(chain, interval).ifPresent(alignment -> hits.add(alignment));
        }

        return hits;
    }

    /**
     * Add up overlap btw the blocks in this chain and the given interval.
     *
     * @return Length of overlap, offsets into first and last ContinuousBlocks,
     * and indices of first and last ContinuousBlocks.
     */
    private static Optional<LocalAlignment> getAlignedBlocks(final Chain chain, final Interval interval) {
        // Convert interval to 0-based, half-open
        int start = interval.getStart() - 1;
        int end = interval.getEnd();

        List<Chain.ContinuousBlock> blockList = chain.getBlocks();

        List<Pair<Integer, Integer>> fromBlocks = new ArrayList<>();
        List<Pair<Integer, Integer>> toBlocks = new ArrayList<>();
        
        boolean hasIntersection = false;
        
        for (int i = 0; i < blockList.size(); ++i) {
            final Chain.ContinuousBlock block = blockList.get(i);
            if (block.fromStart >= end) {
                break;
            } else if (block.getFromEnd() <= start) {
                continue;
            }
            
            hasIntersection = true;

            int fromStart = start > block.fromStart ? start : block.fromStart;
            int fromEnd = block.getFromEnd() > end ? end : block.getFromEnd();

            Integer startOffset = (start > block.fromStart)
                    ? start - block.fromStart
                    : 0;

            int offsetFromEnd = (block.getFromEnd() > end)
                    ? block.getFromEnd() - end
                    : 0;

            int toStart = chain.toNegativeStrand
                    ? chain.toSequenceSize - (chain.getBlock(i).getToEnd() - offsetFromEnd)
                    : chain.getBlock(i).toStart + startOffset;
            int toEnd = chain.toNegativeStrand
                    ? chain.toSequenceSize - (chain.getBlock(i).toStart + startOffset)
                    : chain.getBlock(i).getToEnd() - offsetFromEnd;

            // convert from 0 to 1 based interval start
            fromBlocks.add(new Pair<>(fromStart + 1, fromEnd));
            toBlocks.add(new Pair<>(toStart + 1, toEnd));

        }
        
        Optional<LocalAlignment> alignment = 
                hasIntersection ? 
                Optional.of(new LocalAlignment(chain.fromSequenceName, chain.toSequenceName, chain.toNegativeStrand, fromBlocks, toBlocks)):
                Optional.empty();

        return alignment;
    }

    /**
     * Add up overlap btw the blocks in this chain and the given interval.
     *
     * @return Length of overlap, offsets into first and last ContinuousBlocks,
     * and indices of first and last ContinuousBlocks.
     */
    private static TargetIntersection targetIntersection(final Chain chain, final Interval interval) {
        int intersectionLength = 0;
        // Convert interval to 0-based, half-open
        int start = interval.getStart() - 1;
        int end = interval.getEnd();
        int firstBlockIndex = -1;
        int lastBlockIndex = -1;
        int startOffset = -1;
        int offsetFromEnd = -1;
        List<Chain.ContinuousBlock> blockList = chain.getBlocks();
        for (int i = 0; i < blockList.size(); ++i) {
            final Chain.ContinuousBlock block = blockList.get(i);
            if (block.fromStart >= end) {
                break;
            } else if (block.getFromEnd() <= start) {
                continue;
            }
            if (firstBlockIndex == -1) {
                firstBlockIndex = i;
                if (start > block.fromStart) {
                    startOffset = start - block.fromStart;
                } else {
                    startOffset = 0;
                }
            }
            lastBlockIndex = i;
            if (block.getFromEnd() > end) {
                offsetFromEnd = block.getFromEnd() - end;
            } else {
                offsetFromEnd = 0;
            }
            int thisIntersection = Math.min(end, block.getFromEnd()) - Math.max(start, block.fromStart);
            if (thisIntersection <= 0) {
                throw new SAMException("Should have been some intersection.");
            }
            intersectionLength += thisIntersection;
        }
        if (intersectionLength == 0) {
            return null;
        }
        return new TargetIntersection(chain, intersectionLength, startOffset, offsetFromEnd, firstBlockIndex, lastBlockIndex);
    }

    /**
     * Value class returned by targetIntersection()
     */
    private static class TargetIntersection {

        /**
         * Chain used for this intersection
         */
        final Chain chain;
        /**
         * Total intersectionLength length
         */
        final int intersectionLength;
        /**
         * Offset of target interval start in first block.
         */
        final int startOffset;
        /**
         * Distance from target interval end to end of last block.
         */
        final int offsetFromEnd;
        /**
         * Index of first ContinuousBlock matching interval.
         */
        final int firstBlockIndex;
        /**
         * Index of last ContinuousBlock matching interval.
         */
        final int lastBlockIndex;

        TargetIntersection(final Chain chain, final int intersectionLength, final int startOffset,
                final int offsetFromEnd, final int firstBlockIndex, final int lastBlockIndex) {
            this.chain = chain;
            this.intersectionLength = intersectionLength;
            this.startOffset = startOffset;
            this.offsetFromEnd = offsetFromEnd;
            this.firstBlockIndex = firstBlockIndex;
            this.lastBlockIndex = lastBlockIndex;
        }
    }

    /**
     * Represents a portion of a liftover operation, for use in diagnosing
     * liftover failures.
     */
    public static class PartialLiftover {

        /**
         * Intersection between "from" interval and "from" region of a chain.
         */
        final Interval fromInterval;
        /**
         * Result of lifting over fromInterval (with no percentage mapped
         * requirement). This is null if fromInterval falls entirely with a gap
         * of the chain.
         */
        final Interval toInterval;
        /**
         * id of chain used for this liftover
         */
        final int chainId;
        /**
         * Percentage of bases in fromInterval that lifted over. 0 if
         * fromInterval is not covered by any chain.
         */
        final float percentLiftedOver;

        PartialLiftover(final Interval fromInterval, final Interval toInterval, final int chainId, final float percentLiftedOver) {
            this.fromInterval = fromInterval;
            this.toInterval = toInterval;
            this.chainId = chainId;
            this.percentLiftedOver = percentLiftedOver;
        }

        PartialLiftover(final Interval fromInterval, final int chainId) {
            this.fromInterval = fromInterval;
            this.toInterval = null;
            this.chainId = chainId;
            this.percentLiftedOver = 0.0f;
        }

        public String toString() {
            if (toInterval == null) {
                // Matched a chain, but entirely within a gap.
                return fromInterval.toString() + " (len " + fromInterval.length() + ")=>null using chain " + chainId;
            }
            final String strand = toInterval.isNegativeStrand() ? "-" : "+";
            return fromInterval.toString() + " (len " + fromInterval.length() + ")=>" + toInterval + "(" + strand
                    + ") using chain " + chainId + " ; pct matched " + percentLiftedOver;
        }
    }
}
