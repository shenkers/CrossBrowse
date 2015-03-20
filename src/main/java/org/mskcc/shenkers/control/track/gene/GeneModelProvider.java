/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

/**
 *
 * @author sol
 */
public interface GeneModelProvider {
    
    public Iterable<GeneModel> query(String chr, int start, int end);
}
