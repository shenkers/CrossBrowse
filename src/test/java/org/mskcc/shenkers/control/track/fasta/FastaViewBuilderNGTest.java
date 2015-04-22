/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import com.google.common.util.concurrent.MoreExecutors;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
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
public class FastaViewBuilderNGTest {

    public FastaViewBuilderNGTest() {
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
     * Test of getContent method, of class FastaViewBuilder.
     */
    @Test
    public void testGetContent() throws FileNotFoundException, InterruptedException {
        IndexedFastaSequenceFile ifsf = new IndexedFastaSequenceFile(new File("/mnt/LaiLab/sol/mel_yak_vir/genomes/M.fa"));

        new JFXPanel();
        String chr = "2L";
        int start = 16988333;
        int end = 16988949;

        ExecutorService s = Executors.newFixedThreadPool(2);
        ExecutorService s2 = Executors.newFixedThreadPool(2);
       CountDownLatch cdl = new CountDownLatch(20);

        Service<Object> ser = new Service<Object>() {

            @Override
            protected Task<Object> createTask() {
                return new Task<Object>() {

                    @Override
                    protected Object call() throws Exception {
                        byte[] sequence = ifsf.getSubsequenceAt(chr, start, end).getBases();
                        StringBuilder seqBuffer = new StringBuilder();

                        for (int i = 0; i < sequence.length; i++) {
                            seqBuffer.append((char) sequence[i]);
                        }

                        String seq = seqBuffer.toString();
                        System.out.println("1" + seq);
                        cdl.countDown();
                        return null;
                    }
                };
            }
        };
        Service<Object> ser2 = new Service<Object>() {

            @Override
            protected Task<Object> createTask() {
                return new Task<Object>() {

                    @Override
                    protected Object call() throws Exception {
                        byte[] sequence = ifsf.getSubsequenceAt(chr, start, end).getBases();
                        StringBuilder seqBuffer = new StringBuilder();

                        for (int i = 0; i < sequence.length; i++) {
                            seqBuffer.append((char) sequence[i]);
                        }

                        String seq = seqBuffer.toString();
                        System.out.println("2" + seq);
                        return null;
                    }
                };
            }
        };

        ser.setExecutor(s);
        ser2.setExecutor(s2);
        
        
        for(int i=0; i<10; i++){
        Platform.runLater(()->{
        
        ser.restart();
        ser2.restart();
        
        });
        Thread.sleep(1000);
        }
        
        cdl.await();
        
    }

}
