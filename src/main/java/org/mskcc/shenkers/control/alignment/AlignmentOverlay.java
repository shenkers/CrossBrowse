/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Callback;
import org.apache.commons.lang3.ArrayUtils;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

/**
 *
 * @author sol
 */
public class AlignmentOverlay extends Pane {

    BooleanProperty flipProperty;
    List<Node> nodes;

    Node poly;

    Scale s;

    public AlignmentOverlay() {
        super();
//        s = new Scale();
//        s.xProperty().bind(widthProperty());
//        s.yProperty().bind(heightProperty());
//        getTransforms().add(s);
//      TODO to allow clicks to pass through  pane.setMouseTransparent(true);
        flipProperty = new SimpleBooleanProperty(false);
       
    }

    public Node getContent() {
        return poly;
    }

    public void flip() {
        flipProperty.set(!flipProperty.get());
    }

    private void populateGridPane() {
        ObservableList<Node> children = this.getChildren();
        children.clear();
        int l = nodes.size();
        boolean flip = flipProperty.get();

        for (int i = 0; i < l; i++) {
            Node n = nodes.get(i);
            GridPane.setHalignment(n, HPos.CENTER);
            GridPane.setValignment(n, VPos.CENTER);
            GridPane.setHgrow(n, Priority.ALWAYS);
            GridPane.setVgrow(n, Priority.ALWAYS);
            GridPane.setRowIndex(n, 0);
            GridPane.setColumnIndex(n, flip ? l - i - 1 : i);
            children.add(n);
        }
    }

}
