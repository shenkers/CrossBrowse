/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import static org.testng.Assert.*;

/**
 *
 * @author sol
 */
public class AlignmentOverlayNGTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Polygon p = new Polygon(.2, 0., .3, 0.,.6,.5, 1.2, 1., 1.1, 1.,.5,.5);
        Polygon p1 = new Polygon(.2, 0., .3, 0.,.6,.5, .5,.5);
        Polygon p2 = new Polygon(.6,.5, .2,1.,.1,1., .5,.5);
     
        AlignmentOverlay root = new AlignmentOverlay();
        
        List<Double> weights = FXCollections.observableArrayList(0.,.5,1.);
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
