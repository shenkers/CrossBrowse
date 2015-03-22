/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
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
public class GeneViewBuilderNGTest {
    
    public GeneViewBuilderNGTest() {
    }

     @BeforeClass
    public static void setUpClass() throws Exception {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Platform.exit();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testSomeMethod() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {

                    RangeSet<Integer> exons = TreeRangeSet.create();
                    exons.add(Range.closed(0, 3));
                    exons.add(Range.closed(5, 5));
                    exons.add(Range.closed(8, 10));
                    GeneViewBuilder gvb = new GeneViewBuilder(exons.span(), Optional.ofNullable(exons),Optional.ofNullable(Range.closed(3, 8)));
                    
                    GenericStackedIntervalView gsiv2 = new GenericStackedIntervalView(0, 10);
                    gsiv2.setData(Arrays.asList(
                            new Pair<Integer,Integer>(0,10),
                            new Pair<Integer,Integer>(0,10)
                    ), Arrays.asList(
                            new GenePartIntervalNode(.75, gvb.getView(0, 10)),
                                    new GenePartIntervalNode(.75, gvb.getView(-1, 9))
                    ));

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    BorderPane p = new BorderPane(gsiv2);

                    gsiv2.prefTileHeightProperty().bind(p.heightProperty().divide(3));
                    gsiv2.prefTileWidthProperty().bind(p.widthProperty());
//                    gsiv2.setData(Arrays.asList(
//                                    new Pair<>(0, 2),
//                            new Pair<>(1, 4),
//                            new Pair<>(4, 6)
//                            ), Arrays.asList(
//                                    f.apply(""),
//                                    f.apply(""),
//                                    f.apply("")
//                            ));
                    gsiv2.setVgap(0);
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
    }
    
}
