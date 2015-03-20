/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.DomainFlippable;

/**
 *
 * @author sol
 */
public class GenericIntervalView<T extends Pane & DomainFlippable> extends Pane implements DomainFlippable {

    Logger logger = LogManager.getLogger();

    int min, max;
    int length;

    BooleanProperty flipDomain = new SimpleBooleanProperty(false);

    List<IntervalNode<T>> nodes;

    public GenericIntervalView(int min, int max) {
        this.min = min;
        this.max = max;
        this.length = max - min + 1;
    }

    public BooleanProperty flipDomainProperty() {
        return flipDomain;
    }

    private void setData(List<IntervalNode<T>> nodes) {
        this.nodes = nodes;

        nodes.stream().forEach(n -> n.content.flipDomainProperty().bind(flipDomain));
        getChildren().setAll(nodes.stream().map(n -> n.content).collect(Collectors.toList()));
    }

    public void setData(List<Pair<Integer, Integer>> intervals, List<T> content) {
        List<IntervalNode<T>> nodes = Stream.iterate(0, i -> i + 1).limit(intervals.size()).map(i -> new IntervalNode<T>(intervals.get(i), content.get(i))).collect(Collectors.toList());
        setData(nodes);
    }

    @Override
    protected void layoutChildren() {
        
        boolean flip = flipDomain.get();
        for (int i = 0; i < nodes.size(); i++) {
            IntervalNode inode = nodes.get(i);
            Pair<Integer, Integer> interval = inode.interval;
            Pane n = inode.content;

            if (flip) {
                double upper = 1-((interval.getKey() - min + 0.) / length);
                double lower = 1-((interval.getValue() - min + 1.) / length);
                
                double width = getWidth();
                double height = getHeight();
                n.resizeRelocate(widthProperty().get() * lower, 0, width * (upper - lower), height);
            } else {
                double lower = (interval.getKey() - min + 0.) / length;
                double upper = (interval.getValue() - min + 1.) / length;
                double width = getWidth();
                double height = getHeight();
                n.resizeRelocate(widthProperty().get() * lower, 0, width * (upper - lower), height);
            }
        }
    }

}
