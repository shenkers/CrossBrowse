/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bigwig;

import org.mskcc.shenkers.control.track.bam.*;
import htsjdk.samtools.SamReader;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.broad.igv.bbfile.BBFileReader;
import org.mskcc.shenkers.control.track.AbstractContext;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class BigWigContext extends AbstractContext{
    
    private Property<BBFileReader> readerProperty;
    Semaphore readerSemaphore;

    public BigWigContext(BBFileReader reader) {
        readerProperty = new SimpleObjectProperty<>(reader);
        readerSemaphore = new Semaphore(1);
    }
    
    public void acquireReader() throws InterruptedException{
        readerSemaphore.acquire();
    }
    
    public void releaseReader(){
        readerSemaphore.release();
    }

    /**
     * @return the readerProperty
     */
    public Property<BBFileReader> readerProperty() {
        return readerProperty;
    }

    /**
     * close the old reader and load the new one
     * @param readerProperty the readerProperty to set
     */
    public void setReader(BBFileReader reader) throws IOException {
        readerProperty.setValue(reader);
    }
    
    
    
}
