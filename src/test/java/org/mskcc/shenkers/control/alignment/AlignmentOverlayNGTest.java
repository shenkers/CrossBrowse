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
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
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

    static class BoundPoly3 {

        List<DoubleProperty> ypos;

        Polygon p;
        ObservableList<ObservableDoubleValue> observable;
        // map the list to doubles
        ObservableList<Double> points;
        List<DoubleProperty> xpos;
        List<BooleanProperty> flips;
        List<ObservableDoubleValue> interleaved;
        Callback<ObservableDoubleValue, Observable[]> extractor;
        DoubleProperty yScale;
        DoubleProperty xScale;
        List<ObservableDoubleValue> scaledY;
        List<ObservableDoubleValue> scaledX;
        List<DoubleProperty> xFlipped;

        public BoundPoly3(List<Double> xstart) {
            yScale = new SimpleDoubleProperty(1);
            xScale = new SimpleDoubleProperty(1);

            p = new Polygon();
            xpos = xstart.stream().map(SimpleDoubleProperty::new).collect(Collectors.toList());
            flips = xstart.stream().map(i -> false).map(SimpleBooleanProperty::new).collect(Collectors.toList());

//            IntStream.iterate(0, IntUnaryOperator.identity()).m;
            // since each y pos is doubled, we'll only create half the properties and then mirror them
            ypos = Stream.iterate(0, i -> i + 1).limit(xstart.size()).map(i -> new SimpleDoubleProperty()).collect(Collectors.toList());
            ypos.get(0).setValue(0);
            ypos.get(ypos.size() - 1).setValue(1);
            new SimpleDoubleProperty(0).bind(yScale);
            xFlipped = Stream.iterate(0, i -> i + 1).limit(xstart.size()).map(j -> {
                SimpleDoubleProperty sdp = new SimpleDoubleProperty();
                sdp.bind(new When(flips.get(j)).then(xpos.get(j).negate().add(1.)).otherwise(xpos.get(j)));
                return sdp;
            }
            ).collect(Collectors.toList());

            scaledY = ypos.stream().map(p -> p.multiply(yScale)).collect(Collectors.toList());
            scaledX = xFlipped.stream().map(p -> p.multiply(xScale)).collect(Collectors.toList());

            interleaved = zip(zip(scaledX, scaledX), mirror(scaledY));

            extractor
                    = (ObservableDoubleValue param) -> new Observable[]{param};

            // create an observable list that updates when any of the underlying double values
            // are changed
            observable = FXCollections.observableList(interleaved, extractor);
            // map the list to doubles
            points = EasyBind.map(observable, ObservableDoubleValue::get);
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

    static Logger logger = LogManager.getLogger();

    static class BoundPoly4 {

        Path p;

        DoubleProperty yScale;
        DoubleProperty xScale;

        List<Pair<DoubleProperty, DoubleProperty>> relativeXCoords;
        List<Pair<DoubleProperty, DoubleProperty>> relativeYCoords;

        List<Pair<DoubleBinding, DoubleBinding>> componentXCoords;
        List<Pair<DoubleBinding, DoubleBinding>> componentYCoords;

        List<DoubleProperty> genomeFlipped;

        List<PathElement> pathElements;

        enum PathCase {

            first, middle, l_even, l_odd, r_even, r_odd, last;

        };

        public BoundPoly4(int nGenomes) {

                        yScale = new SimpleDoubleProperty(1);
            xScale = new SimpleDoubleProperty(1);

            
            relativeXCoords = new ArrayList<>();
            relativeYCoords = new ArrayList<>();
            componentXCoords = new ArrayList<>();
            componentYCoords = new ArrayList<>();

            for (int i = 0; i < nGenomes; i++) {
                relativeXCoords.add(new Pair<>(new SimpleDoubleProperty(), new SimpleDoubleProperty()));
                relativeYCoords.add(new Pair<>(new SimpleDoubleProperty(), new SimpleDoubleProperty()));
                componentXCoords.add(new Pair<>(relativeXCoords.get(i).getKey().multiply(xScale), relativeXCoords.get(i).getValue().multiply(xScale)));
                componentYCoords.add(new Pair<>(relativeYCoords.get(i).getKey().multiply(yScale), relativeYCoords.get(i).getValue().multiply(yScale)));
            }


            pathElements = new ArrayList<>();

//            pathElements.add(new MoveTo());
            for (int i = 0; i < nGenomes * 4; i++) {
                Function<Integer, PathCase> f = (j -> {
                    if (j == 0) {
                        return PathCase.first;
                    } else if (j + 1 == nGenomes * 4) {
                        return PathCase.last;
                    } else if (j + 1 == nGenomes * 2) {
                        return PathCase.middle;
                    } else if (j % 2 == 1) {
                        if (j < nGenomes * 2) {
                            return PathCase.l_odd;
                        } else {
                            return PathCase.r_odd;
                        }
                    } else {
                        if (j < nGenomes * 2) {
                            return PathCase.l_even;
                        } else {
                            return PathCase.r_even;
                        }
                    }
                });

                logger.info("i {}", i);

                // get the index of the genome
                int g = i < nGenomes * 2 ? i / 2 : ((4 * nGenomes - i - 1) / 2);
                logger.info("g {}", g);

                Pair<DoubleBinding, DoubleBinding> xCoord = componentXCoords.get(g);
                Pair<DoubleBinding, DoubleBinding> yCoord = componentYCoords.get(g);

                PathCase pc = f.apply(i);
                switch (pc) {
                    case first: {
                        MoveTo moveTo = new MoveTo();
                        LineTo lineTo = new LineTo();

                        moveTo.xProperty().bind(xCoord.getKey());
                        moveTo.yProperty().bind(yCoord.getKey());

                        lineTo.xProperty().bind(xCoord.getKey());
                        lineTo.yProperty().bind(yCoord.getValue());

                        pathElements.add(moveTo);
                        pathElements.add(lineTo);
                        break;
                    }
                    case middle: {
                        LineTo lineTo = new LineTo();
                        lineTo.xProperty().bind(xCoord.getValue());
                        lineTo.yProperty().bind(yCoord.getValue());
                        pathElements.add(lineTo);
                        break;
                    }
                    case last: {
                        LineTo lineTo = new LineTo();
                        lineTo.xProperty().bind(xCoord.getKey());
                        lineTo.yProperty().bind(yCoord.getKey());
                        pathElements.add(lineTo);
                        break;
                    }
                    case l_odd: {
                        CubicCurveTo curveTo = new CubicCurveTo();

                        Pair<DoubleBinding, DoubleBinding> xCoordNext = componentXCoords.get(g + 1);
                        Pair<DoubleBinding, DoubleBinding> yCoordNext = componentYCoords.get(g + 1);

                        DoubleBinding controlY = yCoord.getValue().add(yCoordNext.getKey()).divide(2.);

                        curveTo.controlX1Property().bind(xCoord.getKey());
                        curveTo.controlY1Property().bind(controlY);

                        curveTo.controlX2Property().bind(xCoordNext.getKey());
                        curveTo.controlY2Property().bind(controlY);

                        curveTo.xProperty().bind(xCoordNext.getKey());
                        curveTo.yProperty().bind(yCoordNext.getKey());

                        pathElements.add(curveTo);
                        break;
                    }
                    case l_even: {
                        LineTo lineTo = new LineTo();
                        lineTo.xProperty().bind(xCoord.getKey());
                        lineTo.yProperty().bind(yCoord.getValue());
                        pathElements.add(lineTo);
                        break;
                    }
                    case r_odd: {
                        CubicCurveTo curveTo = new CubicCurveTo();

                        Pair<DoubleBinding, DoubleBinding> xCoordNext = componentXCoords.get(g - 1);
                        Pair<DoubleBinding, DoubleBinding> yCoordNext = componentYCoords.get(g - 1);

                        DoubleBinding controlY = yCoord.getKey().add(yCoordNext.getValue()).divide(2.);

                        curveTo.controlX1Property().bind(xCoord.getValue());
                        curveTo.controlY1Property().bind(controlY);

                        curveTo.controlX2Property().bind(xCoordNext.getValue());
                        curveTo.controlY2Property().bind(controlY);

                        curveTo.xProperty().bind(xCoordNext.getValue());
                        curveTo.yProperty().bind(yCoordNext.getValue());

                        pathElements.add(curveTo);
                        break;
                    }
                    case r_even: {
                        LineTo lineTo = new LineTo();
                        lineTo.xProperty().bind(xCoord.getValue());
                        lineTo.yProperty().bind(yCoord.getKey());
                        pathElements.add(lineTo);
                        break;
                    }
                    default: {
                        throw new RuntimeException("should not get here");
                    }
                }
            }

            p = new Path(pathElements);

        }

        class ScaledLine extends DoubleBinding {

            DoubleProperty xScale;
            DoubleProperty yScale;

            // relativeCoordinates
            DoubleProperty x;
            DoubleProperty y;

            @Override
            protected double computeValue() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        }

        public Path getPath() {
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

    BoundPoly3 bp;
    BoundPoly4 bp4;
    ObservableList<SplitPane.Divider> dividers;
    DoubleProperty pp;

    @Override
    public void start(Stage stage) throws Exception {
//        build();

        bp = new BoundPoly3(Arrays.asList(
                .1, .4, .2, .3,
                .4, .2, .5, .2
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
        dividers = sp.getDividers();

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
        List<DoubleProperty> collect = sp.getDividers().stream().map(d -> d.positionProperty()).collect(Collectors.toList());
        ObservableList<DoubleProperty> oc = FXCollections.observableList(collect, new Callback<DoubleProperty, Observable[]>() {

            @Override
            public Observable[] call(DoubleProperty param) {
                return new Observable[]{param};
            }
        });

//        Bindings.bindContent(bp.ypos.subList(1, 7), oc);
        for (int i = 0; i < 6; i++) {
            bp.ypos.get(i + 1).bind(sp.getDividers().get(i).positionProperty());
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

//        root.getChildren().add(ap.getPolygon());
        bp4 = new BoundPoly4(4);

        final int w = 1;
        bp4.relativeXCoords.get(0).getKey().setValue(0.05 * w);
        bp4.relativeXCoords.get(0).getValue().setValue(0.15 * w);
        bp4.relativeXCoords.get(1).getKey().setValue(0.25 * w);
        bp4.relativeXCoords.get(1).getValue().setValue(0.4 * w);
        bp4.relativeXCoords.get(2).getKey().setValue(0.8 * w);
        bp4.relativeXCoords.get(2).getValue().setValue(0.85 * w);
        bp4.relativeXCoords.get(3).getKey().setValue(0.6 * w);
        bp4.relativeXCoords.get(3).getValue().setValue(0.75 * w);

        bp4.relativeYCoords.get(0).getKey().setValue(0.0 * w);
        bp4.relativeYCoords.get(0).getValue().bind(dividers.get(0).positionProperty().multiply(w));
        bp4.relativeYCoords.get(1).getKey().bind(dividers.get(1).positionProperty().multiply(w));
        bp4.relativeYCoords.get(1).getValue().bind(dividers.get(2).positionProperty().multiply(w));
        bp4.relativeYCoords.get(2).getKey().bind(dividers.get(3).positionProperty().multiply(w));
        bp4.relativeYCoords.get(2).getValue().bind(dividers.get(4).positionProperty().multiply(w));
        bp4.relativeYCoords.get(3).getKey().bind(dividers.get(5).positionProperty().multiply(w));
        bp4.relativeYCoords.get(3).getValue().setValue(1.0 * w);
        
        
        logger.info("elements {}", bp4.pathElements);

//        root.getChildren().add(bp.getPoly());
        root.getChildren().add(bp4.getPath());
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
//        HBox bor = new HBox();
        StackPane bor = new StackPane(sp, root);
        bp.getPoly().setFill(Color.TRANSPARENT);
        bp.getPoly().setStroke(new Color(0, 0, 0, 1));
        bp.xScale.bind(bor.widthProperty());
        bp.yScale.bind(bor.heightProperty());
        bp4.xScale.bind(bor.widthProperty());
        bp4.yScale.bind(bor.heightProperty());


        root.setMouseTransparent(true);
        root.setPrefWidth(300);
        sp.setPrefWidth(300);
//        bor.getChildren().addAll(root, sp, new Polygon(0,0,200,200,100,50));
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
