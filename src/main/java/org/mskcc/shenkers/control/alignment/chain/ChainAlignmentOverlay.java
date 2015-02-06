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

/**
 *
 * @author sol
 */
public class ChainAlignmentOverlay {

    Logger logger = LogManager.getLogger();

    Map<Pair<Genome, Genome>, AlignmentSource> alignmentSources;

    private AlignmentWeaver initializeWeaver(Pair<Genome, Genome> genomePair, Map<Genome, GenomeSpan> displayedSpans) {
        Genome fromGenome = genomePair.getKey();
        Genome toGenome = genomePair.getValue();

        GenomeSpan fromInterval = displayedSpans.get(fromGenome);
        GenomeSpan toInterval = displayedSpans.get(toGenome);

        AlignmentWeaver weaver = new AlignmentWeaver(fromGenome, fromInterval);

        NucleotideMapping mappingFromGenomeToGenome = new NucleotideMapping(fromInterval, toInterval);

        for (LocalAlignment localAlignment : alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval)) {
            mappingFromGenomeToGenome.add(localAlignment);
        }

        weaver.add(toInterval, fromGenome, toGenome, mappingFromGenomeToGenome);

        return weaver;
    }

    private void addAlignment(Pair<Genome, Genome> genomePair, Genome toAdd, Map<Genome, GenomeSpan> displayedSpans, AlignmentWeaver weaver) {

        Genome fromGenome = genomePair.getKey();
        Genome toGenome = genomePair.getValue();

        GenomeSpan fromInterval = displayedSpans.get(fromGenome);
        GenomeSpan toInterval = displayedSpans.get(toGenome);

        // since the pairwise alignment has a polarity, need to make 
        // sure it's going the correct direction to be woven
        boolean alignmentInverted = toAdd.equals(toGenome);

        NucleotideMapping mappingFromGenomeToGenome = new NucleotideMapping(fromInterval, toInterval);

        for (LocalAlignment localAlignment : alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval)) {
            mappingFromGenomeToGenome.add(localAlignment);
        }

        if (alignmentInverted) {
            weaver.add(fromInterval, toGenome, fromGenome, mappingFromGenomeToGenome.inverse());
        } else {
            weaver.add(toInterval, fromGenome, toGenome, mappingFromGenomeToGenome);
        }
    }

    public void weave(Map<Genome, GenomeSpan> displayedSpans) {
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
                        weaver = initializeWeaver(it.next(), displayedSpans);
                    } else {
                        Genome toAdd = incorporated.contains(p.getKey()) ? p.getValue() : p.getKey();
                        addAlignment(p, toAdd, displayedSpans, weaver);
                    }
                }
            }
        }

        Map<Genome, List<Integer>> order = weaver.getOrder();

        int inc = 3;

        Integer max = order.values().stream().flatMap(o -> o.stream()).max((x, y) -> x - y).get();

        List<Map<Genome, Pair<Double, Double>>> relativeCoordinates = new ArrayList<>();
        Map<Genome, Pair<Integer, Integer>> previous = displayedSpans.keySet().stream().collect(Collectors.toMap(g -> g, g -> new Pair<Integer, Integer>(0, 0)));
        for (int i = 0; i <= max; i += inc) {
            Map<Genome, Pair<Integer, Integer>> extremeAbsoluteX = new HashMap<>();

            for (Genome g : displayedSpans.keySet()) {
                List<Integer> gOrder = order.get(g);

                List<Integer> indices = new ArrayList<>();
                for (int j = i; j < i + inc; j++) {
                    int index = gOrder.indexOf(j);
                    if (index != -1) {
                        indices.add(index);
                    }
                }
                Pair<Integer, Integer> next = null;
                if (indices.size() > 0) {
                    next = new Pair<>(Collections.min(indices), Collections.max(indices));
                } else {
                    next = previous.get(g);
                }
                extremeAbsoluteX.put(g, next);
            }
            for (int j = 0; j < numRows; j++) {
                if (alignment[j].charAt(i) != '-') {
                    endIndex[j]++;
                }
            }

            if ((i + 1) % b == 0) {

                BoundPoly4 bp4 = new AlignmentOverlayNGTest.BoundPoly4(3);
                List<Double> xCoords = new ArrayList<>();
                for (int j = 0; j < numRows; j++) {
//                    System.out.println(Arrays.asList(startIndex[j], endIndex[j]));
                    System.out.println(Arrays.asList(startIndex[j] * 1. / nBasesPerRow[j], endIndex[j] * 1. / nBasesPerRow[j]));
                    xCoords.add(startIndex[j] * 1. / nBasesPerRow[j]);
                    bp4.relativeXCoords.get(j).getKey().setValue(startIndex[j] * 1. / nBasesPerRow[j]);
                    startIndex[j] = endIndex[j];

                }
                relativeXCoords.add(null)
            }

            boolean hasSome = true;
            while (hasSome) {
                hasSome = false;
                List<List<Integer>> popped = new ArrayList<List<Integer>>();
                Iterator<List<AlignmentWeaver.Pos>> it = ali.iterator();
                while (it.hasNext()) {
                    List<AlignmentWeaver.Pos> l = it.next();
                    List<AlignmentWeaver.Pos> p = new ArrayList<AlignmentWeaver.Pos>();
                    popped.add(p);
                    while (l.size() > 0 && l.get(0).order < I + inc) {
                        p.add(l.remove(0));
                    }
                    hasSome |= l.size() > 0;
                }
                I += inc;
                System.out.println(popped);
            }
        }

    }
