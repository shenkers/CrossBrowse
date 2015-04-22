/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class Seq extends HBox {

    private static final Logger logger = LogManager.getLogger();

    IntegerProperty numElements = new SimpleIntegerProperty();
    DoubleBinding elemWidth;

    public Seq() {
        elemWidth = Bindings.createDoubleBinding(
                () -> {
                    return widthProperty().get() / numElements.get();
                }, numElements, widthProperty());
        setSnapToPixel(false);
    }

    public void setNumElements(int n) {
        numElements.set(n);
    }
}
