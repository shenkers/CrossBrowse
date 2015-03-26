/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.jersey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.Genome;

/**
 *
 * @author sol
 */
public class CoordinateRequest {

    private static final Logger logger = LogManager.getLogger ();
    
    private String genome_id;
    
    private String chr;
    private int start;
    private int end;
    private boolean isNegativeStrand;

    public CoordinateRequest() {
    }

    /**
     * @return the genome_id
     */
    public String getGenome_id() {
        return genome_id;
    }

    /**
     * @param genome_id the genome_id to set
     */
    public void setGenome_id(String genome_id) {
        this.genome_id = genome_id;
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
     * @return the isNegativeStrand
     */
    public boolean isIsNegativeStrand() {
        return isNegativeStrand;
    }

    /**
     * @param isNegativeStrand the isNegativeStrand to set
     */
    public void setIsNegativeStrand(boolean isNegativeStrand) {
        this.isNegativeStrand = isNegativeStrand;
    }
    
    
   
}
