/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.When;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.HiddenSidesPane;

/**
 *
 * @author sol
 */
public class VerticalHiddenScrollPane extends StackPane{

    private static final Logger logger = LogManager.getLogger();

    ScrollPane scrollWrapper = new ScrollPane();
    HiddenSidesPane hiddenSides = new HiddenSidesPane();
    ScrollBar scrollBar = new ScrollBar();
    
    ObservableBooleanValue sideShowable;
    DoubleBinding triggerDistance;

    public VerticalHiddenScrollPane() {
        super();
        
        hiddenSides.setContent(scrollWrapper);
        hiddenSides.setRight(scrollBar);
        
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setPadding(Insets.EMPTY);

        getChildren().setAll(hiddenSides);

        scrollBar.maxProperty().bind(scrollWrapper.vmaxProperty());
        scrollBar.minProperty().bind(scrollWrapper.vminProperty());

        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollWrapper.vvalueProperty().bindBidirectional(scrollBar.valueProperty());

    }

    /**
     * 
     * @param content
     * @return a pane that will grow to fit the size of its container. The 
     * provided nodes width will be bound to the width of this pane
     */
    public void build(Region content) {
        scrollWrapper.setContent(content);
        content.prefWidthProperty().bind(widthProperty());
        scrollBar.visibleAmountProperty().bind(scrollWrapper.heightProperty().divide(content.prefHeightProperty()));
        sideShowable = Bindings.createBooleanBinding(() -> scrollWrapper.heightProperty().get() < content.prefHeightProperty().get(), scrollWrapper.heightProperty(),content.prefHeightProperty());
        // only reveal the side if there is not enough vertical space to display the region
        triggerDistance = new When(sideShowable).then(16.).otherwise(0.);
        hiddenSides.triggerDistanceProperty().bind(triggerDistance);
    }

}
