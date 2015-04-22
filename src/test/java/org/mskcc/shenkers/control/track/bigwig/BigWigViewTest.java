/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bigwig;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import htsjdk.samtools.SAMFileReader;
import htsjdk.tribble.util.URLHelper;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.broad.igv.bbfile.*;
import static org.junit.Assert.*;

/**
 *
 * @author sol
 */
public class BigWigViewTest {

    public BigWigViewTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testZoom() throws IOException {
        BBFileReader bbfr = new BBFileReader("/home/sol/lailab/sol/mel_yak_vir/bigwig/M/H.plus.bw");

//        long totalNt = bbfr.getChromosomeRegions().stream().map(t -> (long) t.getEndBase()).filter(t -> { System.out.println("t "+t);return true;}).reduce(0l, (a, b) -> a + b);
        Long totalNt = Stream.iterate(0, i -> i + 1)
                .limit(bbfr.getChromosomeNameCount())
                .map(i -> bbfr.getChromosomeBounds(i, i))
                .map(t -> (long) t.getEndBase())
                .filter(t -> {
                    System.out.println("length " + t);
                    return true;
                })
                .reduce(0l, (a, b) -> a + b);

        System.out.println("total length " + totalNt);
        System.out.println("zl " + bbfr.getZoomLevelCount());
        Map<Integer, Integer> zlc = new HashMap<>();
        List<Double> featuresPerNucleotide = new ArrayList<>();
        // no zoom level selected
        featuresPerNucleotide.add(1.);
        for (int i = 1; i <= bbfr.getZoomLevelCount(); i++) {
            zlc.put(i, bbfr.getZoomLevelRecordCount(i));
            featuresPerNucleotide.add(bbfr.getZoomLevelRecordCount(i) * 1. / totalNt);
            System.out.println("records " + bbfr.getZoomLevelRecordCount(i));
            if (bbfr.getZoomLevelRecordCount(i) < 100) {
                ZoomLevelIterator zli = bbfr.getZoomLevelIterator(i);
                while (zli.hasNext()) {
                    ZoomDataRecord next = zli.next();
                    System.out.println("next: " + String.format("%s:%d-%d %.2f", next.getChromName(), next.getChromStart(), next.getChromEnd(), next.getSumData()));
                }

            }
        }

        System.out.println("features per nt " + featuresPerNucleotide.stream().map(s -> 1. / s).collect(Collectors.toList()));

        
                
//                RangeMap<Integer, Double> view = BigWigUtil.values(bbfr, chr, start, end, zoom);
    }
}
