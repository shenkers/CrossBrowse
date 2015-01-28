/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import java.io.File;
import java.util.List;

/**
 *
 * @author Soma
 */
public class ChainAlignmentSource extends ChainParser{

    private final OverlapDetector<Chain> chains = null;

    public ChainAlignmentSource(File chainFile) {
        super(chainFile);
    }

    public void test() {
        File chainFile = null;
        ChainParser lo = new ChainParser(chainFile);
//        lo.diagnosticLiftover(null)
    }

    public Interval liftOver(final Interval interval, final double liftOverMinMatch) {
        if (interval.length() == 0) {
            throw new IllegalArgumentException("Zero-length interval cannot be lifted over.  Interval: "
                    + interval.getName());
        }
        Chain chainHit = null;
        ChainParser.TargetIntersection targetIntersection = null;
        // Number of bases in interval that can be lifted over must be >= this.
        double minMatchSize = liftOverMinMatch * interval.length();

        // Find the appropriate Chain, and the part of the chain corresponding to the interval to be lifted over.
        for (final Chain chain : chains.getOverlaps(interval)) {
            final TargetIntersection candidateIntersection = targetIntersection(chain, interval);
            if (candidateIntersection != null && candidateIntersection.intersectionLength >= minMatchSize) {
                if (chainHit != null) {
                    // In basic liftOver, multiple hits are not allowed.
                    return null;
                }
                chainHit = chain;
                targetIntersection = candidateIntersection;
            } else if (candidateIntersection != null) {
//                LOG.info("Interval " + interval.getName() + " failed to match chain " + chain.id
//                        + " because intersection length " + candidateIntersection.intersectionLength + " < minMatchSize "
//                        + minMatchSize
//                        + " (" + (candidateIntersection.intersectionLength / (float) interval.length()) + " < " + liftOverMinMatch + ")");
            }
        }
        if (chainHit == null) {
            // Can't be lifted over.
            return null;
        }

        return createToInterval(interval.getName(), targetIntersection);
    }
}
