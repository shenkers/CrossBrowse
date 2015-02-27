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
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class GenericIntervalView extends Pane {

    Logger logger = LogManager.getLogger();

    int min, max;
    int length;
    
    List<IntervalNode> nodes;

    public GenericIntervalView(int min, int max) {
        this.min = min;
        this.max = max;
        this.length = max - min + 1;
    }

    public void setData(List<IntervalNode> nodes) {
        this.nodes = nodes;
        getChildren().setAll(nodes.stream().map(n->n.content).collect(Collectors.toList()));
    }

    @Override
    protected void layoutChildren() {         
        for (int i = 0; i < nodes.size(); i++) {
            IntervalNode inode = nodes.get(i);
            Pair<Integer, Integer> interval = inode.interval;
            Pane n = inode.content;

            double lower = (interval.getKey() - min + 0.) / length;
            double upper = (interval.getValue() - min + 1.) / length;
            double width = getWidth();
            double height = getHeight();
            n.resizeRelocate(widthProperty().get()*lower, 0, width*(upper-lower), height);
            
//            n.xProperty().bind(widthProperty().multiply(lower));
//            n.widthProperty().bind(widthProperty().multiply(upper-lower));
//            n.heightProperty().bind(heightProperty());
//
//            content.add(n);
        }
    }
    
    
    
}
