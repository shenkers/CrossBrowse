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
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.HiddenSidesPane;
import org.mskcc.shenkers.data.IntervalDataSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class IntervalViewNGTest {

    public IntervalViewNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    @Test
    public void testRangeSetIntervalView() throws InterruptedException {
        System.out.println("testIntervalView");

        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    System.out.println("running");
                    double[][] intervals = {
                        {.1, .2}
                    };
//                    Range r = null;
                    RangeSet<Double> rs = TreeRangeSet.create();
                    rs.add(Range.closed(.1, .2));
                    rs.add(Range.closed(.2, .3));
                    rs.add(Range.closed(.32, .35));
                    rs.add(Range.closed(.6, .8));

                    RangeSetIntervalView p = new RangeSetIntervalView(0, 100);
                    p.setData(Arrays.asList(
                                    new Pair(10, 20),
                                    new Pair(20, 30),
                                    new Pair(32, 35),
                                    new Pair(60, 80)
                            ));

//                    p.prefTileHeightProperty().bind(p.heightProperty());
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }

//    @Test
    public void testIntervalView() throws InterruptedException {
        System.out.println("testIntervalView");
        Pane p = new Pane();

        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    System.out.println("running");
                    double[][] intervals = {
                        {.1, .2}
                    };
//                    Range r = null;
                    RangeSet<Double> rs = TreeRangeSet.create();
                    rs.add(Range.closed(.1, .2));
                    rs.add(Range.closed(.2, .3));
                    rs.add(Range.closed(.32, .35));
                    rs.add(Range.closed(.6, .8));

                    for (Range<Double> r : rs.asRanges()) {
                        System.out.println(r.lowerEndpoint() + " - " + r.upperEndpoint());
                    }
                    for (Range<Double> interval : rs.asRanges()) {
                        Rectangle r = new Rectangle();
                        r.widthProperty().bind(p.widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint()));
                        r.heightProperty().bind(p.heightProperty());
                        r.xProperty().bind(p.widthProperty().multiply(interval.lowerEndpoint()));
                        p.getChildren().add(r);
                    }
//                    p.prefTileHeightProperty().bind(p.heightProperty());
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }
    
     @Test
    public void testGenericStackedIntervalView() throws InterruptedException {
        List<Pair<Integer,Integer>> intervals = Arrays.asList(
                new Pair(0, 1),
                new Pair(1, 2),
                new Pair(2, 3),
                new Pair(3, 4),
                new Pair(4, 5),
                new Pair(5, 6)
        );
        List<Pane> nodes = intervals.stream().map(i->new RectangleIntervalNode()).collect(Collectors.toList());
        GenericStackedIntervalView p = new GenericStackedIntervalView(0, 6);
        p.setData(intervals,nodes);
        
        ScrollPane sp = new ScrollPane(p);
        ScrollBar sb = new ScrollBar();
        sb.maxProperty().bind(sp.vmaxProperty());
        sb.minProperty().bind(sp.vminProperty());
        sb.visibleAmountProperty().bind(sp.heightProperty().divide(p.prefHeightProperty()));
        sb.setOrientation(Orientation.VERTICAL);
        sp.vvalueProperty().bindBidirectional(sb.valueProperty());
        HiddenSidesPane hsp = new HiddenSidesPane();
        hsp.setContent(sp);
        hsp.setRight(sb);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        p.setOrientation(Orientation.VERTICAL);
        p.prefTileHeightProperty().bind(new SimpleDoubleProperty(40));
//        p.minHeightProperty().bind(new SimpleDoubleProperty(20).multiply(Bindings.size(p.getChildren())));
        p.prefTileWidthProperty().bind(sp.widthProperty());
        p.prefHeightProperty().bind(new SimpleDoubleProperty(50).multiply(Bindings.size(p.getChildren())).subtract(10));
        p.prefWidthProperty().bind(sp.widthProperty());
        sp.setPadding(Insets.EMPTY);
        p.setVgap(10);

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    Scene scene = new Scene(hsp, 300, 300, Color.GRAY);
                    stage.setTitle("GenericStackedPaneTest");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        l.await();
    }

