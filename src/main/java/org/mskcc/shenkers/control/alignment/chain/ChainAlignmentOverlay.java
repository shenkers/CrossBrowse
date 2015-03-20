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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
        logger.info("querying from {} to {}", fromInterval, toInterval);
        Set<Boolean> toNegativeStrand = new HashSet<>();
        List<LocalAlignment> alignments = alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval);
        logger.info("{} chains overlapping query region", alignments.size());
        for (LocalAlignment localAlignment : alignments) {
            if (IntervalTools.overlaps(localAlignment.getFromStart(), localAlignment.getFromEnd(), fromInterval.getStart(), fromInterval.getEnd())
                    && IntervalTools.overlaps(localAlignment.getToStart(), localAlignment.getToEnd(), toInterval.getStart(), toInterval.getEnd())) {
                logger.info("before trim: {}-{}", localAlignment.getToStart(), localAlignment.getToEnd());
                logger.info("after trim: {}-{}", localAlignment.trim(fromInterval, toInterval).getToStart(), localAlignment.trim(fromInterval, toInterval).getToEnd());
                mappingFromGenomeToGenome.add(localAlignment.trim(fromInterval, toInterval));
                toNegativeStrand.add(localAlignment.getToNegativeStrand());
            } else {
                logger.info("alignment doesn't overlap queried regions");
                logger.info("from {}:{}-{}", fromInterval.getChr(), fromInterval.getStart(), fromInterval.getEnd());
                logger.info("to {}:{}-{}", toInterval.getChr(), toInterval.getStart(), toInterval.getEnd());
                logger.info("local alignment: from {}:{}-{}", localAlignment.getFromSequenceName(),localAlignment.getFromStart(), localAlignment.getFromEnd());
                logger.info("local alignment: to {}:{}-{}", localAlignment.getToSequenceName(),localAlignment.getToStart(), localAlignment.getToEnd());
            }
        }

        assert toNegativeStrand.size() == 1 : "Can't represent alignments go to plus and minus strand yet";

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
        logger.info("querying from {} to {}", fromInterval, toInterval);
        Set<Boolean> toNegativeStrand = new HashSet<>();
        List<LocalAlignment> alignments = alignmentSources.get(genomePair).getAlignment(fromInterval, toInterval);
        logger.info("{} chains overlapping query region", alignments.size());
         
        for (LocalAlignment localAlignment : alignments) {
            if (IntervalTools.overlaps(localAlignment.getFromStart(), localAlignment.getFromEnd(), fromInterval.getStart(), fromInterval.getEnd())
                    && IntervalTools.overlaps(localAlignment.getToStart(), localAlignment.getToEnd(), toInterval.getStart(), toInterval.getEnd())) {
                logger.info("before trim to {}: {}-{}", toInterval, localAlignment.getToStart(), localAlignment.getToEnd());
                logger.info("after trim: {}-{}", localAlignment.trim(fromInterval, toInterval).getToStart(), localAlignment.trim(fromInterval, toInterval).getToEnd());

                mappingFromGenomeToGenome.add(localAlignment.trim(fromInterval, toInterval));
                toNegativeStrand.add(localAlignment.getToNegativeStrand());
            } else {
                logger.info("alignment doesn't overlap queried regions");
                logger.info("from {}:{}-{}", fromInterval.getChr(), fromInterval.getStart(), fromInterval.getEnd());
                logger.info("to {}:{}-{}", toInterval.getChr(), toInterval.getStart(), toInterval.getEnd());
                logger.info("local alignment: from {}:{}-{}", localAlignment.getFromStart(), localAlignment.getFromEnd());
                logger.info("local alignment: to {}:{}-{}", localAlignment.getToStart(), localAlignment.getToEnd());
            }
        }
        assert toNegativeStrand.size() == 1 : "Can't represent alignments go to plus and minus strand yet";
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

        try {
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
                            logger.info("adding {} to the alignment", toAdd);
                            addAlignment(p, toAdd, displayedSpans, weaver);
                        }
                    }
                }
            }

            Map<Genome, List<Integer>> order = weaver.getOrder();

            logger.info("order keyset {}", order.keySet());
            for (Genome g : displayedSpans.keySet()) {
                logger.info("{} order {}", g, order.get(g));
            }

            Integer max = order.values().stream().flatMap(o -> o.stream()).max((x, y) -> x - y).get();

            Map<Genome, Integer> displayedSpanLength = displayedSpans.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().length()));

            List<Map<Genome, Pair<Double, Double>>> relativeCoordinates = new ArrayList<>();
