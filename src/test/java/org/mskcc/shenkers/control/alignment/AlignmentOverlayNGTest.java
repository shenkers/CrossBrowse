/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import static org.testng.Assert.*;

/**
 *
 * @author sol
 */
public class AlignmentOverlayNGTest extends Application {

    Logger l = LogManager.getLogger();

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

    public static <T> List<T> mirror(List<T> a) {
        List<T> l = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            l.add(a.get(i));
        }
        for (int i = a.size() - 1; i > -1; i--) {
            l.add(a.get(i));
        }
        return l;
    }

    static class BoundPoly {

        List<DoubleProperty> ypos;

        Polygon p;
        ObservableList<DoubleProperty> observable;
            // map the list to doubles
            ObservableList<Double> points;
            List<DoubleProperty> xpos;
List<DoubleProperty> interleaved;
Callback<DoubleProperty, Observable[]> extractor;
        public BoundPoly(List<Double> xstart) {
            p = new Polygon();
            xpos = xstart.stream().map(SimpleDoubleProperty::new).collect(Collectors.toList());
//            IntStream.iterate(0, IntUnaryOperator.identity()).m;

            // since each y pos is doubled, we'll only create half the properties and then mirror them
            ypos = Stream.iterate(0, i -> i + i).limit(xstart.size()).map(i -> new SimpleDoubleProperty()).collect(Collectors.toList());
            ypos.get(0).setValue(0);
            ypos.get(ypos.size() - 1).setValue(1);

            interleaved = zip(zip(xpos, xpos), mirror(ypos));

            extractor
                    = (DoubleProperty param) -> new Observable[]{param};

            // create an observable list that updates when any of the underlying double values
            // are changed
            observable = FXCollections.observableList(interleaved, extractor);
            // map the list to doubles
            points = EasyBind.map(observable, DoubleProperty::getValue);
            p.getPoints().setAll(points);
            points.addListener(new ListChangeListener<Double>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends Double> c) {
//                   p.getPoints().setAll(points);
                    System.err.println("points listener detects change");

                    while (c.next()) {
                        if (c.wasUpdated()) {
                            System.err.println("updated from " + c.getFrom() + " to " + c.getTo());

                            for (int i = c.getFrom(); i < c.getTo(); i++) {
                                p.getPoints().set(i, points.get(i));
                            }
                        }
                    }
                }
            });

//            EasyBind.listBind(p.getPoints(), points);
        }

        public Polygon getPoly() {
            return p;
        }

    }

    static class BoundPoly2 {

        List<Double> xpos;
        ObservableList<Double> ypos;

        Polygon p;
        
        public BoundPoly2(List<Double> xstart) {
            p = new Polygon();
            xpos = FXCollections.observableArrayList(xstart);
            ypos = FXCollections.observableArrayList(Collections.nCopies(xstart.size(), 0.));

//            ypos.addChange
//            interleaved = zip(zip(xpos, xpos), mirror(ypos));
            ypos.addListener(new ListChangeListener<Double>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends Double> c) {
                    p.getPoints().setAll(zip(zip(xpos, xpos), mirror(ypos)));
                }
            });
        }

        public Polygon getPoly() {
            return p;
        }

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

        List<Double> ypos = Arrays.asList(.9, .8, .7);
        List<Double> zip = zip(xpos, ypos);
//        System.err.println(zip);
        System.err.println(mirror(ypos));
        System.err.println(zip(doubled, mirror(ypos)));
        System.exit(0);
    }
    
    BoundPoly bp;

    @Override
    public void start(Stage stage) throws Exception {
//        build();
       
        bp = new BoundPoly(Arrays.asList(
                .1, .4, .2, .3,
                .4, .3, .5, .2
        //                10.,40.,20.,30.,
        //                40.,30.,50.,20.
        ));

        List<Double> splitPane = Arrays.asList(0., .1, .2, .3, .6, .7, .8, 1.);

        for (int i = 0; i < splitPane.size(); i++) {
            bp.ypos.get(i).setValue(splitPane.get(i));
        }
        System.err.println(bp.ypos);
        System.err.println("polypoints");
        System.err.println(bp.getPoly().getPoints());

        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);

        sp.getItems().addAll(
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a")),
                new BorderPane(new Label("a"))
        );

//        ObservableList<DoubleProperty> map = EasyBind.map(sp.getDividers(), SplitPane.Divider::positionProperty);
        ObservableList<SplitPane.Divider> dividers = sp.getDividers();
     

//        sp.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
//
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("change in div detected");
//                System.err.println(Thread.currentThread().getName());
//                System.err.println("bp " + bp.ypos.get(1).getValue());
//                System.err.println("new Value" + newValue);
////                bp.ypos.get(1).setValue(Math.max(0, Math.min(1, newValue.doubleValue())));
//                Platform.runLater(()->{bp.ypos.get(1).setValue(Math.max(0, Math.min(1, newValue.doubleValue())));});
//                
//            }
//        });
        List<DoubleProperty> collect = sp.getDividers().stream().map(d->d.positionProperty()).collect(Collectors.toList());
        ObservableList<DoubleProperty> oc = FXCollections.observableList(collect, new Callback<DoubleProperty, Observable[]>() {
            
            @Override
            public Observable[] call(DoubleProperty param) {
                return new Observable[]{param};
            }
        });
        
//        Bindings.bindContent(bp.ypos.subList(1, 7), oc);
        for(int i=0; i<6; i++){
             bp.ypos.get(i+1).bind(sp.getDividers().get(i).positionProperty());
        }
//        bp.ypos.get(1).bind(sp.getDividers().get(0).positionProperty());
//        bp.ypos.get(2).bind(sp.getDividers().get(1).positionProperty());
//        bp.ypos.get(3).bind(sp.getDividers().get(2).positionProperty());
//        bp.ypos.get(4).bind(sp.getDividers().get(3).positionProperty());
//        bp.ypos.get(5).bind(sp.getDividers().get(4).positionProperty());
//        bp.ypos.get(6).bind(sp.getDividers().get(5).positionProperty());
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
        root.getChildren().add(bp.getPoly());
//        root.getChildren().add(new Polygon(0.1, 0.0, 0.1, 0.1, 0.4, 0.2, 0.4, 0.3, 0.2, 0.6, 0.2, 0.7, 0.3, 0.8, 0.3, 1.0, 0.4, 1.0, 0.4, 0.8, 0.3, 0.7, 0.3, 0.6, 0.5, 0.3, 0.5, 0.2, 0.2, 0.1, 0.2, 0.0));
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
//        BorderPane bor = new BorderPane();
        HBox bor = new HBox();
        root.setPrefWidth(300);
        sp.setPrefWidth(300);
        bor.getChildren().addAll(root, sp);
//        bor.setLeft(root);
//        bor.setRight(sp);
        Scene scene = new Scene(bor, 300, 300, Color.GRAY);
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