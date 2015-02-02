/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 *
 * @author sol
 */
public class LocalAlignment {

    String fromSequenceName;
    String toSequenceName;

    boolean toNegativeStrand;

    List<Pair<Integer, Integer>> fromBlocks;
    List<Pair<Integer, Integer>> toBlocks;

    // the sizes of the aligned blocks
    List<Integer> blockSizes;

    // the boundaries of the regions spanned by the aligned blocks
    int fromStart;
    int fromEnd;
    int toStart;
    int toEnd;

    public LocalAlignment(String fromSequenceName, String toSequenceName, boolean toNegativeStrand, List<Pair<Integer, Integer>> fromBlocks, List<Pair<Integer, Integer>> toBlocks) {
        this.fromSequenceName = fromSequenceName;
        this.toSequenceName = toSequenceName;
        this.toNegativeStrand = toNegativeStrand;
        this.fromBlocks = fromBlocks;
        this.toBlocks = toBlocks;
        


        blockSizes = fromBlocks.stream().map(pair -> pair.getValue() - pair.getKey() + 1).collect(Collectors.toList());
        
        assert fromBlocks.size() == toBlocks.size() : "Expect lists of aligned blocks to have the same length";
        assert blockSizes.equals(toBlocks.stream().map(pair -> pair.getValue() - pair.getKey() + 1).collect(Collectors.toList())) : "Expect query/target block lists to contains blocks with equal sizes";
        
        fromStart = fromBlocks.get(0).getKey();
        fromEnd = fromBlocks.get(fromBlocks.size() - 1).getValue();
        toStart = toNegativeStrand ? toBlocks.get(fromBlocks.size() - 1).getKey() : toBlocks.get(0).getKey();
        toEnd = toNegativeStrand ? toBlocks.get(0).getValue() : toBlocks.get(fromBlocks.size() - 1).getValue();
    }
    
    public int getNBlocks(){
        return blockSizes.size();
    }

    public int getBlockSize(int i){
        return blockSizes.get(i);
    }
    
    public String getFromSequenceName(){
        return fromSequenceName;
    }
    
    public String getToSequenceName(){
        return fromSequenceName;
    }
    
    public boolean getToNegativeStrand(){
        return toNegativeStrand;
    }
    
    public Pair<Integer,Integer> getFromBlock(int i){
        return fromBlocks.get(i);
    }
    
    public Pair<Integer,Integer> getToBlock(int i){
        return toBlocks.get(i);
    }
}
