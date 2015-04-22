/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

/**
 *
 * @author sol
 */
public class SeqUnit extends StackPane {

    private static final Logger logger = LogManager.getLogger();

    Text txt = new Text();
    BooleanBinding textFits;
    Subscription sub;
    double inset = 1.5;
    Background bg = new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY), new BackgroundFill(Color.GRAY.darker(), CornerRadii.EMPTY, new Insets(inset)));

    public SeqUnit() {
        setBackground(bg);
        setMinHeight(20);
        setPrefHeight(20);
        setMinWidth(0);
        setSnapToPixel(false);
        
        txt.setFont(Font.font("Monospaced"));
        txt.setFill(Color.WHITE);
        
        textFits = Bindings.createBooleanBinding(() -> {
            return txt.boundsInLocalProperty().get().getWidth() <= prefWidthProperty().get()-(1.25*inset);
        }, txt.boundsInLocalProperty(), prefWidthProperty());

        sub = EasyBind.includeWhen(getChildren(), txt, textFits);

    }

    public void setText(String text) {
        txt.setText(text);
    }

    public void setColor(Color c) {
        setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY), new BackgroundFill(c.darker(), CornerRadii.EMPTY, new Insets(inset))));
    }
}