//        Map<Genome, Pair<Double, Double>> previous = displayedSpans.keySet().stream().collect(Collectors.toMap(g -> g, g -> new Pair<Double, Double>(0., 0.)));
            {
                for (int i = 0; i <= max; i += basesPerColumn) {
                    Map<Genome, List<Integer>> collect = displayedSpans.keySet().stream().collect(Collectors.toMap(g -> g, g -> new ArrayList<>()));
                    boolean anyAlignedColumns = false;
                    for (int j = i; j < i + basesPerColumn; j++) {
                        boolean isAlignedColumn = true;
                        for (Genome g : displayedSpans.keySet()) {
                            List<Integer> gOrder = order.get(g);
                            isAlignedColumn &= gOrder.contains(j);
                        }
                        if (isAlignedColumn) {
                            anyAlignedColumns = true;
                            for (Genome g : displayedSpans.keySet()) {
                                List<Integer> gOrder = order.get(g);
                                int index = gOrder.indexOf(j);
                                if (index != -1) {
//                                    logger.info("genome {} contains index {}", g, j);
                                    List<Integer> indices = collect.get(g);
                                    indices.add(index);
                                }
                            }
                        }
                    }
                    if (anyAlignedColumns) {
                        Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
                        for (Genome g : displayedSpans.keySet()) {
                            int l = displayedSpanLength.get(g);
                            List<Integer> indices = collect.get(g);
                            Pair<Double, Double> next = new Pair<>((Collections.min(indices) + 0.) / l, (Collections.max(indices) + 1.) / l);
                            logger.info("genome {} adding pair {}", g, next);
                            logger.info("indices {} l {}", indices, l);
                            relativeX.put(g, next);
                        }
                        relativeCoordinates.add(relativeX);
                    }
                }
            }
            if (max % basesPerColumn != 0) {
                int remainingColumns = max % basesPerColumn;

                Map<Genome, List<Integer>> collect = displayedSpans.keySet().stream().collect(Collectors.toMap(g -> g, g -> new ArrayList<>()));
                boolean anyAlignedColumns = false;
                for (int j = max - remainingColumns; j <= max; j++) {
                    boolean isAlignedColumn = true;
                    for (Genome g : displayedSpans.keySet()) {
                        List<Integer> gOrder = order.get(g);
                        isAlignedColumn &= gOrder.contains(j);
                    }
                    if (isAlignedColumn) {
                        anyAlignedColumns = true;
                        for (Genome g : displayedSpans.keySet()) {
                            List<Integer> gOrder = order.get(g);
                            int index = gOrder.indexOf(j);
                            if (index != -1) {
//                                logger.info("genome {} contains index {}", g, j);
                                List<Integer> indices = collect.get(g);
                                indices.add(index);
                            }
                        }
                    }
                }
                if (anyAlignedColumns) {
                    Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
                    for (Genome g : displayedSpans.keySet()) {
                        int l = displayedSpanLength.get(g);
                        List<Integer> indices = collect.get(g);
                        Pair<Double, Double> next = new Pair<>((Collections.min(indices) + 0.) / l, (Collections.max(indices) + 1.) / l);
                        logger.info("genome {} adding pair {}", g, next);
                        logger.info("indices {} l {}", indices, l);
                        relativeX.put(g, next);
                    }
                    relativeCoordinates.add(relativeX);
                }
            }

