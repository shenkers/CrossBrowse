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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public static LocalAlignment trim(LocalAlignment blocks, Interval query_i, Interval target_i) {

        List<Pair<Integer, Integer>> fromBlocks = new ArrayList<>();
        List<Pair<Integer, Integer>> toBlocks = new ArrayList<>();

        for (int i = 0; i < blocks.fromBlocks.size(); i++) {

            Pair<Integer, Integer> fromBlock = blocks.fromBlocks.get(i);
            Pair<Integer, Integer> toBlock = blocks.toBlocks.get(i);

            assert IntervalTools.isContained(fromBlock.getKey(), fromBlock.getValue(), query_i.getStart(), query_i.getEnd()) : "it is assumed that all blocks in query will be contained in the query interval";

            // if this block overlaps it is either OK as is, or needs to be trimmed
            if (IntervalTools.overlaps(toBlock.getKey(), toBlock.getValue(), target_i.getStart(), target_i.getEnd())) {
                if (IntervalTools.isContained(toBlock.getKey(), toBlock.getValue(), target_i.getStart(), target_i.getEnd())) {
                    fromBlocks.add(fromBlock);
                    toBlocks.add(toBlock);
                } else {
                    int offsetTargetStart = toBlock.getKey() < target_i.getStart() ? target_i.getStart() - toBlock.getKey() : 0;
                    int offsetTargetEnd = toBlock.getValue() > target_i.getEnd() ? toBlock.getKey() - target_i.getEnd() : 0;

                    Pair<Integer, Integer> offsetToBlock = new Pair<>(toBlock.getKey() + offsetTargetStart, toBlock.getValue() - offsetTargetEnd);
                    Pair<Integer, Integer> offsetFromBlock
                            = blocks.toNegativeStrand
                                    ? new Pair<>(fromBlock.getKey() + offsetTargetEnd, fromBlock.getValue() - offsetTargetStart)
                                    : new Pair<>(fromBlock.getKey() + offsetTargetStart, fromBlock.getValue() - offsetTargetEnd);

                    fromBlocks.add(offsetFromBlock);
                    toBlocks.add(offsetToBlock);
                }
            }
        }

        return new LocalAlignment(blocks.fromSequenceName, blocks.toSequenceName, blocks.toNegativeStrand, fromBlocks, toBlocks);
    }

    /**
     * Test of getChainIntersections method, of class ChainParser.
     */
    @Test
    public void testGetChainIntersections() throws FileNotFoundException {
//        File f = new File("/home/sol/Downloads/dm3.droYak2.chain");
        File f = new File("/home/sol/Downloads/dm3.droYak2.chain");

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
//            chr3L:74548-74869
//chr3L:42085-42438
            blocks = trim(blocks, new Interval("chr3L", 74548, 74869), new Interval("chr3L", 42088, 42438));
            StringBuilder gapped1 = new StringBuilder();
            StringBuilder gapped2 = new StringBuilder();

            gapped1.append(StringUtils.repeat("X", blocks.blockSizes.get(0)));
            gapped2.append(StringUtils.repeat("X", blocks.blockSizes.get(0)));
            int last1 = blocks.fromBlocks.get(0).getValue();
            int last2 = blocks.toNegativeStrand ? blocks.toBlocks.get(0).getKey() : blocks.toBlocks.get(0).getValue();
            for (int i = 1; i < blocks.fromBlocks.size(); i++) {
                System.out.println(String.format("%s:%d-%d %d", blocks.fromSequenceName, blocks.fromBlocks.get(i).getKey(), blocks.fromBlocks.get(i).getValue(), blocks.fromBlocks.get(i).getValue() - blocks.fromBlocks.get(i).getKey()));
                System.out.println(String.format("%s:%d-%d %d", blocks.toSequenceName, blocks.toBlocks.get(i).getKey(), blocks.toBlocks.get(i).getValue(), blocks.toBlocks.get(i).getValue() - blocks.toBlocks.get(i).getKey()));
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
            System.out.println(74869 - 74548 + 1);
            System.out.println(42438 - 42085 + 1);
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

    Logger logger = LogManager.getLogger();

    @Test
    public void testChainWeaving() throws FileNotFoundException {
//        File f = new File("/home/sol/Downloads/dm3.droYak2.chain");
        logger.info("test: chain weaving");

        Genome g1 = new Genome("dm3","mel");
        Genome g2 = new Genome("yak","yak");
        Genome g3 = new Genome("vir","vir");
        
        Interval melInterval = new Interval("3L", 74548, 74869);
        Interval yakInterval = new Interval("3L", 42088, 42438);
        Interval virInterval = new Interval("scaffold_13049",2917506,2917933);

        AlignmentWeaver weaver = new AlignmentWeaver(g1, melInterval, false);
        {
            File f = new File("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droYak3.net.chain");

            System.out.println("loading chain file");
            ChainParser instance = new ChainParser(f);
            System.out.println("calculating intersections");
            List<LocalAlignment> chainIntersections = instance.getChainIntersections(melInterval);
            
            NucleotideMapping mapping = new NucleotideMapping(melInterval, yakInterval);
            for (LocalAlignment a : chainIntersections) {
                logger.info("{}:{}-{}", a.fromSequenceName, a.fromStart, a.fromEnd);
                logger.info("{}:{}-{}", a.toSequenceName, a.toStart, a.toEnd);
                LocalAlignment trimmed = trim(a, melInterval, yakInterval);
                mapping.add(trimmed);
            }
            weaver.add(yakInterval, g1, g2, mapping);
        }

        {
            File f = new File("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droVir3.net.chain");

            System.out.println("loading chain file");
            ChainParser instance = new ChainParser(f);
            System.out.println("calculating intersections");
            List<LocalAlignment> chainIntersections = instance.getChainIntersections(melInterval);
            logger.info("vir intersections size {}", chainIntersections.size());
            NucleotideMapping mapping = new NucleotideMapping(melInterval, virInterval);
            for (LocalAlignment a : chainIntersections) {
                logger.info("{}:{}-{}", a.toSequenceName, a.toStart, a.toEnd);
                LocalAlignment trimmed = trim(a, melInterval, virInterval);
                mapping.add(trimmed);
            }
            weaver.add(virInterval, g1, g3, mapping);
        }
        
        weaver.printAli2(Arrays.asList(g1,g2,g3));
    }

}
