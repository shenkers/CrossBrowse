/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.Indicator;
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
public class VerticalHiddenScrollPaneBuilderNGTest {

    public VerticalHiddenScrollPaneBuilderNGTest() {
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
    public void testClickRouting(){
    
        EventSource<Integer> clicks = new EventSource<>();
        final BooleanProperty notAnnotating = new SimpleBooleanProperty(true);
        final BooleanProperty notZooming = new SimpleBooleanProperty(true);
        EventStream<Integer> annotationClicks = clicks.suppressWhen(notAnnotating);
        EventStream<Integer> zoomClicks = clicks.suppressWhen(notZooming);
        
        class annotator implements Consumer<Integer> {

            int i=0;
            List<Integer> l = new ArrayList<>();
            public void accept(Integer t) {
                if(i==0){
                    l.clear();
                }
                i=(i+1)%2;
                l.add(t);
                if(i==0){
                    System.out.println("annotated "+l);
                    notAnnotating.setValue(true);
                }
            }
            
        }
        
        class zoomer implements Consumer<Integer> {

            int i=0;
            List<Integer> l = new ArrayList<>();
            public void accept(Integer t) {
                if(i==0){
                    l.clear();
                }
                i=(i+1)%2;
                l.add(t);
                if(i==0){
                    System.out.println("zoomed "+l);
                    notZooming.setValue(true);
                }
            }
            
        }
        
        annotationClicks.subscribe(new annotator());
        zoomClicks.subscribe(new zoomer());
        
        clicks.push(10);
        notAnnotating.setValue(false);
        clicks.push(11);
        clicks.push(12);
        clicks.push(13);
        notZooming.setValue(false);
        clicks.push(14);
        clicks.push(15);
        clicks.push(16);
        clicks.push(17);
    }
    
    @Test
    public void testIndicator() {
        ObservableList<BooleanProperty> isBusy = FXCollections.observableArrayList();

        isBusy.addListener((ListChangeListener.Change<? extends BooleanProperty> c) -> System.out.println("changed " + c.getList()));
        MonadicBinding<Boolean> combine = EasyBind.combine(isBusy, stream -> stream.reduce(false, (r, e) -> r || e));
        combine.addListener((a, b, c) -> {
            System.out.println("was " + b + " now " + c);
        });
        BooleanProperty busyA = new SimpleBooleanProperty(false);
        BooleanProperty busyB = new SimpleBooleanProperty(false);
        isBusy.add(busyA);
        busyA.setValue(true);
        busyA.setValue(false);
        isBusy.add(busyB);
        new Thread(() -> {
            try {
                Thread.sleep(300);
                busyB.setValue(true);
                Thread.sleep(300);
                busyB.setValue(false);
                BooleanProperty busyC = new SimpleBooleanProperty(true);
                isBusy.add(busyC);
                Thread.sleep(300);
                busyC.setValue(false);
            } catch (InterruptedException ex) {
                Logger.getLogger(VerticalHiddenScrollPaneBuilderNGTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            busyB.setValue(false);
        }).start();

//        isBusy.addListener((c) -> System.out.println("changed "+c));
    }

    /**
     * Test of build method, of class VerticalHiddenScrollPaneBuilder.
     */
//    @Test
    public void testBuild() throws InterruptedException {
        System.out.println("build");
//        BorderPane bp = new BorderPane();
        VBox ap = new VBox();
        ap.getChildren().addAll(new Label("one"), new Label("two"));

//        bp.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        ap.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
//        ap.setPrefSize(100, 200);
//        ap.setMaxHeight(400);
//        VerticalHiddenScrollPaneBuilder instance = new VerticalHiddenScrollPaneBuilder();
//        Node expResult = null;
//        ap.minHeight(Region.USE_PREF_SIZE);
//        ap.prefWidth(Double.MAX_VALUE);
//        ap.prefHeight(400);
        ap.setMaxHeight(Region.USE_PREF_SIZE);
        ap.setMaxWidth(Double.MAX_VALUE);
//        ap.setPrefSize(150, 50);
        ap.setPrefHeight(450);

        VerticalHiddenScrollPane vhsp = new VerticalHiddenScrollPane();
        vhsp.build(ap);
//        Anchorpanebu
//        Node result = instance.build(ap);

        // TODO review the generated test code and remove the default call to fail.
        CountDownLatch l = new CountDownLatch(1);

        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });

                    VBox vb = new VBox(vhsp);
                    vb.setAlignment(Pos.BOTTOM_CENTER);

                    Scene scene = new Scene(vb, 300, 300, Color.GREEN);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
    }

    @Test
    public void two() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);

        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });

                    ListView<String> v = new ListView<>(FXCollections.observableArrayList("A", "B", "C"));

                    Scene scene = new Scene(v, 300, 300, Color.GREEN);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();

    }

}
