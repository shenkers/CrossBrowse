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
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.HiddenSidesPane;

/**
 *
 * @author sol
 */
public class StackedIntervalView extends TilePane {

    List<TreeRangeSet<Integer>> rows = new ArrayList<>();
    int min, max;
    int length;
    int minSpace = 1;

    public StackedIntervalView(int min, int max) {
        rows.add(TreeRangeSet.create());
        this.min = min;
        this.max = max;
        this.length = max - min + 1;
        setSnapToPixel(false);
        setVgap(5);
        setOrientation(Orientation.VERTICAL);
    }

    public void setData(List<Pair<Integer, Integer>> intervals) {

        Collections.sort(intervals, new Comparator<Pair<Integer, Integer>>() {

            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        
//        Pair<Integer, Integer> r = intervals.get(0);
        for (Pair<Integer, Integer> interval : intervals) {
            Range<Integer> range = Range.closed(interval.getKey(), interval.getValue());
            int i = 0;
            added:
            {
                // add the interval to the first row that doesn't intersect with
                while (i < rows.size()) {
                    TreeRangeSet<Integer> set = rows.get(i);
                    RangeSet<Integer> intersection = set.subRangeSet(Range.closed(interval.getKey() - minSpace, interval.getValue() + minSpace));
                    if (intersection.isEmpty()) {
                        set.add(range);
                        break added;
                    }
                    i++;
                }
                TreeRangeSet<Integer> row = TreeRangeSet.create();
                row.add(range);
                rows.add(row);
            }
        }

        List<Node> content = new ArrayList<>();
        for (RangeSet<Integer> row : rows) {
            RangeSetIntervalView rowView = new RangeSetIntervalView(min, max);
            rowView.setData(row);
            content.add(rowView);
        }

        getChildren().setAll(content);
    }
}
