/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.mskcc.shenkers.control.alignment.AlignmentOverlayNGTest.BoundPoly3;
import org.mskcc.shenkers.control.alignment.AlignmentOverlayNGTest.BoundPoly4;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class AlignmentViewNGTest extends Application {

    public AlignmentViewNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    @Test
    public void testSomeMethod() {
        launch(new String[0]);
    }

    AlignmentOverlay root;

    List l = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        // TODO review the generated test code and remove the default call to fail.
        List<String> sl = new ArrayList<>();
                for(int i=0; i<9; i++){
                    String s = "";
                    for(int j=0; j<99; j++){
                        s = s+(Math.random()<.4?"X":"-");
                    }
                    sl.add(s);
                }
        String[] alignment = sl.toArray(new String[0]);

        int numColumns = alignment[0].length();
        int numRows = alignment.length;
        int[] nBasesPerRow = new int[numRows];
        int[] startIndex = new int[numRows];
        int[] endIndex = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            nBasesPerRow[i] = alignment[i].replaceAll("-", "").length();
            startIndex[i] = 0;
            endIndex[i] = 0;
        }
        int b = 3;

        SplitPane sp = new SplitPane();
        for(int i=0; i<sl.size()+sl.size()-1; i++)
            sp.getItems().add(new BorderPane());
//        sp.getItems().addAll(
//                new BorderPane(new Label("a")),
//                new BorderPane(new Label("b")),
//                new BorderPane(new Label("c")),
//                new BorderPane(new Label("d")),
//                new BorderPane(new Label("e"))
//        );
        sp.setOrientation(Orientation.VERTICAL);

        root = new AlignmentOverlay();

        StackPane bor = new StackPane(sp, root);
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (alignment[j].charAt(i) != '-') {
                    endIndex[j]++;
                }
            }

            if ((i + 1) % b == 0) {

                BoundPoly4 bp4 = new AlignmentOverlayNGTest.BoundPoly4(numRows);
                List<Double> xCoords = new ArrayList<>();
                for (int j = 0; j < numRows; j++) {
//                    System.out.println(Arrays.asList(startIndex[j], endIndex[j]));
                    System.out.println(Arrays.asList(startIndex[j] * 1. / nBasesPerRow[j], endIndex[j] * 1. / nBasesPerRow[j]));
                    xCoords.add(startIndex[j] * 1. / nBasesPerRow[j]);
                    bp4.relativeXCoords.get(j).getKey().setValue(startIndex[j] * 1. / nBasesPerRow[j]);
                    startIndex[j] = endIndex[j];

                }
                for (int j = numRows - 1; j > -1; j--) {
                    xCoords.add(endIndex[j] * 1. / nBasesPerRow[j]);
                    bp4.relativeXCoords.get(j).getValue().setValue(endIndex[j] * 1. / nBasesPerRow[j]);
                }
                System.err.println("xcoords size " + xCoords.size());
                BoundPoly3 bp = new BoundPoly3(xCoords);
                bp.ypos.get(0).setValue(0);
                bp.ypos.get((2*sl.size())-1).setValue(1.);
                for (int k = 0; k < 4; k++) {
                    bp.ypos.get(k + 1).bind(sp.getDividers().get(k).positionProperty());
                }
                for (int j = 0; j < numRows; j++) {
                    Pair<DoubleProperty, DoubleProperty> pair = bp4.relativeYCoords.get(j);

                    if (j == 0) {
                    pair.getKey().setValue(0);
                    pair.getValue().bind(sp.getDividers().get(0).positionProperty());
                } else if (j == numRows - 1) {
                    pair.getKey().bind(sp.getDividers().get(sp.getDividers().size() - 1).positionProperty());
                    pair.getValue().setValue(1);
                } else {
                    pair.getKey().bind(sp.getDividers().get((j * 2) - 1).positionProperty());
                    pair.getValue().bind(sp.getDividers().get(j * 2).positionProperty());
                }
                }

                bp.flips.get(0).set(true);
                bp.flips.get(bp.flips.size() - 1).set(true);
//                bp.flips.get(1).set(true);
//                bp.flips.get(2).set(true);
                bp.getPoly().setFill(new Color(Math.random(), Math.random(), Math.random(), 1.0));
//                bp.getPoly().setStroke(new Color(0, 0, 0, 1));
                bp.xScale.bind(bor.widthProperty());
                bp.yScale.bind(bor.heightProperty());
                bp4.xScale.bind(bor.widthProperty());
                bp4.yScale.bind(bor.heightProperty());
                Color c = new Color(0, Math.random(), Math.random(), 1.0);
                bp4.getPath().setFill(c);
                bp4.getPath().setStroke(new Color(1, 1, 1, 1.0));
                
                

//                root.getChildren().add(bp.getPoly());
                root.getChildren().add(bp4.getPath());
//                l.add(bp); 
                l.add(bp4);
                System.out.println("");
            }
        }
        if (numColumns % b != 0) {
            List<Double> xCoords = new ArrayList<>();
            for (int j = 0; j < numRows; j++) {
//                    System.out.println(Arrays.asList(startIndex[j], endIndex[j]));
                System.out.println(Arrays.asList(startIndex[j] * 1. / nBasesPerRow[j], endIndex[j] * 1. / nBasesPerRow[j]));
                xCoords.add(startIndex[j] * 1. / nBasesPerRow[j]);
                startIndex[j] = endIndex[j];
            }
            for (int j = numRows - 1; j > -1; j--) {
                xCoords.add(endIndex[j] * 1. / nBasesPerRow[j]);
            }
            BoundPoly3 bp = new BoundPoly3(xCoords);
            bp.ypos.get(0).setValue(0);
            bp.ypos.get(3).setValue(1.);
            for (int k = 0; k < 4; k++) {
                bp.ypos.get(k + 1).bind(sp.getDividers().get(k).positionProperty());
            }

            bp.getPoly().setFill(new Color(Math.random(), Math.random(), Math.random(), .1));
            bp.getPoly().setStroke(new Color(0, 0, 0, 1));
            bp.xScale.bind(bor.widthProperty());
            bp.yScale.bind(bor.heightProperty());

            l.add(bp);
            root.getChildren().add(bp.getPoly());
        }
        System.out.println(root.getChildren().size() + " children");
        root.setMouseTransparent(true);
        root.setPrefWidth(300);
        sp.setPrefWidth(300);
//        bor.getChildren().addAll(root, sp, new Polygon(0,0,200,200,100,50));
//        bor.setLeft(root);D
//        bor.setRight(sp);
        Scene scene = new Scene(bor, 300, 300, Color.GRAY);
        stage.setTitle("JavaFX Scene Graph Demo");
        stage.setScene(scene);
        stage.show();
    }

}
