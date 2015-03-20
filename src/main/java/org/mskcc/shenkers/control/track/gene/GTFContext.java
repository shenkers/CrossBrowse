/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import htsjdk.tribble.BasicFeature;

/**
 *
 * @author sol
 */
public class GTFContext extends BasicFeature {

    private String source;
    private String feature;
    private String score;
    private char strand;
    private String frame;
    private String attributes;

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the feature
     */
    public String getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(String feature) {
        this.feature = feature;
    }

    /**
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * @return the strand
     */
    public char getStrand() {
        return strand;
    }

    /**
     * @param strand the strand to set
     */
    public void setStrand(char strand) {
        this.strand = strand;
    }

    /**
     * @return the frame
     */
    public String getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(String frame) {
        this.frame = frame;
    }

    /**
     * @return the attributes
     */
    public String getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the transcript_id
     */
    public String getTranscriptId() {
        return transcript_id;
    }

    /**
     * @param transcript_id the transcript_id to set
     */
    public void setTranscriptId(String transcript_id) {
        this.transcript_id = transcript_id;
    }

    /**
     * @return the gene_id
     */
    public String getGeneId() {
        return gene_id;
    }

    /**
     * @param gene_id the gene_id to set
     */
    public void setGeneId(String gene_id) {
        this.gene_id = gene_id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    enum Feature {

        exon, CDS
    }

    enum Frame {

        first, second, third
    }

    private String transcript_id;
    private String gene_id;
    private String name;

    public GTFContext(String chr, int start, int stop) {
        super(chr, start, stop);
    }

}
