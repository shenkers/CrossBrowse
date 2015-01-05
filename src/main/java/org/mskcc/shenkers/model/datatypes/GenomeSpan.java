/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model.datatypes;

/**
 *
 * @author sol
 */
public class GenomeSpan {
    private String chr; private Integer start; private Integer end; private Boolean negativeStrand;

    public GenomeSpan(String chr, int start, int end, boolean isNegativeStrand) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.negativeStrand = isNegativeStrand;
    }

    /**
     * @return the chr
     */
    public String getChr() {
        return chr;
    }

    /**
     * @param chr the chr to set
     */
    public void setChr(String chr) {
        this.chr = chr;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @return the negativeStrand
     */
    public boolean isNegativeStrand() {
        return negativeStrand;
    }

    /**
     * @param negativeStrand the negativeStrand to set
     */
    public void setNegativeStrand(boolean negativeStrand) {
        this.negativeStrand = negativeStrand;
    }

    @Override
    public String toString() {
        return chr+":"+start+"-"+":"+end+":"+(isNegativeStrand() ? '-' : '+');
    }
    
    
}
