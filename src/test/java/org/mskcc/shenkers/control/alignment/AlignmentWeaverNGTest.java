/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import static org.mskcc.shenkers.control.alignment.AlignmentWeaver.add;
import static org.mskcc.shenkers.control.alignment.AlignmentWeaver.printAli;
import org.mskcc.shenkers.model.datatypes.Genome;
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
        alignments.put(new Pair<>(g1,g2), null);
        alignments.put(new Pair<>(g2,g3), null);
        alignments.put(new Pair<>(g3,g4), null);
        alignments.put(new Pair<>(g4,g5), null);
        AlignmentWeaver.weave(alignments);
    }
    
}
