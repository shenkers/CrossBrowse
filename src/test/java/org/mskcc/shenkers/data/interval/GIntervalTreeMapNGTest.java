/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.data.interval;

import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.readers.PositionalBufferedStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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
public class GIntervalTreeMapNGTest {

    public GIntervalTreeMapNGTest() {
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

//    @Test
    public void testSomeMethod() {
        GIntervalTree git = new GIntervalTree();
        git.add("X", 1, 10);
        git.add("X", 3, 14);
        git.stream().forEach(x -> System.out.println("0 overlap: " + x));
        git.streamOverlaps("X", 0, 0).forEach(x -> System.out.println("a overlap: " + x));
        git.streamOverlaps("X", 1, 1).forEach(x -> System.out.println("b overlap: " + x));
        git.streamOverlaps("X", 1, 3).forEach(x -> System.out.println("c overlap: " + x));
        git.streamOverlaps("X", 3, 3).forEach(x -> System.out.println("d overlap: " + x));
        git.streamOverlaps("X", 10, 10).forEach(x -> System.out.println("e overlap: " + x));
        git.streamOverlaps("X", 11, 11).forEach(x -> System.out.println("f overlap: " + x));
        git.streamOverlaps("X", 15, 15).forEach(x -> System.out.println("g overlap: " + x));
        ChrStrand cs = new ChrStrand("X", Strand.NONE);
        System.out.println("eq? " + cs.equals(new ChrStrand("X", Strand.NONE)));
        System.out.println("empty : " + git.empty);
        git.remove("X", 1, 10, Strand.NONE);
        git.stream().forEach(x -> System.out.println("0 overlap: " + x));
    }

   
}
