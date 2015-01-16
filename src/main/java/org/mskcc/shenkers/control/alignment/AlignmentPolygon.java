/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Scale;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class AlignmentPolygon {

    Logger logger = LogManager.getLogger();

    // hold the genomic coordinates of nucleotides this shape represents for each genome
    List<GenomeSpan> span;

    // the regions visible for each genome in the viewer
    List<GenomeSpan> displayedSpans;

    // the amount of vertical space apportioned to each genome
    List<Double> weight;

    public AlignmentPolygon(List<GenomeSpan> span, List<GenomeSpan> displayedSpans, List<Double> weight) {

        this.span = span;
        this.displayedSpans = displayedSpans;
        this.weight = weight;
    }

    public Polygon getPolygon() {
        Stack<Double> ends = new Stack<>();
        Stack<Double> heights = new Stack<>();

        
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < span.size(); i++) {
            GenomeSpan s = span.get(i);
            GenomeSpan d = displayedSpans.get(i);
            double displayedSpanWidth = d.getWidth() + 0.;
            double relativeWidth = s.getWidth() / displayedSpanWidth;
            int distanceFromStart = d.isNegativeStrand() ? d.getEnd() - s.getEnd() : s.getStart() - d.getStart();
//            int distanceFromStart = d.isNegativeStrand() ? d.getEnd() : d.getStart();
            double relativeStart = (distanceFromStart) / displayedSpanWidth;
            double relativeHeight = weight.get(i);
            
            points.add(relativeStart);
            points.add(relativeHeight);

            ends.push(relativeStart + relativeWidth);
            heights.push(relativeHeight);
        }
        
        while(ends.size() > 0){
            points.add(ends.pop());
            points.add(heights.pop());
        }
        
        logger.info(points);
        
        return new Polygon(ArrayUtils.toPrimitive(points.toArray(new Double[0])));
    }

}
