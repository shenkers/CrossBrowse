/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author Soma
 */
public class AlignedBlocks {

    String fromSequenceName;
    String toSequenceName;

    boolean toNegativeStrand;

    List<Pair<Integer, Integer>> fromBlocks;
    List<Pair<Integer, Integer>> toBlocks;

    public AlignedBlocks(String fromSequenceName, String toSequenceName, boolean toNegativeStrand, List<Pair<Integer, Integer>> fromBlocks, List<Pair<Integer, Integer>> toBlocks) {
        this.fromSequenceName = fromSequenceName;
        this.toSequenceName = toSequenceName;
        this.toNegativeStrand = toNegativeStrand;
        this.fromBlocks = fromBlocks;
        this.toBlocks = toBlocks;
    }

    
}
