/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.interval;

import org.mskcc.shenkers.control.track.gene.*;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.mskcc.shenkers.control.track.AbstractContext;

/**
 *
 * @author sol
 */
public class IntervalContext extends AbstractContext{
    
    private Property<IntervalProvider> datasourceProperty;
    Semaphore readerSemaphore;

    public IntervalContext(IntervalProvider reader) {
        datasourceProperty = new SimpleObjectProperty<>(reader);
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
    public Property<IntervalProvider> readerProperty() {
        return datasourceProperty;
    }

    /**
     * close the old reader and load the new one
     * @param readerProperty the readerProperty to set
     */
    public void setReader(IntervalProvider reader) throws IOException {
        datasourceProperty.setValue(reader);
    }
    
    
    
}
