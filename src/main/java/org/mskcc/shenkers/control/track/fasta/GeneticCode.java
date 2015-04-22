/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class GeneticCode {

    private static final Logger logger = LogManager.getLogger();
    Map<String, String> genetic_code = new HashMap<String, String>();
    Map<Character, Character> complement = new HashMap<>();

    public GeneticCode() {

        genetic_code.put("TTT", "F");
        genetic_code.put("TTC", "F");
        genetic_code.put("TTA", "L");
        genetic_code.put("TTG", "L");
        genetic_code.put("CTT", "L");
        genetic_code.put("CTC", "L");
        genetic_code.put("CTA", "L");
        genetic_code.put("CTG", "L");
        genetic_code.put("ATT", "I");
        genetic_code.put("ATC", "I");
        genetic_code.put("ATA", "I");
        genetic_code.put("ATG", "M");
        genetic_code.put("GTT", "V");
        genetic_code.put("GTC", "V");
        genetic_code.put("GTA", "V");
        genetic_code.put("GTG", "V");
        genetic_code.put("TCT", "S");
        genetic_code.put("TCC", "S");
        genetic_code.put("TCA", "S");
        genetic_code.put("TCG", "S");
        genetic_code.put("CCT", "P");
        genetic_code.put("CCC", "P");
        genetic_code.put("CCA", "P");
        genetic_code.put("CCG", "P");
        genetic_code.put("ACT", "T");
        genetic_code.put("ACC", "T");
        genetic_code.put("ACA", "T");
        genetic_code.put("ACG", "T");
        genetic_code.put("GCT", "A");
        genetic_code.put("GCC", "A");
        genetic_code.put("GCA", "A");
        genetic_code.put("GCG", "A");
        genetic_code.put("TAT", "Y");
        genetic_code.put("TAC", "Y");
        genetic_code.put("TAA", "*");
        genetic_code.put("TAG", "*");
        genetic_code.put("CAT", "H");
        genetic_code.put("CAC", "H");
        genetic_code.put("CAA", "Q");
        genetic_code.put("CAG", "Q");
        genetic_code.put("AAT", "N");
        genetic_code.put("AAC", "N");
        genetic_code.put("AAA", "K");
        genetic_code.put("AAG", "K");
        genetic_code.put("GAT", "D");
        genetic_code.put("GAC", "D");
        genetic_code.put("GAA", "E");
        genetic_code.put("GAG", "E");
        genetic_code.put("TGT", "C");
        genetic_code.put("TGC", "C");
        genetic_code.put("TGA", "*");
        genetic_code.put("TGG", "W");
        genetic_code.put("CGT", "R");
        genetic_code.put("CGC", "R");
        genetic_code.put("CGA", "R");
        genetic_code.put("CGG", "R");
        genetic_code.put("AGT", "S");
        genetic_code.put("AGC", "S");
        genetic_code.put("AGA", "R");
        genetic_code.put("AGG", "R");
        genetic_code.put("GGT", "G");
        genetic_code.put("GGC", "G");
        genetic_code.put("GGA", "G");
        genetic_code.put("GGG", "G");

        complement.put('A', 'T');
        complement.put('T', 'A');
        complement.put('G', 'C');
        complement.put('C', 'G');
    }

    public List<String> codons(String seq, Frame f) {

        int skip = 0;
        switch (f) {
            case p0:
                skip = 0;
                break;
            case p1:
                skip = 1;
                break;
            case p2:
                skip = 2;
                break;
        }
        logger.info("frame {} skip {}", f, skip);
        List<String> codons = new ArrayList<>((seq.length() - skip) / 3);
        for (int i = skip; i + 3 <= seq.length(); i += 3) {
            codons.add(seq.substring(i, i + 3));
        }

        return codons;
    }

    public String translate(String dnaSequence) {
        assert dnaSequence.length() % 3 == 0 : String.format("Sequence must have a length that is a multiple of 3 (got %d)", dnaSequence.length());
        if (dnaSequence.length() % 3 != 0) {
            throw new IllegalArgumentException("Sequence must have a length that is a multiple of 3");
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < dnaSequence.length(); i += 3) {
            String codon = dnaSequence.substring(i, i + 3);
            String peptide = genetic_code.getOrDefault(codon, "?");
            sb.append(peptide);
        }

        return sb.toString();
    }

    public String reverse(String seq) {
        StringBuilder reverseComplement = new StringBuilder(seq);
        return reverseComplement.reverse().toString();
    }

    public String complement(String seq) {
        StringBuilder sb = new StringBuilder(seq.length());
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            sb.append(complement.getOrDefault(c, c));
        }
        return sb.toString();
    }
    
    public String revComp(String seq){
        return reverse(complement(seq));
    }

}