//        for (int i = 0; i <= max; i += basesPerColumn) {
//            Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
//
//            Set<Integer> commonCoordinates = Stream.iterate(i, j -> j + 1).limit(i + basesPerColumn).collect(Collectors.toSet());
//
//            for (Genome g : displayedSpans.keySet()) {
//                List<Integer> gOrder = order.get(g);
//                int l = displayedSpanLength.get(g);
//
//                List<Integer> indices = new ArrayList<>();
//                List<Integer> toRetain = new ArrayList<>();
//                for (int j = i; j < i + basesPerColumn; j++) {
//                    int index = gOrder.indexOf(j);
//                    if (index != -1) {
//                        logger.info("genome {} contains index {}", g, j);
//                        indices.add(index);
//                        toRetain.add(j);
//                    }
//                }
//                commonCoordinates.retainAll(toRetain);
//
//                if (indices.size() > 0) {
//                    Pair<Double, Double> next = null;
//                    next = new Pair<>((Collections.min(indices) + 0.) / l, (Collections.max(indices) + 1.) / l);
//                    logger.info("genome {} adding pair {}", g, next);
//                    logger.info("indices {} l {}", indices, l);
//                    relativeX.put(g, next);
//                }
////                else {
////                   next = new Pair<>(previous.get(g).getValue(),previous.get(g).getValue());
////                }
//
//            }
//
//            if (!commonCoordinates.isEmpty()) {
//                relativeCoordinates.add(relativeX);
//            }
////            previous = relativeX;
//
//        }
//        if (max % basesPerColumn != 0) {
//            int remainingColumns = max % basesPerColumn;
//
//            Map<Genome, Pair<Double, Double>> relativeX = new HashMap<>();
//            Set<Integer> commonCoordinates = Stream.iterate(max - remainingColumns, j -> j + 1).limit(max).collect(Collectors.toSet());
//
//            for (Genome g : displayedSpans.keySet()) {
//                List<Integer> gOrder = order.get(g);
//                int l = displayedSpanLength.get(g);
//
//                List<Integer> indices = new ArrayList<>();
//                List<Integer> toRetain = new ArrayList<>();
//                for (int j = max - remainingColumns; j <= max; j++) {
//                    int index = gOrder.indexOf(j);
//                    if (index != -1) {
//                        indices.add(index);
//                        toRetain.add(j);
//                    }
//                }
//                commonCoordinates.retainAll(toRetain);
//
//                if (indices.size() > 0) {
//                    Pair<Double, Double> next = null;
//                    next = new Pair<>((Collections.min(indices) + 0.) / l, (Collections.max(indices) + 1.) / l);
//                    relativeX.put(g, next);
//
//                }
////                else {
////                    next = new Pair<>(previous.get(g).getValue(),previous.get(g).getValue());
////                }
//
//            }
//            if (!commonCoordinates.isEmpty()) {
//                relativeCoordinates.add(relativeX);
//            }
//        }
            return relativeCoordinates;
        } catch (Exception e) {
            logger.error("error constructing alignment from specified regions", e);
        }
        return new ArrayList<>();
    }

    public List<Node> getOverlayPaths(List<Genome> gOrder, Map<Genome, GenomeSpan> displayedSpans, Map<Genome, ObservableValue<? extends Boolean>> genomeFlipped, int basesPerColumn, List<DoubleProperty> dividerPositionProperties, ReadOnlyDoubleProperty widthProperty, ObservableDoubleValue heightProperty) {

        List<Map<Genome, Pair<Double, Double>>> alignmentColumnsRelativeX = getAlignmentColumnsRelativeX(displayedSpans, basesPerColumn);

        List<ObservableValue<? extends Boolean>> flips = gOrder.stream().map(g -> genomeFlipped.get(g)).collect(Collectors.toList());

        List<Node> paths = new ArrayList<>();

        for (Map<Genome, Pair<Double, Double>> column : alignmentColumnsRelativeX) {
            logger.info("{}", column);

            boolean allRowsAligned = true;
            for (int i = 0; i < gOrder.size(); i++) {
                Genome g = gOrder.get(i);
                Pair<Double, Double> calculatedRelativeX = column.get(g);
                allRowsAligned &= calculatedRelativeX.getValue() - calculatedRelativeX.getKey() > 0;
            }

            if (!allRowsAligned) {
                continue;
            }

            CurvedOverlayPath cop = new CurvedOverlayPath(gOrder.size());
            for (int i = 0; i < gOrder.size(); i++) {
                cop.getGenomeFlipped().get(i).bind(flips.get(i));
            }

            paths.add(cop.getPath());

            cop.getXScale().bind(widthProperty);
            cop.getYScale().bind(heightProperty);

            cop.getPath().setFill(new Color(Math.random(), Math.random(), Math.random(), .1));
//            cop.getPath().setStroke(new Color(0, 0, 0, 0));
//            cop.getPath().setStrokeWidth(1.);
            for (int i = 0; i < gOrder.size(); i++) {
                Genome g = gOrder.get(i);
                Pair<Double, Double> calculatedRelativeX = column.get(g);
                logger.info("adding column {} {}", g, calculatedRelativeX);
                Pair<DoubleProperty, DoubleProperty> relativeX = cop.getRelativeXCoords().get(i);
                relativeX.getKey().setValue(calculatedRelativeX.getKey());
                relativeX.getValue().setValue(calculatedRelativeX.getValue());
                Pair<DoubleProperty, DoubleProperty> relativeY = cop.getRelativeYCoords().get(i);

                if (i == 0) {
                    logger.info("case 1 : {} i={}", g, i);
                    relativeY.getKey().setValue(0);
                    relativeY.getValue().bind(dividerPositionProperties.get(0));
                } else if (i == gOrder.size() - 1) {
                    logger.info("case 2 : {} i={}", g, i);
                    relativeY.getKey().bind(dividerPositionProperties.get(dividerPositionProperties.size() - 1));
                    relativeY.getValue().setValue(1);
                } else {
                    logger.info("case 3 : {} i={}", g, i);
                    relativeY.getKey().bind(dividerPositionProperties.get((i * 2) - 1));
                    relativeY.getValue().bind(dividerPositionProperties.get(i * 2));
                }
            }

        }

        return paths;
    }
}
