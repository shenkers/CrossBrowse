/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import org.mskcc.shenkers.control.alignment.LocalAlignment;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang3.StringUtils;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Soma
 */
public class ChainParserNGTest {

    public ChainParserNGTest() {
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
     * Test of getChainIntersections method, of class ChainParser.
     */
    @Test
    public void testGetChainIntersections() throws FileNotFoundException {
        File f = new File("C:/Users/sol/Downloads/dm3.droYak2.chain");

//        Scanner scan = new Scanner(f);
//        int i=0;
//        int j=0;
//        PrintStream out = new PrintStream(new File("C:/Users/sol/Downloads/dm3.droYak2.chain"));
//        while(scan.hasNext()){
//            String line = scan.nextLine();
//            System.out.println(i+"\t"+line);
//            i++;
//            
//            if(i==185681)
//                break;
//            
//            out.println(line);
//            
//        }
//        out.close();
//        System.exit(0);
//        chrX:411720-413080
//        chr3R 27905053 28832112
//        chain 747933599 chr3R 27905053 + 46 27900188 chr3R 28832112 + 366441 28829329 2
//        chr3R:9536-9791
//        90	chain 970691389 chr3L 24543557 + 43412 23775837 chr3L 24197627 + 750 24183090 1
//chr3L:74548-74869
        Interval interval = new Interval("chr3L", 74548, 74869);

        System.out.println("loading chain file");
        ChainParser instance = new ChainParser(f);
        System.out.println("calculating intersections");
        List<LocalAlignment> chainIntersections = instance.getChainIntersections(interval);
        for (LocalAlignment blocks : chainIntersections) {
            StringBuilder gapped1 = new StringBuilder();
            StringBuilder gapped2 = new StringBuilder();

            gapped1.append(StringUtils.repeat("X", blocks.blockSizes.get(0)));
            gapped2.append(StringUtils.repeat("X", blocks.blockSizes.get(0)));
            int last1 = blocks.fromBlocks.get(0).getValue();
            int last2 = blocks.toNegativeStrand ? blocks.toBlocks.get(0).getKey() : blocks.toBlocks.get(0).getValue();
            for (int i = 1; i < blocks.fromBlocks.size(); i++) {
                System.out.println(String.format("%s:%d-%d", blocks.fromSequenceName, blocks.fromBlocks.get(i).getKey(), blocks.fromBlocks.get(i).getValue()));
                System.out.println(String.format("%s:%d-%d", blocks.toSequenceName, blocks.toBlocks.get(i).getKey(), blocks.toBlocks.get(i).getValue()));
                System.err.println("");

                gapped1.append(StringUtils.repeat("X", blocks.fromBlocks.get(i).getKey() - last1 - 1));
                gapped2.append(StringUtils.repeat("-", blocks.fromBlocks.get(i).getKey() - last1 - 1));
                int gap2 = blocks.toNegativeStrand ? last2 - blocks.toBlocks.get(i).getValue() - 1 : blocks.toBlocks.get(i).getKey() - last2 - 1;
                gapped2.append(StringUtils.repeat("X", gap2));
                gapped1.append(StringUtils.repeat("-", gap2));

                gapped1.append(StringUtils.repeat("X", blocks.blockSizes.get(i)));
                gapped2.append(StringUtils.repeat("X", blocks.blockSizes.get(i)));

                last1 = blocks.fromBlocks.get(i).getValue();
                last2 = blocks.toNegativeStrand ? blocks.toBlocks.get(i).getKey() : blocks.toBlocks.get(i).getValue();

            }
            System.out.println(gapped1.toString());
            System.out.println(gapped2.toString());
            System.out.println(gapped1.toString().replaceAll("-", "").length());
            System.out.println(gapped2.toString().replaceAll("-", "").length());
            System.out.println(74869 - 74548+1);
            System.out.println(42438 - 42085+1);
        }

//        chr3L:74548-74869
//chr3L:42085-42438
        System.out.println(chainIntersections.size());
        //        List expResult = null;
        //        List result = instance.getChainIntersections(interval);
        //        assertEquals(result, expResult);
        //        // TODO review the generated test code and remove the default call to fail.
        //        fail("The test case is a prototype.");
        ;
    }

}
