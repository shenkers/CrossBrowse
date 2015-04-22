/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.AbstractContext;

/**
 *
 * @author sol
 */
public class FastaContext extends AbstractContext {

    private static final Logger logger = LogManager.getLogger();

    private Semaphore readerSemaphore = new Semaphore(1);
    private IndexedFastaSequenceFile ifsf;
    private BooleanProperty complement = new SimpleBooleanProperty(false);
    ExecutorService ioService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    public FastaContext(String fastaFile, boolean force_index) throws FileNotFoundException, IOException {
        File fasta = new File(fastaFile);

        File fa_fai = new File(fastaFile.concat(".fai"));

        logger.info("{} already exists? {}", fa_fai.getAbsoluteFile(), fa_fai.exists());
        if (force_index || !fa_fai.exists()) {
            logger.info("creating fasta index");
            FastaTools.createIndex(fasta);
        }

        this.ifsf = new IndexedFastaSequenceFile(fasta);

    }

    public boolean acquireReader() {
        return readerSemaphore.tryAcquire();
    }

    public void releaseReader() {
        readerSemaphore.release();
    }

    /**
     * @return the complement
     */
    public BooleanProperty getComplement() {
        return complement;
    }

    /**
     * @param complement the complement to set
     */
    public void setComplement(boolean complement) {
        this.complement.setValue(complement);
    }

    public IndexedFastaSequenceFile getFasta() {
        return ifsf;
    }
}
