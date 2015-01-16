/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;

/**
 *
 * @author sol
 */
public class SparseLineHistogramView {

    private static final Logger logger = LogManager.getLogger();

    Pane graphic;

    // the range of data plotted
    DoubleProperty minProperty;
    DoubleProperty maxProperty;

    // whether or not the reference frames should be flipped
    BooleanProperty flipDomainProperty;
    BooleanProperty flipRangeProperty;

    Rectangle background;

    // the number of data points
    IntegerProperty n;

    // where the base of the histogram starts from
    NumberBinding zero;

    public SparseLineHistogramView() {

        minProperty = new SimpleDoubleProperty(0);
        maxProperty = new SimpleDoubleProperty(1);

        flipDomainProperty = new SimpleBooleanProperty(false);
        flipRangeProperty = new SimpleBooleanProperty(true);

        graphic = new BorderPane();

        background = new Rectangle();

        background.setFill(Color.WHITE);
        background.setStroke(Color.WHITE);

        background.widthProperty().bind(graphic.widthProperty());
        background.heightProperty().bind(graphic.heightProperty());

        graphic.getChildren().add(background);

        zero = bindToRange(0);
        n = new SimpleIntegerProperty(0);
    }

    public Pane getGraphic() {
        return graphic;
    }

    public void setMin(double d) {
        minProperty.set(d);
    }

    public void setMax(double d) {
        maxProperty.set(d);
    }

    public void clearData() {
        n.setValue(0);
        graphic.getChildren().retainAll(background);
    }

    public void setData(Map<Integer,Double> data, int length, Task task) {
        n.set(length);

        for (Integer i : data.keySet()) {
            graphic.getChildren().add(getLine(i, zero, data.get(i)));
            if (task.isCancelled()) {
                logger.info("recieved cancel");
                break;
            }
        }
    }

    /**
     *
     * @param i - the index
     * @param l - the number of data points
     * @param d - the value of the data point
     * @return
     */
    private Line getLine(int i, NumberBinding zero, double d) {

        Line line = new Line(0., 0., 0., 1.);

        NumberBinding domainBinding = new When(flipDomainProperty)
                .then(
                        n.subtract(i + 1.).divide(n.add(1.)))
                .otherwise(
                        new SimpleDoubleProperty(i + 1.).divide(n.add(1.))).multiply(graphic.widthProperty());

        line.startXProperty().bind(domainBinding);
        line.endXProperty().bind(domainBinding);

        line.startYProperty().bind(zero);
        line.endYProperty().bind(bindToRange(d));

        return line;
    }

//    /**
//     *
//     * @param i - the index
//     * @param l - the number of data points
//     * @param d - the value of the data point
//     * @return
//     */
//    private Line getLine(int i, int l, NumberBinding zero, double d) {
//
//        Line line = new Line(0., 0., 0., 1.);
//
//        DoubleBinding domainBinding = new When(flipDomainProperty).then((l - (i + 1.)) / (l + 1)).otherwise((i + 1.) / (l + 1)).multiply(graphic.widthProperty());
//
//        line.startXProperty().bind(domainBinding);
//        line.endXProperty().bind(domainBinding);
//
//        line.startYProperty().bind(zero);
//        line.endYProperty().bind(bindToRange(d));
//
//        return line;
//    }
    private NumberBinding bindToRange(double d) {

        NumberBinding whetherZeroGreaterThanMax = new When(maxProperty.lessThan(d)).then(maxProperty).otherwise(minProperty.multiply(-1).add(d).divide(maxProperty.subtract(minProperty)));
        NumberBinding base = new When(minProperty.greaterThan(d)).then(minProperty).otherwise(whetherZeroGreaterThanMax);
        NumberBinding flipped = new When(flipRangeProperty).then(base.multiply(-1).add(1)).otherwise(base);

        NumberBinding scaled = flipped.multiply(graphic.heightProperty());

        return scaled;
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return graphic.widthProperty();
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return graphic.heightProperty();
    }

    public BooleanProperty flipDomainProperty() {
        return flipDomainProperty;
    }

    public void setFlipDomain(boolean flip) {
        flipDomainProperty.set(flip);
    }

    public void setFlipRange(boolean flip) {
        flipRangeProperty.set(!flip);
    }
}
