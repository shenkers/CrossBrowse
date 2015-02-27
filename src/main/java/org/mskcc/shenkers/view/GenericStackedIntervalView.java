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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.HiddenSidesPane;

/**
 *
 * @author sol
 */
public class GenericStackedIntervalView extends TilePane {

    List<TreeRangeSet<Integer>> rows = new ArrayList<>();
    List<List<IntervalNode>> rowNodes = new ArrayList<>();
    int min, max;
    int length;
    int minSpace = 1;

    public GenericStackedIntervalView(int min, int max) {
        this.min = min;
        this.max = max;
        this.length = max - min + 1;
        setSnapToPixel(false);
        setVgap(5);
        setOrientation(Orientation.VERTICAL);
    }
    
    
    public void setData(List<Pair<Integer, Integer>> intervals, List<? extends Pane> content) {
        rows.clear();
        rowNodes.clear();
        
        rows.add(TreeRangeSet.create());
        rowNodes.add(new ArrayList<>());
        
        List<IntervalNode> nodes = Stream.iterate(0, i->i+1).limit(intervals.size()).map(i -> new IntervalNode(intervals.get(i),content.get(i))).collect(Collectors.toList());

        Collections.sort(nodes, new Comparator<IntervalNode>() {

            @Override
            public int compare(IntervalNode o1, IntervalNode o2) {
                return o1.interval.getKey() - o2.interval.getKey();
            }
        });
        
        for (IntervalNode inode : nodes) {
            Pair<Integer,Integer> interval = inode.interval;
            Range<Integer> range = Range.closed(interval.getKey(), interval.getValue());
            int i = 0;
            added:
            {
                // add the interval to the first row that doesn't intersect with
                while (i < rows.size()) {
                    TreeRangeSet<Integer> set = rows.get(i);
                    List<IntervalNode> rowContent = rowNodes.get(i);
                    RangeSet<Integer> intersection = set.subRangeSet(Range.closed(interval.getKey() - minSpace, interval.getValue() + minSpace));
                    if (intersection.isEmpty()) {
                        set.add(range);
                        rowContent.add(inode);
                        break added;
                    }
                    i++;
                }
                
                TreeRangeSet<Integer> row = TreeRangeSet.create();
                row.add(range);
                rows.add(row);
                
                List<IntervalNode> rowContent = new ArrayList<>();
                rowContent.add(inode);
                rowNodes.add(rowContent);
            }
        }
        
        List<Node> children = new ArrayList<>();
        for(List<IntervalNode> row : rowNodes){
            GenericIntervalView rowView = new GenericIntervalView(min, max);
//            List<Pair<Integer,Integer>> rowIntervals = new ArrayList<>(row.size());
//            List<Pane> rowContent = new ArrayList<>(row.size());
//            row.stream().forEach(i -> {rowIntervals.add(i.interval); rowContent.add(i.content);});
            rowView.setData(row);
            children.add(rowView);
        }

        getChildren().setAll(children);
    }
}
