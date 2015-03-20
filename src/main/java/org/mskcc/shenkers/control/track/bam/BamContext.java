/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bam;

import htsjdk.samtools.SamReader;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.mskcc.shenkers.control.track.AbstractContext;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class BamContext extends AbstractContext{
    
    private Property<SamReader> readerProperty;
    Semaphore readerSemaphore;

    public BamContext(SamReader reader) {
        readerProperty = new SimpleObjectProperty<>(reader);
        readerSemaphore = new Semaphore(1);
    }
    
    public boolean acquireReader() throws InterruptedException{
        return readerSemaphore.tryAcquire();
    }
    
    public void releaseReader(){
        readerSemaphore.release();
    }

    /**
     * @return the readerProperty
     */
    public Property<SamReader> readerProperty() {
        return readerProperty;
    }

    /**
     * close the old reader and load the new one
     * @param readerProperty the readerProperty to set
     */
    public void setReader(SamReader reader) throws IOException {
        readerProperty.getValue().close();
        readerProperty.setValue(reader);
    }
    
    
    
}
