/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.model;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class CoordinateChangeEvent {

    private static final Logger logger = LogManager.getLogger ();
    
    private Genome genome;
    private Optional<GenomeSpan> prev;
    private Optional<GenomeSpan> next;

    public CoordinateChangeEvent(Genome genome, Optional<GenomeSpan> prev, Optional<GenomeSpan> next) {
        this.genome = genome;
        this.prev = prev;
        this.next = next;
    }   

    /**
     * @return the genome
     */
    public Genome getGenome() {
        return genome;
    }

    /**
     * @return the prev
     */
    public Optional<GenomeSpan> getPrev() {
        return prev;
    }

    /**
     * @return the next
     */
    public Optional<GenomeSpan> getNext() {
        return next;
    }
}
