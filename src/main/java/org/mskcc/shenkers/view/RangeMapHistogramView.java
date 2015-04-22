/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.data.interval.RangeTools;

/**
 *
 * @author sol
 */
public class RangeMapHistogramView {

    private static final Logger logger = LogManager.getLogger();

    Pane graphic;
    Polygon p = new Polygon();
    ObjectBinding<List<Double>> listBinding;

    // the range of data plotted
    DoubleProperty minProperty;
    DoubleProperty maxProperty;

    // whether or not the reference frames should be flipped
    BooleanProperty flipDomainProperty;
    BooleanProperty flipRangeProperty;

    Rectangle background;

    // where the base of the histogram starts from
    DoubleProperty zero;

    InvalidationListener pointUpdater = o -> {
        p.getPoints().setAll(listBinding.get());
    };

    public RangeMapHistogramView() {

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

        zero = new SimpleDoubleProperty(0);
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
        graphic.getChildren().retainAll(background);
    }

    public void setData(Range<Integer> view, RangeMap<Integer, Double> data) {

        int lb = view.lowerEndpoint();
        int ub = view.upperEndpoint();
        int r = ub - lb + 1;
        Map<Range<Integer>, Double> map = data.asMapOfRanges();


        if (listBinding != null) {
            listBinding.removeListener(pointUpdater);
        }
        listBinding = Bindings.createObjectBinding(() -> {

            double width = graphic.widthProperty().get();
            double height = graphic.heightProperty().get();
            double min = minProperty.get();
            double max = maxProperty.get();
            boolean flipDomain = flipDomainProperty.get();
            boolean flipRange = flipRangeProperty.get();

            List<Double> points = map
                    .entrySet()
                    .stream()
                    .flatMap(entry
                            -> {
                        
                        Double value = scaleValue(entry.getValue(), min, max, height, flipRange);
                        double[] d = scaleDomain(entry.getKey(), lb, r, width, flipDomain);
                        return Stream.of(
                                d[0], value,
                                d[1], value
                        );
                    }
                    ).collect(Collectors.toList());
            points.addAll(0, Arrays.asList(points.get(0), scaleValue(zero.doubleValue(), min, max, height,flipRange)));
            points.addAll(Arrays.asList(points.get(points.size() - 2), scaleValue(zero.doubleValue(), min, max, height,flipRange)));

            return points;
        }, graphic.widthProperty(), graphic.heightProperty(), minProperty, maxProperty, flipRangeProperty, flipDomainProperty, zero);

        listBinding.addListener(pointUpdater);

        graphic.getChildren().add(p);
    }

    private double[] scaleDomain(Range<Integer> i, int lb, double r, double width, boolean flip) {
        double s = (i.lowerEndpoint().doubleValue()-lb)/r;
        double e = (i.upperEndpoint().doubleValue()-lb+1)/r;
        
        double flippedS = flip ? (1 - s) : s;
        double flippedE = flip ? (1 - e) : e;
        
        double scaledS = flippedS * width;
        double scaledE = flippedE * width;
        
        return new double[]{scaledS,scaledE};
    }
    
    private double scaleValue(double value, double min, double max, double height, boolean flip) {
        double windowed = (value - min) / (max - min);
        double flipped = flip ? (1 - windowed) : windowed;
        double scaled = flipped * height;
        
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
