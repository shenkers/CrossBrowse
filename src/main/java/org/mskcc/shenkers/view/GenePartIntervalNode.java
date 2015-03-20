/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.DomainFlippable;

/**
 *
 * @author sol
 */
public class GenePartIntervalNode<T extends Pane & DomainFlippable> extends Pane implements DomainFlippable {

    private static final Logger logger = LogManager.getLogger();
    T child;
    double rHeight;

    BooleanProperty flipDomain = new SimpleBooleanProperty(false);

    /**
     *
     * @param rHeight the relative fraction of the pane height the rectangle
     * occupies
     */
    public GenePartIntervalNode(double rHeight, T child) {
        this.rHeight = rHeight;
        this.child = child;
        child.flipDomainProperty().bind(flipDomain);
        getChildren().add(child);
        flipDomain.addListener((o, old, newVal) -> requestLayout());
//        setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(2), Insets.EMPTY)));
    }

    protected void layoutChildren() {
//        for (int i = 0; i < nodes.size(); i++) {
//            IntervalNode inode = nodes.get(i);
//            Pair<Integer, Integer> interval = inode.interval;
//            Pane n = inode.content;
//
//            double lower = (interval.getKey() - min + 0.) / length;
//            double upper = (interval.getValue() - min + 1.) / length;
//            double width = getWidth();
//            double height = getHeight();
        child.resizeRelocate(0., getHeight() * ((1 - rHeight) / 2), getWidth(), getHeight() * rHeight);

//            n.xProperty().bind(widthProperty().multiply(lower));
//            n.widthProperty().bind(widthProperty().multiply(upper-lower));
//            n.heightProperty().bind(heightProperty());
//
//            content.add(n);
//        }
    }

    @Override
    public BooleanProperty flipDomainProperty() {
        return flipDomain;
    }

}
