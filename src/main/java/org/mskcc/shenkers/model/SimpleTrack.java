/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class SimpleTrack extends Pane implements ChangeListener<GenomeSpan> {

    Label l;

    public SimpleTrack() {
        super();
        l = new Label("start value");
//        l.prefHeightProperty().bind(this.heightProperty());
//        l.prefWidthProperty().bind(this.widthProperty());
        this.getChildren().add(l);
        
    }
    
    @Override
    public void changed(ObservableValue<? extends GenomeSpan> observable, GenomeSpan oldValue, GenomeSpan newValue) {
       l.setText(newValue.toString());
    }
    
}
