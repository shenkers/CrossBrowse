/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.interval;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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

    /**
     * Test of getContent method, of class IntervalView.
     */
    @Test
    public void testGetContent() throws IOException, InterruptedException {
        System.out.println("getContent");

        IntervalContext context = new IntervalContext(new BEDIntervalProvider("/home/sol/annotator/dm3_PA_sites.bed", false, false));
        context.spanProperty().setValue(Optional.of(new GenomeSpan("2L", 52222, 52822, false)));

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    IntervalView instance = new IntervalView();
                    Task<Pane> content = instance.getContent(context);
                    content.setOnSucceeded(ev -> {

                        try {
                            Stage stage = new Stage();
                            stage.setOnHidden(e -> {
                                l.countDown();
                            });
                            Scene scene = new Scene(content.get(), 300, 300, Color.GRAY);
                            stage.setTitle("StackedPaneTest");
                            stage.setScene(scene);
                            stage.show();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(IntervalViewNGTest.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(IntervalViewNGTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    new Thread(content).start();
                }
        );
        l.await();
    }

}
