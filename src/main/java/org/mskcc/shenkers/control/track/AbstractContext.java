/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public abstract class AbstractContext {
    
    private Property<Optional<GenomeSpan>> span;

    public AbstractContext() {
        this.span = new SimpleObjectProperty<>(Optional.empty());
    }
    
    

    /**
     * @return the span
     */
    public Property<Optional<GenomeSpan>> spanProperty() {
        return span;
    }

    /**
     * @param span the span to set
     */
    public void setSpan(GenomeSpan span) {
        this.span.setValue(Optional.of(span));
    }
    
    
}
