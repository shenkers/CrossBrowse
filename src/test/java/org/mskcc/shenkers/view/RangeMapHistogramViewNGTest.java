/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.broad.igv.bbfile.BBFileReader;
import org.mskcc.shenkers.control.track.bigwig.BigWigUtil;
import org.mskcc.shenkers.data.interval.RangeTools;
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
public class RangeMapHistogramViewNGTest {

    public RangeMapHistogramViewNGTest() {
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @Test
    public void testGetGraphic() throws InterruptedException, IOException {
        String title = "";
        CountDownLatch l = new CountDownLatch(1);
        BBFileReader bbfr = new BBFileReader("/home/sol/lailab/sol/mel_yak_vir/bigwig/M/H.plus.bw");

        RangeMap<Integer, Double> values = BigWigUtil.values(bbfr, "2LHet", 322402, 322441, 0);

        Range<Integer> view = Range.closed(322402, 322441);
        values.asMapOfRanges().entrySet().stream().forEach(e -> System.out.println(String.format("%s %f", e.getKey(), e.getValue())));
        RangeMapHistogramView v = new RangeMapHistogramView();

        Platform.runLater(
                () -> {
                    v.setData(view, values);
                    v.setMax(15);
                    v.setFlipRange(true);
                    v.setFlipDomain(false);
                    Pane graphic = v.getGraphic();
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    Scene scene = new Scene(graphic, 300, 300, Color.GRAY);
                    stage.setTitle(title);
                    stage.setScene(scene);
                    stage.show();
                });
        l.await();
        v.clearData();
    }
}
