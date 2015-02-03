/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.util;

/**
 *
 * @author sol
 */
public class IntervalTools {

    /**
     *
     * @param s
     * @param e
     * @param start
     * @param eend
     * @return whether (s,e) is nested within (start,end)
     */
    public static boolean isContained(int s, int e, int start, int end) {
        return s > start - 1 && e < end + 1 && s < e + 1;
    }

    /**
     *
     * @param s
     * @param e
     * @param start
     * @param eend
     * @return whether (s,e) is nested within (start,end)
     */
    public static boolean overlaps(int s, int e, int start, int end) {
        return (s > start - 1 && s < end + 1) || (e > start - 1 && e < end + 1) || (start > s - 1 && end < e + 1);
    }

    public static int[] intersection(int s1, int e1, int s2, int e2) {
        if (!overlaps(s1, e1, s2, e2)) {
            throw new RuntimeException(String.format("intervals (%d,%d) and (%d,%d) do not overlap", s1, e1, s2, e2));
        }
        return new int[]{Math.max(s1, s2), Math.min(e1, e2)};
    }
}
