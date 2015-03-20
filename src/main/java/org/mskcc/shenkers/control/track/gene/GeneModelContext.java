/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.mskcc.shenkers.control.track.AbstractContext;

/**
 *
 * @author sol
 */
public class GeneModelContext extends AbstractContext{
    
    private Property<GeneModelProvider> readerProperty;
    Semaphore readerSemaphore;

    public GeneModelContext(GeneModelProvider reader) {
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
    public Property<GeneModelProvider> readerProperty() {
        return readerProperty;
    }

    /**
     * close the old reader and load the new one
     * @param readerProperty the readerProperty to set
     */
    public void setReader(GeneModelProvider reader) throws IOException {
        readerProperty.setValue(reader);
    }
    
    
    
}
