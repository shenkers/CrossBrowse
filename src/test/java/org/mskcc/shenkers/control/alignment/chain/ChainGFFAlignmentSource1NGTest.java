/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.tabix.ChainContext;
import htsjdk.samtools.tabix.GFFChainContextCodec;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.readers.LineIterator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import org.mskcc.shenkers.control.alignment.NucleotideMapping;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
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
public class ChainGFFAlignmentSource1NGTest {

    final static Logger logger = LogManager.getLogger();

    public ChainGFFAlignmentSource1NGTest() {
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
     * Test of getAlignment method, of class ChainGFFAlignmentSource1.
     */
    @Test
    public void testGetAlignment() throws IOException {
        System.out.println("getAlignment");
        BlockCompressedInputStream bcis = new BlockCompressedInputStream(new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff.bgz"));
        while (true) {

            ChainContext next = new GFFChainContextCodec().decode(bcis.readLine());
            if (next.getToNegativeStrand()) {
                continue;
            }
             logger.info("q {}:{}-{}", next.getChr(), next.getStart(), next.getEnd());
            logger.info("t {}:{}-{}", next.getTargetChr(), next.getTargetStart(), next.getTargetEnd());
            logger.info("");
            LocalAlignment alignedBlocks = ChainGFFAlignmentSource1.getAlignedBlocks(next, new GenomeSpan(next.getChr(), next.getStart(), next.getEnd(), false)).get();
           
            for (int i = 0; i < alignedBlocks.getNBlocks(); i++) {
//            System.out.println(String.format("%s %s", alignedBlocks.getFromBlock(i), alignedBlocks.getToBlock(i)));
                Pair<Integer, Integer> fromBlock = alignedBlocks.getFromBlock(i);
                Pair<Integer, Integer> toBlock = alignedBlocks.getToBlock(i);
                logger.info("Q {} {}:{}-{}", alignedBlocks.getBlockSize(i), next.getChr(), fromBlock.getKey(), fromBlock.getValue());
                logger.info("T {} {}:{}-{}", alignedBlocks.getBlockSize(i), next.getTargetChr(), toBlock.getKey(), toBlock.getValue());
                logger.info("");

            }
            if (!next.getToNegativeStrand()) {
                break;
            }
        }
//        System.out.println(alignedBlocks);

    }

//    @Test
    public void testWeirdOverlay() throws IOException {
        ChainGFFAlignmentSource1 cgas = new ChainGFFAlignmentSource1("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain", false);
        GenomeSpan from = new GenomeSpan("chrX", 145992655, 145992801, false); 
        GenomeSpan to = new GenomeSpan("chrX", 66776526, 66776896, false);
        logger.info("from size {}", from.getEnd()-from.getStart()+1);
            logger.info("to size {}", to.getEnd()-to.getStart()+1);
        List<LocalAlignment> alignment = cgas.getAlignment(from,to);
        NucleotideMapping nm = new NucleotideMapping(from, to);
        for (LocalAlignment l : alignment) {
            nm.add(l);
            logger.info("from {} {} block size {}", l.getFromStart(), l.getFromEnd(), l.getFromEnd() - l.getFromStart() + 1);
            logger.info("to {} {} block size {}", l.getToStart(), l.getToEnd(), l.getToEnd() - l.getToStart() + 1);
            for (int i = 0; i < l.getNBlocks(); i++) {
                l.getBlockSize(i);
                
                logger.info("from offset {}",l.getFromBlock(i).getKey()-from.getStart());
                logger.info("to offset {}",l.getToBlock(i).getKey()-to.getStart());

                if (i > 0) {
                    logger.info("gap from {}", l.getFromBlock(i).getKey() - l.getFromBlock(i - 1).getValue() - 1);
                    logger.info("gap to {}", l.getToBlock(i).getKey() - l.getToBlock(i - 1).getValue() - 1);
                }
                
                logger.info("block size {}", l.getBlockSize(i));
                
            }
        }
    }

}
