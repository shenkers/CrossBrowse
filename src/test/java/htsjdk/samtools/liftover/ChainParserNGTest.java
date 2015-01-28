/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.util.List;
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
    public void testGetChainIntersections() {
        File f = new File("C:/Users/Soma/Downloads/dm3.droYak2.all.chain.gz");
//        chrX:411,720-413,080
        Interval interval = null;
        ChainParser instance = new ChainParser(f);
        instance.getChainIntersections(interval)
//        List expResult = null;
//        List result = instance.getChainIntersections(interval);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
