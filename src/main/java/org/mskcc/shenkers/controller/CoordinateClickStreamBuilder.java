/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.reactfx.EventSource;

/**
 *
 * @author sol
 */
public class CoordinateClickStreamBuilder {

    ObservableValue<Optional<GenomeSpan>> span;
    Region n;

    public CoordinateClickStreamBuilder(Region n, ObservableValue<Optional<GenomeSpan>> span) {

        this.n = n;
this.span = span;
    }

    /**
     * 
     * @return  an event stream of the coordinate clicked
     */
    public EventSource<Integer> build() {
        EventSource<Integer> evt = new EventSource<>();
        n.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            span.getValue().ifPresent(s -> {
                int width = s.getEnd() - s.getStart() + 1;
                double x = e.getX();
                boolean flipped = s.isNegativeStrand();
                int coord = (int) Math.floor(e.getX() / n.widthProperty().get() * width);
                evt.push(flipped ? s.getEnd() - coord : s.getStart()+coord);
            });

        });
        return evt;
    }
}
