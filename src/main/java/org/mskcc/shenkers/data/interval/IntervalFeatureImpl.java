/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.data.interval;

import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.bed.FullBEDFeature;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class IntervalFeatureImpl<T> implements IntervalFeature<T> {

    private static final Logger logger = LogManager.getLogger ();
   
    protected String chr;
    protected int start;
    protected int end;
    protected Strand strand = Strand.NONE;
    T value = null;
  
    public IntervalFeatureImpl(String chr, int start, int end) {
        this.start = start;
        this.end = end;
        this.chr = chr;
    }
    
    public IntervalFeatureImpl(String chr, int start, int end, Strand strand) {
        this(chr, start, end);
        this.strand=strand;
    }
    
    public IntervalFeatureImpl(String chr, int start, int end, Strand strand, T value) {
        this(chr, start, end, strand);
        this.value=value;
    }

    public String getChr() {
        return chr;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Strand getStrand() {
        return strand;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s:%d-%d:%s : ", chr,start,end,strand,value);
    }    
    
}