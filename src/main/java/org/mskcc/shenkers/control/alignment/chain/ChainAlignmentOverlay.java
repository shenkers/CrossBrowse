/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.alignment.AlignmentWeaver;
import org.mskcc.shenkers.control.alignment.CurvedOverlayPath;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import org.mskcc.shenkers.control.alignment.NucleotideMapping;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.util.IntervalTools;

/**
 *
 * @author sol
 */
public class ChainAlignmentOverlay {

    Logger logger = LogManager.getLogger();

    Map<Pair<Genome, Genome>, AlignmentSource> alignmentSources;

    public ChainAlignmentOverlay(Map<Pair<Genome, Genome>, AlignmentSource> alignmentSources) {
        this.alignmentSources = alignmentSources;
    }

    private AlignmentWeaver initializeWeaver(Pair<Genome, Genome> genomePair, Map<Genome, GenomeSpan> displayedSpans) {
        Genome fromGenome = genomePair.getKey();
        Genome toGenome = genomePair.getValue();

        GenomeSpan fromInterval = displayedSpans.get(fromGenome);
        GenomeSpan toInterval = displayedSpans.get(toGenome);

        AlignmentWeaver weaver = new AlignmentWeaver(fromGenome, fromInterval);

        NucleotideMapping mappingFromGenomeToGenome = new NucleotideMapping(fromInterval, toInterval);

        logger.info("Adding alignments from {} to {}", fromGenome, toGenome);
        Set<Boolean> toNegativeStrand = new HashSet<>();
        for (LocalAlignment localAlignment : alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval)) {
            logger.info("before trim: {}-{}",localAlignment.getToStart(), localAlignment.getToEnd());
            logger.info("after trim: {}-{}",localAlignment.trim(fromInterval, toInterval).getToStart(), localAlignment.trim(fromInterval, toInterval).getToEnd());
            mappingFromGenomeToGenome.add(localAlignment.trim(fromInterval, toInterval));
            toNegativeStrand.add(localAlignment.getToNegativeStrand());
        }
        
        assert toNegativeStrand.size()==1 : "Can't represent alignments go to plus and minus strand yet";

        weaver.add(toInterval, fromGenome, toGenome, toNegativeStrand.iterator().next(), mappingFromGenomeToGenome);

