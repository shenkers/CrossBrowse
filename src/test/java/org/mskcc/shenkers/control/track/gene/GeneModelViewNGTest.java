/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import htsjdk.tribble.CloseableTribbleIterator;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
public class GeneModelViewNGTest {
    
    public GeneModelViewNGTest() {
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

    /**
     * Test of getContent method, of class GeneModelView.
     */
    @Test
    public void testGetContent() throws IOException, InterruptedException, ExecutionException {
        System.out.println("getContent");
        GeneModelContext context = new GeneModelContext(new GTFGeneModelProvider("/home/sol/data/gene_models/Drosophila_melanogaster.BDGP5.69.gtf", false, false));
          int start = 1058118;
        int end = 1103042;
        context.setSpan(new GenomeSpan("chr3R", start, end, false));
        GeneModelView instance = new GeneModelView();
        
        Task<Pane> result = instance.getContent(context);
        result.run();
        Pane get = result.get();
        
        
        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    
                    Scene scene = new Scene(get, 300, 300, Color.GRAY);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        
    }
    
}
