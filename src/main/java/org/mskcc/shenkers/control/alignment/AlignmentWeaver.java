/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class AlignmentWeaver {

    Map<Genome, List<Integer>> order = new HashMap<>();
    Map<Genome, Boolean> negativeWithRespectToFounder = new HashMap<>();

    public AlignmentWeaver(Genome fromGenome, GenomeSpan fromInterval) {

        List<Integer> o = new ArrayList<>(fromInterval.length());
        for (int i = 0; i < fromInterval.length(); i++) {
            o.add(i);
        }

        order.put(fromGenome, o);
        negativeWithRespectToFounder.put(fromGenome, Boolean.FALSE);
    }

    static class Pos {

        // order in the alignment iteration
        int order;
        // genomic coord
        int loc;

        public Pos(int order, int loc) {
            this.order = order;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return "l=" + loc + "; o=" + order;
        }
    }

    static class Ali {

        // query to target mapping
        Map<Integer, Integer> m;

        public Ali(Map<Integer, Integer> m) {
            this.m = m;
        }
    }

    /**
     *
     * @return the alignment order for nucleotides in each genome
     */
    public Map<Genome, List<Integer>> getOrder() {
        return order;
    }

    public static void add(List<List<Pos>> al, List<Integer> newSeq, int prevSeq, Ali a) {
        // the sequence that we are aligning to
        List<Pos> prevPos = al.get(prevSeq);
        Map<Integer, Integer> newSeqOrder = new HashMap<Integer, Integer>();
        for (Pos p : prevPos) {
            if (a.m.containsKey(p.loc) && newSeq.contains(a.m.get(p.loc))) {
                newSeqOrder.put(a.m.get(p.loc), p.order);
            }
        }

        System.out.printf("newSeqOrder %s\n", newSeqOrder);

        Map<Integer, Integer> inc = new HashMap<Integer, Integer>();
        int i = 0;
        int pInc = 0;
        int nInc = 0;

        // figure out how many positions we have to shift over by
        for (Integer l : newSeq) {
            if (!newSeqOrder.containsKey(l)) {
                nInc++;
            } else {
                Integer n = newSeqOrder.get(l);
                while (i < n) {
                    inc.put(i, pInc);
                    i++;
                }
                inc.put(n, nInc);
                pInc = nInc;
            }
        }
        System.out.printf("inc %s\n", inc);

        // apply the shift
        for (List<Pos> l : al) {
            for (Pos p : l) {
                if (inc.containsKey(p.order)) {
                    p.order = p.order + inc.get(p.order);
                } else {
                    p.order = p.order + nInc;
                }
            }
        }
        System.out.printf("newPos %s\n", al);

        // figure out the order for aligned positions relative to the shifted positions
        Map<Integer, Integer> newSeqOrder2 = new HashMap<Integer, Integer>();
        for (Pos p : prevPos) {
            if (a.m.containsKey(p.loc) && newSeq.contains(a.m.get(p.loc))) {
                newSeqOrder2.put(a.m.get(p.loc), p.order);
            }
        }

        System.out.printf("newSeqOrder2 %s\n", newSeqOrder2);

        // flow the order back to unlabeled positions
        for (int j = newSeq.size() - 1; j > -1; j--) {
            if (!newSeqOrder2.containsKey(newSeq.get(j))) {
                if (j + 1 < newSeq.size() && newSeqOrder2.containsKey(newSeq.get(j + 1))) {
                    newSeqOrder2.put(newSeq.get(j), newSeqOrder2.get(newSeq.get(j + 1)) - 1);
                }
            }
        }

        System.out.printf("r-l filled newSeqOrder2 %s\n", newSeqOrder2);

        for (int j = 0; j < newSeq.size(); j++) {
            if (!newSeqOrder2.containsKey(newSeq.get(j))) {
                newSeqOrder2.put(newSeq.get(j), newSeqOrder2.get(newSeq.get(j - 1)) + 1);
            }
        }

        System.out.printf("l-r filled newSeqOrder2 %s\n", newSeqOrder2);

        // create a new sequence with the order
        List<Pos> newPos = new ArrayList<Pos>();
        for (Integer l : newSeq) {
            newPos.add(new Pos(newSeqOrder2.get(l), l));
        }
        al.add(newPos);
    }

    public void add(GenomeSpan toInterval, Genome fromGenome, Genome toGenome, boolean toNegativeStrand, NucleotideMapping fromGenomeToGenome) {
        // figure out if the toGenome is negative with respect to the founder
        Boolean fromNegativeWithRespectToFounder = negativeWithRespectToFounder.get(fromGenome);
        // if we are on the same strand as the fromGenome, we have the same orientation with respect ot the founder
        boolean oppositeToFounder = toNegativeStrand ? !fromNegativeWithRespectToFounder : fromNegativeWithRespectToFounder;
        negativeWithRespectToFounder.put(toGenome, oppositeToFounder);
        
        // the sequence that we are aligning to
        List<Integer> fromGenomeOrder = order.get(fromGenome);

        List<Optional<Integer>> newSeqOrder = Stream.iterate(0, j -> j + 1).limit(toInterval.length()).map(i -> {
            Optional<Integer> o = Optional.empty();
            return o;
        }).collect(Collectors.toList());

        for (int i = 0; i < fromGenomeOrder.size(); i++) {
            Integer fOrder = fromGenomeOrder.get(i);

            fromGenomeToGenome.fromRelativeOffset.get(i).ifPresent(relativeOffset -> {
                int o = relativeOffset;
                newSeqOrder.set(o, Optional.of(fOrder));
            });
        }

        logger.info("newSeqOrder {}\n", newSeqOrder);

        System.out.printf("newSeqOrder %s\n", newSeqOrder);

        List<Integer> orderShifts = new ArrayList<>();

        int nInc = 0;

        {
            if (oppositeToFounder) {
                int pInc = 0;
                for (int j = toInterval.length() - 1; j > -1; j--) {
                    Optional<Integer> nOrder = newSeqOrder.get(j);
                    if (nOrder.isPresent()) {
                        int oldOrder = nOrder.get();
                        while (orderShifts.size() < oldOrder) {
                            orderShifts.add(pInc);
                        }
                        orderShifts.add(nInc);
                        pInc = nInc;
                    } else {
                        nInc++;
                    }
                }
            } else {
                int pInc = 0;
                for (int j = 0; j < toInterval.length(); j++) {
                    Optional<Integer> nOrder = newSeqOrder.get(j);
                    if (nOrder.isPresent()) {
                        int oldOrder = nOrder.get();
                        while (orderShifts.size() < oldOrder) {
                            orderShifts.add(pInc);
                        }
                        orderShifts.add(nInc);
                        pInc = nInc;
                    } else {
                        nInc++;
                    }
                }
            }
            logger.info("inc4 {}", orderShifts);
        }

        // apply the shift
        for (Genome g : order.keySet()) {
            List<Integer> gOrder = order.get(g);
            for (int j = 0; j < gOrder.size(); j++) {
                int o = gOrder.get(j);
                if (o < orderShifts.size()) {
//                    logger.info("o {} inc {} inc2 {}", o, inc.get(o), inc2.get(j));
//                    assert inc.get(gOrder.get(j)).equals(inc4.get(o)) : "incs not equal";
//                    o = o + inc.get(gOrder.get(j));
                    gOrder.set(j, o + orderShifts.get(o));
                } else {
//                    o = o + nInc;
                    gOrder.set(j, o + nInc);
                }

            }
        }
        logger.info("newPos {}\n", order);

        List<Optional<Integer>> newSeqOrder2 = Stream.iterate(0, j -> j + 1).limit(toInterval.length()).map(i -> {
            Optional<Integer> o = Optional.empty();
            return o;
        }).collect(Collectors.toList());

        // figure out the order for aligned positions relative to the shifted positions
        for (int i = 0; i < fromGenomeOrder.size(); i++) {
            Integer fOrder = fromGenomeOrder.get(i);

            fromGenomeToGenome.fromRelativeOffset.get(i).ifPresent(relativeOffset -> {
                int o = relativeOffset;
                newSeqOrder2.set(o, Optional.of(fOrder));
            });
        }

        logger.info("newSeqOrder2 {}\n", newSeqOrder2);

        // flow the order back to unlabeled positions
        if (oppositeToFounder) {
            for (int j = toInterval.length() - 1; j > -1; j--) {
                Optional<Integer> o = newSeqOrder2.get(j);
                if (!o.isPresent()) {
                    if (j + 1 < toInterval.length() && newSeqOrder2.get(j + 1).isPresent()) {
                        newSeqOrder2.set(j, Optional.of(newSeqOrder2.get(j + 1).get() + 1));
                    }
                }
            }

            logger.info("r-l filled newSeqOrder2 {}\n", newSeqOrder2);

            for (int j = 0; j < toInterval.length(); j++) {
                Optional<Integer> o = newSeqOrder2.get(j);
                if (!o.isPresent()) {
                    newSeqOrder2.set(j, Optional.of(newSeqOrder2.get(j - 1).get() - 1));
                }
            }
        } else {
            for (int j = toInterval.length() - 1; j > -1; j--) {
                Optional<Integer> o = newSeqOrder2.get(j);
                if (!o.isPresent()) {
                    if (j + 1 < toInterval.length() && newSeqOrder2.get(j + 1).isPresent()) {
                        newSeqOrder2.set(j, Optional.of(newSeqOrder2.get(j + 1).get() - 1));
                    }
                }
            }

            logger.info("r-l filled newSeqOrder2 {}\n", newSeqOrder2);

            for (int j = 0; j < toInterval.length(); j++) {
                Optional<Integer> o = newSeqOrder2.get(j);
                if (!o.isPresent()) {
                    newSeqOrder2.set(j, Optional.of(newSeqOrder2.get(j - 1).get() + 1));
                }
            }

            System.out.printf("l-r filled newSeqOrder2 %s\n", newSeqOrder2);
        }
        /**/

//        Optional.
        List<Integer> collect = newSeqOrder2.stream().map(Optional::get).collect(Collectors.toList());
        order.put(toGenome, collect);
    }

    public static void printAli(List<List<Pos>> ali) {
        for (List<Pos> l : ali) {
            int pO = 0;
            for (Pos p : l) {
                System.out.print(StringUtils.repeat("\t", p.order - pO) + "" + p.loc);
                pO = p.order;
            }
            System.out.println();
        }
    }

    public void printAli2(List<Genome> gOrder) {
        Integer max = order.values().stream().flatMap(o -> o.stream()).max((i1, i2) -> i1 - i2).get();
        logger.info("MAX {}", max);
        for (Genome g : gOrder) {
//            int pO = 0;
            StringBuilder b = new StringBuilder(StringUtils.repeat("-", max + 1));
            List<Integer> l = order.get(g);
//            StringBuilder b = new StringBuilder();
            for (Integer porder : l) {
//                logger.info("PORDER {}",porder);
//                b.append(StringUtils.repeat("\t", porder - pO) + "X");
                b.setCharAt(porder, 'X');
//                pO = porder;
            }
            logger.info(b.toString());
        }
    }

    static Logger logger = LogManager.getLogger();

    public static void weave(Map<Pair<Genome, Genome>, LocalAlignment> alignments) {
        Set<Genome> incorporated = new HashSet<>();
        Map<Genome, Integer> rowOrder = new HashMap<>();
        Set<Pair<Genome, Genome>> remainder = new HashSet<>(alignments.keySet());

        while (!remainder.isEmpty()) {
            Iterator<Pair<Genome, Genome>> it = remainder.iterator();
            while (it.hasNext()) {
                Pair<Genome, Genome> p = it.next();
                if (incorporated.size() == 0 || incorporated.contains(p.getKey()) || incorporated.contains(p.getValue())) {
                    it.remove();
                    incorporated.add(p.getKey());
                    incorporated.add(p.getValue());

                    logger.info("incorporating pair {} {}", p.getKey().getId(), p.getValue().getId());
                }
            }
        }
    }
}
