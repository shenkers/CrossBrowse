/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.mskcc.shenkers.control.track.DomainFlippable;
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
public class GenePartIntervalNodeNGTest {

    public GenePartIntervalNodeNGTest() {
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
    public <T extends Pane & DomainFlippable> void testSomeMethod() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);
        GenericStackedIntervalView gsiv2 = new GenericStackedIntervalView(0, 10);

        System.out.println("before");
        Platform.runLater(
                () -> {

//                    GenericStackedIntervalView gsiv = new GenericStackedIntervalView(0, 5);
                    Function<String, T> f = (str) -> {
                        GenericIntervalView gsiv = new GenericIntervalView(0, 25);
                        gsiv.setData(
                                Arrays.asList(
                                        new Pair<>(0, 8),
                                        new Pair<>(9, 11),
                                        new Pair<>(12, 13),
                                        new Pair<>(14, 14),
                                        new Pair<>(15, 17),
                                        new Pair<>(18, 23),
                                        new Pair<>(24, 25)),
                                Arrays.asList(
                                        new GenePartIntervalNode(.5, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(1, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(.1, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(1, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(.1, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(1, new RectangleIntervalNode()),
                                        new GenePartIntervalNode(.5, new RectangleIntervalNode()))
                        );
                        return (T) gsiv;
                    };
                    
                    gsiv2.flipDomainProperty().setValue(true);
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    BorderPane p = new BorderPane(gsiv2);

                    gsiv2.prefTileHeightProperty().bind(p.heightProperty().divide(3));
                    gsiv2.prefTileWidthProperty().bind(p.widthProperty());
                    gsiv2.setData(Arrays.asList(
                                    new Pair<>(0, 2),
                                    new Pair<>(1, 4),
                                    new Pair<>(4, 6)
                            ), Arrays.asList(
                                    f.apply(""),
                                    f.apply(""),
                                    f.apply("")
                            ));
                    gsiv2.setVgap(0);
                    
                    gsiv2.setOnMouseClicked(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {
                            System.out.println("clicked "+gsiv2.flipDomainProperty().get());
                             gsiv2.flipDomainProperty().set(!gsiv2.flipDomainProperty().get());
                            
//        while (1==1) {
//            System.out.println("here");
//            try {
//                
//                System.in.read();
//                System.out.println(""+gsiv2.flipDomainProperty().get());
//               
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
                        }
                    });
                    Scene scene = new Scene(p, 300, 300, Color.WHITE);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
//        System.out.println("after");
        l.await();
    }

}
