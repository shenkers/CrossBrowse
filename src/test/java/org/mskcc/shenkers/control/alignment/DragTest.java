/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.liftover.ChainConverter;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.util.AbstractAsyncWriter;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.BasicFeature;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.Feature;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.util.TabixUtils;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.samtools.liftover.AlignmentContext;
import htsjdk.samtools.liftover.GFFAlignmentContextCodec;
import htsjdk.samtools.liftover.GFFAlignmentContextEncoder;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.LocationAware;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.TribbleIndexedFeatureReader;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.readers.PositionalBufferedStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.CharacterStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.controlsfx.control.HiddenSidesPane;
import org.fxmisc.easybind.EasyBind;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

/**
 *
 * @author sol
 */
public class DragTest {

    public DragTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("starting JFX");
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @AfterClass
    public static void tearDownClass() {
        Platform.exit();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    @Test
    public void testHidden() throws InterruptedException {
        System.out.println("testHidden");
        Pane p = new Pane();
        IntegerProperty spanWidth = new SimpleIntegerProperty(3);
        BooleanProperty dragDetected = new SimpleBooleanProperty();
        class DragEvent {

            private final double startX;
            private final double endX;

            public DragEvent(double startX, double endX) {
                this.startX = startX;
                this.endX = endX;
            }

        }
        class ShiftEvent {

            int delta;

            public ShiftEvent(int delta) {
                this.delta = delta;
            }

        }
        EventSource<DragEvent> source = new EventSource<>();

        EventStream<ShiftEvent> shiftEvents = source.map(e -> new ShiftEvent(
                (int) Math.floor(e.endX * spanWidth.get())
                - (int) Math.floor(e.startX * spanWidth.get())
        ));
        shiftEvents.subscribe(de -> logger.printf(Level.INFO, "subscriber received shift of  %d", de.delta));
        source.subscribe(de -> logger.printf(Level.INFO, "subscriber received drag from %.2f to %.2f", de.startX, de.endX));

        IntegerProperty coord = new SimpleIntegerProperty(0);
        shiftEvents.subscribe(s -> coord.set(coord.get() + s.delta));

        coord.addListener((a, b, c) -> {
            logger.info("coord change to {} detected", c);
        });

        DoubleProperty relativeX = new SimpleDoubleProperty();
        p.addEventHandler(MouseEvent.DRAG_DETECTED, e -> {

            dragDetected.set(true);
            double x = e.getX();
            relativeX.set(x / p.getWidth());
            logger.info("detected drag at x {} % {}", x, x / p.getWidth());
        }
        );

        p.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
//            logger.info("dragging");
//                dragDetected.set(true);
        }
        );

        p.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (dragDetected.get()) {
                double x = e.getX();
                logger.info("drag released at x {} % {}", x, x / p.getWidth());
                dragDetected.set(false);
                source.push(new DragEvent(relativeX.get(), x / p.getWidth()));
            }
        }
        );

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {
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
        l.await();
        Thread.sleep(1000);
    }

}
