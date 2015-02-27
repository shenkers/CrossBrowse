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
public class RangeSetIntervalView extends Pane {

    Logger logger = LogManager.getLogger();
    
    RangeSet<Double> ranges = TreeRangeSet.create();
    int min, max;
    int length;

    public RangeSetIntervalView(int min, int max) {
        this.min = min;
        this.max = max;
        this.length = max - min + 1;
    }

//                    rs.add(Range.closed(.1, .2));
//                    rs.add(Range.closed(.2, .3));
//                    rs.add(Range.closed(.32, .35));
//                    rs.add(Range.closed(.6, .8));
//
//                    for (Range<Double> r : rs.asRanges()) {
//                        System.out.println(r.lowerEndpoint() + " - " + r.upperEndpoint());
//                    }
//                    for (Range<Double> interval : rs.asRanges()) {
//                        Rectangle r = new Rectangle();
//                        r.widthProperty().bind(p.widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint()));
//                        r.heightProperty().bind(p.heightProperty());
//                        r.xProperty().bind(p.widthProperty().multiply(interval.lowerEndpoint()));
//                        p.getChildren().add(r);
//                    }
    public void setData(RangeSet<Integer> intervals) {
       for (Range<Integer> interval : intervals.asRanges()) {
           Range<Double> r = Range.closed(
                    (interval.lowerEndpoint() - min + 0.) / length,
                    (interval.upperEndpoint() - min + 1.) / length);
           logger.info("range {}",r);
            ranges.add(r);
        }

        List<Node> content = new ArrayList<>();
        for (Range<Double> interval : ranges.subRangeSet(Range.closed(0., 1.)).asRanges()) {
            Rectangle rectangle = new Rectangle();
            
            rectangle.widthProperty().bind(widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint()));
            rectangle.heightProperty().bind(heightProperty());
            rectangle.xProperty().bind(widthProperty().multiply(interval.lowerEndpoint()));

            content.add(rectangle);
        }
        
        getChildren().setAll(content);
    }
    
    public void setData(List<Pair<Integer, Integer>> intervals) {
       
        for (Pair<Integer, Integer> interval : intervals) {
            ranges.add(Range.closed(
                    (interval.getKey() - min - 1.) / length,
                    (interval.getValue() - min + 0.) / length));
        }

        List<Node> content = new ArrayList<>();
        for (Range<Double> interval : ranges.subRangeSet(Range.closed(0., 1.)).asRanges()) {
            Rectangle rectangle = new Rectangle();
            
            rectangle.widthProperty().bind(widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint()));
            rectangle.heightProperty().bind(heightProperty());
            rectangle.xProperty().bind(widthProperty().multiply(interval.lowerEndpoint()));

            content.add(rectangle);
        }
        
        getChildren().setAll(content);
    }
}