//    @Test
    public void testStackedIntervalView() throws InterruptedException {
        StackedIntervalView p = new StackedIntervalView(0, 6);
        p.setData(Arrays.asList(
                new Pair(0, 1),
                new Pair(1, 2),
                new Pair(2, 3),
                new Pair(3, 4),
                new Pair(4, 5),
                new Pair(5, 6)
        //, {8, 10}, {1, 2}, {3, 7},
        //            {9, 10}, {1, 2}, {3, 5}, {6, 7}, {8, 10}, {2, 5}, {8, 10}
        ));
        
           ScrollPane sp = new ScrollPane(p);
        ScrollBar sb = new ScrollBar();
        sb.maxProperty().bind(sp.vmaxProperty());
        sb.minProperty().bind(sp.vminProperty());
        sb.visibleAmountProperty().bind(sp.heightProperty().divide(p.prefHeightProperty()));
        sb.setOrientation(Orientation.VERTICAL);
        sp.vvalueProperty().bindBidirectional(sb.valueProperty());
        HiddenSidesPane hsp = new HiddenSidesPane();
        hsp.setContent(sp);
        hsp.setRight(sb);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        p.setOrientation(Orientation.VERTICAL);
        p.prefTileHeightProperty().bind(new SimpleDoubleProperty(40));
//        p.minHeightProperty().bind(new SimpleDoubleProperty(20).multiply(Bindings.size(p.getChildren())));
        p.prefTileWidthProperty().bind(sp.widthProperty());
        p.prefHeightProperty().bind(new SimpleDoubleProperty(50).multiply(Bindings.size(p.getChildren())).subtract(10));
        p.prefWidthProperty().bind(sp.widthProperty());
        sp.setPadding(Insets.EMPTY);
        p.setVgap(10);

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    Scene scene = new Scene(hsp, 300, 300, Color.GRAY);
                    stage.setTitle("StackedPaneTest");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        l.await();
    }

//    @Test
    public void testStackIntervalView() throws InterruptedException {
        System.out.println("testStackIntervalView");
        int[][] d = new int[][]{
            {2, 6}, {7, 10}, {1, 3}, {4, 6}, {8, 10}, {1, 2}, {3, 7},
            {9, 10}, {1, 2}, {3, 5}, {6, 7}, {8, 10}, {2, 5}, {8, 10}
        };
        List<int[]> asList = Arrays.asList(d);
        Collections.sort(asList, new Comparator<int[]>() {

            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });
        List<TreeRangeSet<Integer>> rows = new ArrayList<>();
        rows.add(TreeRangeSet.create());
        for (int[] r : d) {
            Range<Integer> R = Range.closed(r[0], r[1]);
            int i = 0;
            added:
            {
                while (i < rows.size()) {
                    TreeRangeSet<Integer> set = rows.get(i);
                    RangeSet<Integer> intersection = set.subRangeSet(Range.closed(r[0] - 1, r[1] + 1));
                    if (intersection.isEmpty()) {
                        set.add(R);
                        break added;
                    }
                    i++;
                }
//                Stri i = ;
                TreeRangeSet<Integer> row = TreeRangeSet.create();
                row.add(R);
                rows.add(row);
            }
        }
        TilePane p = new TilePane();
        p.setSnapToPixel(false);
        for (int i = 0; i < rows.size(); i++) {
            p.getChildren().add(get(rows.get(i), 0, 11));
            System.out.println(rows.get(i).toString());
            StringBuilder sb = new StringBuilder(11);
            sb.append(StringUtils.repeat(".", 11));
            for (int j = 0; j < 11; j++) {
                if (rows.get(i).contains(j)) {
                    sb.setCharAt(j, 'X');
                }
            }
            System.out.println(sb.toString());
        }
//        p.prefWidth(100);
//        p.prefHeight(100);
        ScrollPane sp = new ScrollPane(p);
        ScrollBar sb = new ScrollBar();
        sb.maxProperty().bind(sp.vmaxProperty());
        sb.minProperty().bind(sp.vminProperty());
        sb.visibleAmountProperty().bind(sp.heightProperty().divide(p.prefHeightProperty()));
        sb.setOrientation(Orientation.VERTICAL);
        sp.vvalueProperty().bindBidirectional(sb.valueProperty());
        HiddenSidesPane hsp = new HiddenSidesPane();
        hsp.setContent(sp);
        hsp.setRight(sb);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        p.setOrientation(Orientation.VERTICAL);
        p.prefTileHeightProperty().bind(new SimpleDoubleProperty(40));
//        p.minHeightProperty().bind(new SimpleDoubleProperty(20).multiply(Bindings.size(p.getChildren())));
        p.prefTileWidthProperty().bind(sp.widthProperty());
        p.prefHeightProperty().bind(new SimpleDoubleProperty(50).multiply(Bindings.size(p.getChildren())).subtract(10));
        p.prefWidthProperty().bind(sp.widthProperty());
        sp.setPadding(Insets.EMPTY);
        p.setVgap(10);

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    Scene scene = new Scene(hsp, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        l.await();
    }

    public Node get(RangeSet<Integer> intervals, int start, int end) {
        RangeSet<Integer> view = intervals.subRangeSet(Range.closed(start, end));
        double l = end - start + 1.;
        Pane p = new Pane();
        for (Range<Integer> interval : view.asRanges()) {
            Rectangle r = new Rectangle();
            r.widthProperty().bind(p.widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint() + 1).divide(l));
            r.heightProperty().bind(p.heightProperty());
            r.xProperty().bind(p.widthProperty().multiply(interval.lowerEndpoint()).divide(l));
//            System.out.println(r);
            p.getChildren().add(r);
        }
        return p;
    }

}
