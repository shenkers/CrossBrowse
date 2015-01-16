/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 *
 * @author sol
 */
public class StretchStringPane extends StackPane{

    Text lbl;
    
    public StretchStringPane(String text) {
        super();
        
        lbl = new Text(text);
        lbl.setFont(Font.font("Monospaced"));
        
        getChildren().add(lbl);
        
//        lbl.setBoundsType(TextBoundsType.VISUAL);
        
        DoubleBinding widthBinding = widthProperty().divide(lbl.getBoundsInLocal().getWidth());
        DoubleBinding heightBinding = heightProperty().divide(lbl.getBoundsInLocal().getHeight());
        NumberBinding scale = Bindings.min(widthBinding, heightBinding);
        lbl.scaleXProperty().bind(scale);
        lbl.scaleYProperty().bind(scale);

    }
    
    
}
