/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.mskcc.shenkers.control.alignment.AlignmentWeaver.add;
import static org.mskcc.shenkers.control.alignment.AlignmentWeaver.printAli;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.util.IntervalTools;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class AlignmentWeaverNGTest {

    public AlignmentWeaverNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of main method, of class AlignmentWeaver.
     */
    @Test
    public void testMain() {
        List<AlignmentWeaver.Pos> l1 = new ArrayList<AlignmentWeaver.Pos>();
        for (int i = 0; i < 3; i++) {
            l1.add(new AlignmentWeaver.Pos(i, i));
        }
        List<List<AlignmentWeaver.Pos>> ali = new ArrayList<>();
        ali.add(l1);

        {
            List<Integer> newSeq = new ArrayList<Integer>();
            for (int i = 0; i < 2; i++) {
                newSeq.add(i);
            }
            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
            m.put(0, 0);
            m.put(2, 1);
            AlignmentWeaver.Ali a = new AlignmentWeaver.Ali(m);
            add(ali, newSeq, 0, a);
        }

        System.out.println(ali);
        printAli(ali);
        {
            List<Integer> newSeq = new ArrayList<Integer>();
            for (int i = 0; i < 3; i++) {
                newSeq.add(i);
            }
            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
            m.put(0, 0);
            m.put(1, 2);
            AlignmentWeaver.Ali a = new AlignmentWeaver.Ali(m);
            add(ali, newSeq, 1, a);
        }
        System.out.println(ali);
        printAli(ali);

        {
            List<Integer> newSeq = new ArrayList<Integer>();
            for (int i = 0; i < 3; i++) {
                newSeq.add(i);
            }
            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
            m.put(0, 2);

            AlignmentWeaver.Ali a = new AlignmentWeaver.Ali(m);
            add(ali, newSeq, 2, a);
        }
        System.out.println(ali);
        printAli(ali);

        {
            List<Integer> newSeq = new ArrayList<Integer>();
            for (int i = 0; i < 3; i++) {
                newSeq.add(i);
            }
            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
            m.put(2, 0);

            AlignmentWeaver.Ali a = new AlignmentWeaver.Ali(m);
            add(ali, newSeq, 3, a);
        }
        System.out.println(ali);
        printAli(ali);

        int I = 0;
        int inc = 3;

        boolean hasSome = true;
        while (hasSome) {
            hasSome = false;
            List<List<AlignmentWeaver.Pos>> popped = new ArrayList<List<AlignmentWeaver.Pos>>();
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

    static Logger logger = LogManager.getLogger();

    /**
     * Test of weave method, of class AlignmentWeaver.
     */
    @Test
    public void testWeave() {
        System.out.println("weave");
        Map<Pair<Genome, Genome>, LocalAlignment> alignments = new HashMap<>();
        Genome g1 = new Genome("mm10", "mouse");
        Genome g2 = new Genome("hg19", "human");
        Genome g3 = new Genome("dm3", "fly");
        Genome g4 = new Genome("dya", "yak");
        Genome g5 = new Genome("rnor", "rat");
        alignments.put(new Pair<>(g1, g2), null);
        alignments.put(new Pair<>(g2, g3), null);
        alignments.put(new Pair<>(g3, g4), null);
        alignments.put(new Pair<>(g4, g5), null);
        AlignmentWeaver.weave(alignments);
    }

    @Test
    public void testMapping() {
        logger.info("test mapping");
        List<Pair<Integer, Integer>> l1 = new ArrayList<>();
        List<Pair<Integer, Integer>> l2 = new ArrayList<>();
        List<Pair<Integer, Integer>> l3 = new ArrayList<>();
        List<Pair<Integer, Integer>> l4 = new ArrayList<>();
        List<Pair<Integer, Integer>> l5 = new ArrayList<>();

        l1.add(new Pair<>(10, 20));
        l1.add(new Pair<>(30, 36));

        l2.add(new Pair<>(13, 23));
        l2.add(new Pair<>(27, 33));

        l3.add(new Pair<>(13, 23));
        l3.add(new Pair<>(1, 7));

        l4.add(new Pair<>(20, 22));
        l5.add(new Pair<>(9, 11));

        LocalAlignment f = new LocalAlignment("a", "b", false, l1, l2);
        LocalAlignment r = new LocalAlignment("a", "c", true, l1, l3);
        LocalAlignment x = new LocalAlignment("a", "c", false, l4, l5);

        NucleotideMapping nmf = new NucleotideMapping(new GenomeSpan("a", 10, 36, false), new GenomeSpan("b", 13, 33, false));
        nmf.add(f);
        NucleotideMapping nmr = new NucleotideMapping(new GenomeSpan("a", 10, 36, false), new GenomeSpan("c", 1, 23, false));
        nmr.add(r);
        nmr.add(x);
    }

    @Test
    public void testAdd() {

        logger.info("testing ADD");

        Genome g1 = new Genome("mm10", "mouse");
        Genome g2 = new Genome("hg19", "human");
        Genome g3 = new Genome("dm3", "fly");
        Genome g4 = new Genome("dya", "yak");
        Genome g5 = new Genome("rnor", "rat");

        GenomeSpan i1 = new GenomeSpan("", 0, 2, false);
        GenomeSpan i2 = new GenomeSpan("", 0, 1, false);
        GenomeSpan i3 = new GenomeSpan("", 0, 2, false);
        GenomeSpan i4 = new GenomeSpan("", 0, 2, false);
        GenomeSpan i5 = new GenomeSpan("", 0, 2, false);

        AlignmentWeaver weaver = null;
        {

            weaver = new AlignmentWeaver(g1, i1);
            logger.info("w_order {}", weaver.order);
        }
        {
            GenomeSpan fromInterval = i1;
            GenomeSpan toInterval = i2;
            Genome fromGenome = g1;
            Genome toGenome = g2;
            NucleotideMapping mapping = new NucleotideMapping(fromInterval, toInterval);
            mapping.add(new LocalAlignment("", "", false,
                    Arrays.asList(
                            new Pair(0, 0),
                            new Pair(2, 2)),
                    Arrays.asList(
                            new Pair(0, 0),
                            new Pair(1, 1))));
            weaver.add(toInterval, fromGenome, toGenome, mapping);
        }
        logger.info("w_order {}", weaver.order);

        {
            GenomeSpan fromInterval = i2;
            GenomeSpan toInterval = i3;
            Genome fromGenome = g2;
            Genome toGenome = g3;
            NucleotideMapping mapping = new NucleotideMapping(fromInterval, toInterval);
            mapping.add(new LocalAlignment("", "", false,
                    Arrays.asList(
                            new Pair(0, 0),
                            new Pair(1, 1)),
                    Arrays.asList(
                            new Pair(0, 0),
                            new Pair(2, 2))));
            weaver.add(toInterval, fromGenome, toGenome, mapping);
        }
        logger.info("w_order {}", weaver.order);

        {
            GenomeSpan fromInterval = i3;
            GenomeSpan toInterval = i4;
            Genome fromGenome = g3;
            Genome toGenome = g4;
            NucleotideMapping mapping = new NucleotideMapping(fromInterval, toInterval);
            mapping.add(new LocalAlignment("", "", false,
                    Arrays.asList(
                            new Pair(0, 0)),
                    Arrays.asList(
                            new Pair(2, 2))));
            weaver.add(toInterval, fromGenome, toGenome, mapping);
        }
        logger.info("w_order {}", weaver.order);

        {
            GenomeSpan fromInterval = i4;
            GenomeSpan toInterval = i5;
            Genome fromGenome = g4;
            Genome toGenome = g5;
            NucleotideMapping mapping = new NucleotideMapping(fromInterval, toInterval);
            mapping.add(new LocalAlignment("", "", false,
                    Arrays.asList(
                            new Pair(2, 2)),
                    Arrays.asList(
                            new Pair(0, 0))));
            weaver.add(toInterval, fromGenome, toGenome, mapping);
        }
        logger.info("w_order {}", weaver.order);
        weaver.printAli2(Arrays.asList(g1,g2,g3,g4,g5));
    }
//        {
//            List<Integer> newSeq = new ArrayList<Integer>();
//            for (int i = 0; i < 3; i++) {
//                newSeq.add(i);
//            }
//            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
//            m.put(2, 0);
//
//            AlignmentWeaver.Ali a = new AlignmentWeaver.Ali(m);
//            add(ali, newSeq, 3, a);
//        }
}
