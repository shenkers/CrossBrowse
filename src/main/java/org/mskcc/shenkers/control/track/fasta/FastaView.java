/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.inhibeans.property.SimpleBooleanProperty;

/**
 *
 * @author sol
 */
public class FastaView extends VBox {

    private static final Logger logger = LogManager.getLogger();
    
    BooleanProperty flip = new SimpleBooleanProperty(false);
    BooleanProperty complement = new SimpleBooleanProperty(false);
    GeneticCode code = new GeneticCode();

    /**
     * 
     * @param frameOffset - the frame offset is determined by the start position
     * of the queried sequence, ensuring peptides stay in the same row
     * @param dnaSeq 
     */
    public void setSequence(int frameOffset, String dnaSeq) {

        int len = dnaSeq.length();

        boolean isFlipped = flip.get(); 
        boolean isComplemented = complement.get();
        
        Seq[] translations = new Seq[3];
        
        Frame[] frames = Frame.values();
        int o = 3-(len%3);
        logger.info("offset o {}",o);
        for(int i=0; i<3; i++){
             List<String> tranlation = translate(dnaSeq, frames[i], isFlipped, isComplemented);
             translations[(i+frameOffset)%3] = new TSeqBuilder(len, tranlation, frames[isFlipped ? ((3+len-i)%3) : i]).build();
        }
        
        String flipped = isFlipped ? code.reverse(dnaSeq) : dnaSeq;
        String complemented = isComplemented ? code.complement(flipped) : flipped;
      

        HBox spacer = new HBox();
        spacer.setPrefHeight(10.);
        spacer.setMinHeight(10.);
        getChildren().setAll(new SeqBuilder(complemented).build());
        getChildren().addAll(translations);

    }
    
    public List<String> translate(String seq, Frame f, boolean isFlipped, boolean isComplemented){
        
        List<String> codons = code.codons(seq, f);
        List<String> translation = new ArrayList<>(codons.size());
        for(String codon : codons){
            translation.add(code.translate(isComplemented ? code.revComp(codon) : codon));
        }
        if(isFlipped){
            Collections.reverse(translation);
        }
        
        return translation;
    }

}
