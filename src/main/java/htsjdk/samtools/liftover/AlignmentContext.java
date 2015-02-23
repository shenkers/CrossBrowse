/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.tribble.BasicFeature;
import java.util.List;

/**
 *
 * @author Soma
 */
public class AlignmentContext extends BasicFeature {

    private List<Integer> queryStarts;
    private List<Integer> targetStarts;
    private List<Integer> blockLengths;
    private String targetChr;
    private int targetStart, targetEnd;
    private String id;
    private double score;
    private boolean toNegativeStrand;

    public AlignmentContext(String chr, int start, int stop) {
        super(chr, start, stop);
    }

    /**
     * @return the queryStarts
     */
    public List<Integer> getQueryStarts() {
        return queryStarts;
    }

    /**
     * @param queryStarts the queryStarts to set
     */
    public void setQueryStarts(List<Integer> queryStarts) {
        this.queryStarts = queryStarts;
    }

    /**
     * @return the targetStarts
     */
    public List<Integer> getTargetStarts() {
        return targetStarts;
    }

    /**
     * @param targetStarts the targetStarts to set
     */
    public void setTargetStarts(List<Integer> targetStarts) {
        this.targetStarts = targetStarts;
    }

    /**
     * @return the blockLengths
     */
    public List<Integer> getBlockLengths() {
        return blockLengths;
    }

    /**
     * @param blockLengths the blockLengths to set
     */
    public void setBlockLengths(List<Integer> blockLengths) {
        this.blockLengths = blockLengths;
    }

    /**
     * @return the toNegativeStrand
     */
    public boolean getToNegativeStrand() {
        return toNegativeStrand;
    }

    /**
     * @param toNegativeStrand the toNegativeStrand to set
     */
    public void setToNegativeStrand(boolean toNegativeStrand) {
        this.toNegativeStrand = toNegativeStrand;
    }

    /**
     * @return the targetChr
     */
    public String getTargetChr() {
        return targetChr;
    }

    /**
     * @param targetChr the targetChr to set
     */
    public void setTargetChr(String targetChr) {
        this.targetChr = targetChr;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return the targetStart
     */
    public int getTargetStart() {
        return targetStart;
    }

    /**
     * @param targetStart the targetStart to set
     */
    public void setTargetStart(int targetStart) {
        this.targetStart = targetStart;
    }

    /**
     * @return the targetEnd
     */
    public int getTargetEnd() {
        return targetEnd;
    }

    /**
     * @param targetEnd the targetEnd to set
     */
    public void setTargetEnd(int targetEnd) {
        this.targetEnd = targetEnd;
    }

}