        return weaver;
    }

    private void addAlignment(Pair<Genome, Genome> genomePair, Genome toAdd, Map<Genome, GenomeSpan> displayedSpans, AlignmentWeaver weaver) {

        Genome fromGenome = genomePair.getKey();
        Genome toGenome = genomePair.getValue();

        GenomeSpan fromInterval = displayedSpans.get(fromGenome);
        GenomeSpan toInterval = displayedSpans.get(toGenome);

        // since the pairwise alignment has a polarity, need to make 
        // sure it's going the correct direction to be woven
        boolean alignmentInverted = toAdd.equals(fromGenome);

        NucleotideMapping mappingFromGenomeToGenome = new NucleotideMapping(fromInterval, toInterval);

        logger.info("Adding alignments from {} to {}", fromGenome, toGenome);
         Set<Boolean> toNegativeStrand = new HashSet<>();
       for (LocalAlignment localAlignment : alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval)) {
               logger.info("before trim to {}: {}-{}", toInterval, localAlignment.getToStart(), localAlignment.getToEnd());
            logger.info("after trim: {}-{}",localAlignment.trim(fromInterval, toInterval).getToStart(), localAlignment.trim(fromInterval, toInterval).getToEnd());
         
           mappingFromGenomeToGenome.add(localAlignment.trim(fromInterval, toInterval));
                 toNegativeStrand.add(localAlignment.getToNegativeStrand());
   }
        assert toNegativeStrand.size()==1 : "Can't represent alignments go to plus and minus strand yet";
        if (alignmentInverted) {
            weaver.add(fromInterval, toGenome, fromGenome, toNegativeStrand.iterator().next(), mappingFromGenomeToGenome.inverse());
        } else {
            weaver.add(toInterval, fromGenome, toGenome, toNegativeStrand.iterator().next(), mappingFromGenomeToGenome);
        }
    }
    
    

    public List<Map<Genome, Pair<Double, Double>>> getAlignmentColumnsRelativeX(Map<Genome, GenomeSpan> displayedSpans, int basesPerColumn) {
        assert basesPerColumn > 0 : String.format("basesPerColumn should b > 0, (got %d)", basesPerColumn);
        
        Set<Genome> incorporated = new HashSet<>();
        Set<Pair<Genome, Genome>> remainder = new HashSet<>(alignmentSources.keySet());

        AlignmentWeaver weaver = null;
        while (!remainder.isEmpty()) {

            Iterator<Pair<Genome, Genome>> it = remainder.iterator();

            while (it.hasNext()) {
                Pair<Genome, Genome> p = it.next();
                // if one of the genomes in this pair has not yet been woven in
                // if one of the genomes in the pair is already in the alignment we can weave this one in
                if (incorporated.size() == 0 || incorporated.contains(p.getKey()) ^ incorporated.contains(p.getValue())) {
                    logger.info("incorporating pair {} {}", p.getKey().getId(), p.getValue().getId());
                    it.remove();
                    incorporated.add(p.getKey());
                    incorporated.add(p.getValue());

                    if (weaver == null) {
                        weaver = initializeWeaver(p, displayedSpans);
                    } else {
                        Genome toAdd = incorporated.contains(p.getKey()) ? p.getValue() : p.getKey();
                        logger.info("adding {} to the alignment",toAdd);
                        addAlignment(p, toAdd, displayedSpans, weaver);
                    }
                }
            }
        }

        Map<Genome, List<Integer>> order = weaver.getOrder();
        
        logger.info("order keyset {}", order.keySet());
     
        Integer max = order.values().stream().flatMap(o -> o.stream()).max((x, y) -> x - y).get();

        Map<Genome, Integer> displayedSpanLength = displayedSpans.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().length()));

        List<Map<Genome, Pair<Double, Double>>> relativeCoordinates = new ArrayList<>();
        Map<Genome, Pair<Double, Double>> previous = displayedSpans.keySet().stream().collect(Collectors.toMap(g -> g, g -> new Pair<Double, Double>(0., 0.)));
        for (int i = 0; i <= max; i += basesPerColumn) {
            Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
            relativeCoordinates.add(relativeX);

            for (Genome g : displayedSpans.keySet()) {
                List<Integer> gOrder = order.get(g);
                int l = displayedSpanLength.get(g);

                List<Integer> indices = new ArrayList<>();
                for (int j = i; j < i + basesPerColumn; j++) {
                    int index = gOrder.indexOf(j);
                    if (index != -1) {
                        indices.add(index);
                    }
                }
                Pair<Double, Double> next = null;
                if (indices.size() > 0) {
                    next = new Pair<>((Collections.min(indices) + 0. )/ l, (Collections.max(indices) + 1. )/ l);
                } else {
                   next = new Pair<>(previous.get(g).getValue(),previous.get(g).getValue());
                }
                relativeX.put(g, next);
                
            }
            
            
            previous = relativeX;

        }
        if(max % basesPerColumn != 0){
            int remainingColumns = max % basesPerColumn;
            
            Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
            relativeCoordinates.add(relativeX);

            for (Genome g : displayedSpans.keySet()) {
                List<Integer> gOrder = order.get(g);
                int l = displayedSpanLength.get(g);

                List<Integer> indices = new ArrayList<>();
                for (int j = max - remainingColumns; j <= max; j++) {
                    int index = gOrder.indexOf(j);
                    if (index != -1) {
                        indices.add(index);
                    }
                }
                Pair<Double, Double> next = null;
                if (indices.size() > 0) {
                    next = new Pair<>((Collections.min(indices) + 0. )/ l, (Collections.max(indices) + 1. )/ l);
                } else {
                    next = new Pair<>(previous.get(g).getValue(),previous.get(g).getValue());
                }
                relativeX.put(g, next);
                
            }
        }

        return relativeCoordinates;
    }
}