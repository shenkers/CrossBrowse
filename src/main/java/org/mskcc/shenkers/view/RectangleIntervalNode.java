/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.view;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class RectangleIntervalNode extends Pane{

    private static final Logger logger = LogManager.getLogger ();

    public RectangleIntervalNode() {
        setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(2), Insets.EMPTY)));
    }   
    
}
