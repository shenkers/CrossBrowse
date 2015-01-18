/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import static org.testng.Assert.*;

/**
 *
 * @author sol
 */
public class AlignmentOverlayNGTest extends Application {

    public static <T> List<T> zip(List<T> a, List<T> b) {

        List<T> l = new ArrayList<>(a.size() + b.size());
        for (int i = 0; i < a.size() || i < b.size(); i++) {
            if (i < a.size()) {
                l.add(a.get(i));
            }
            if (i < b.size()) {
                l.add(b.get(i));
            }
        }

        return l;
    }

    public void build() {
        List<Double> xpos = FXCollections.observableArrayList(.1, .2, .3);

//        String s = "abc";
//        System.out.println(Arrays.asList(s, s, s));
//        System.exit(0);
        List<Double> doubled = xpos.stream().map(d -> Arrays.asList(d, d)).flatMap(l -> l.stream()).collect(Collectors.toList());
        System.err.println(doubled);
        Collector<Double, List<Double>, List<Double>> col = Collector.of(ArrayList::new, (a, b) -> {
            System.err.println("consuming a " + a + " b " + b);
            return;
        }, (a, b) -> {
            System.err.println("operating a " + a + " b " + b);
            return null;
        });
        xpos.stream().collect(col);

        List<Double> ypos = Arrays.asList(.9,.8,.7);
        List<Double> zip = zip(xpos, ypos);
        System.err.println(zip);
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws Exception {
        build();

        DoubleProperty dp = new SimpleDoubleProperty(2);
        ObservableList<DoubleProperty> oal = FXCollections.observableArrayList(new Callback<DoubleProperty, Observable[]>() {

            @Override
            public Observable[] call(DoubleProperty param) {
                return new Observable[]{param};
            }
        });
        oal.add(dp);

        ObservableList<DoubleProperty> oal2 = FXCollections.observableArrayList(dp);

        oal.addListener(new ListChangeListener<DoubleProperty>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends DoubleProperty> c) {
                System.out.println("oal changed");
            }
        });
        oal2.addListener(new ListChangeListener<DoubleProperty>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends DoubleProperty> c) {
                System.out.println("oal2 changed");
            }
        });
        System.out.println("changed dp");

        dp.addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("dp listener");
            }
        });
        dp.setValue(3);

        Polygon p = new Polygon(.2, 0., .3, 0., .6, .5, 1.2, 1., 1.1, 1., .5, .5);
        Polygon p1 = new Polygon(.2, 0., .3, 0., .6, .5, .5, .5);
        Polygon p2 = new Polygon(.6, .5, .2, 1., .1, 1., .5, .5);

        AlignmentOverlay root = new AlignmentOverlay();

        List<Double> weights = FXCollections.observableArrayList(0., .5, 1.);
        List<GenomeSpan> spans = new ArrayList<>();
        spans.add(new GenomeSpan("", 1, 1, false));
        spans.add(new GenomeSpan("", 3, 3, false));
        spans.add(new GenomeSpan("", 2, 2, false));
        List<GenomeSpan> displayedSpans = new ArrayList<>();
        displayedSpans.add(new GenomeSpan("", 1, 10, true));
        displayedSpans.add(new GenomeSpan("", 1, 10, true));
        displayedSpans.add(new GenomeSpan("", 1, 10, true));
        AlignmentPolygon ap = new AlignmentPolygon(spans, displayedSpans, weights);

        root.getChildren().add(ap.getPolygon());
//        root.getChildren().add(p);
//        root.widthProperty().addListener(new ChangeListener<Number>() {
//
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if(Math.random()<.01){
//                double r=Math.random();
//                p.getPoints().set(5, r);
//                p.getPoints().set(11, r);
//                }
//            }
//        });
        Scene scene = new Scene(root, 300, 300, Color.GRAY);
        stage.setTitle("JavaFX Scene Graph Demo");
        stage.setScene(scene);
        stage.show();
    }

    public AlignmentOverlayNGTest() {
    }

    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getContent method, of class AlignmentOverlay.
     */
    @org.testng.annotations.Test
    public void test() {
        launch(new String[0]);
        fail("The test case is a prototype.");
    }

}
