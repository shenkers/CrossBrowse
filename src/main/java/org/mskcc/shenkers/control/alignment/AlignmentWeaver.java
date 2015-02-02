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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sol
 */
public class AlignmentWeaver {

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
            return "l=" + loc+ "; o=" + order;
        }
    }

    static class Ali {

        // query to target mapping

        Map<Integer, Integer> m;

        public Ali(Map<Integer, Integer> m) {
            this.m = m;
        }
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

        int maxOrder = 0;

        // apply the shift
        for (List<Pos> l : al) {
            for (Pos p : l) {
                if (inc.containsKey(p.order)) {
                    p.order = p.order + inc.get(p.order);
                } else {
                    p.order = p.order + nInc;
                }
                maxOrder = Math.max(maxOrder, p.order);
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
}
