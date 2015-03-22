/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.liftover.ChainParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.mskcc.shenkers.control.alignment.AlignmentOverlay;
import org.mskcc.shenkers.control.alignment.AlignmentOverlayNGTest;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.alignment.AlignmentWeaver;
import org.mskcc.shenkers.control.alignment.CurvedOverlayPath;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import org.mskcc.shenkers.model.datatypes.Genome;
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
public class ChainAlignmentOverlayNGTest {

    static Logger logger = LogManager.getLogger();

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void t() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    Genome g1 = new Genome("dm3", "mel");
                    Genome g2 = new Genome("yak", "yak");

                    ChainGFFAlignmentSource1 cgas = null;
                    try {
                        cgas = new ChainGFFAlignmentSource1("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain", false);
                    } catch (IOException ex) {
                        logger.info(ex);
                    }
                    GenomeSpan from = new GenomeSpan("chrX", 145992655, 145992771, false);
                    GenomeSpan to = new GenomeSpan("chrX", 66776726, 66776806, false);

//              GenomeSpan melInterval = new GenomeSpan("3L", 74548, 74554, false);
//            GenomeSpan yakInterval = new GenomeSpan("3L", 42084, 42097, false);
//            GenomeSpan melInterval = new GenomeSpan("3L", 74548, 74551, false);
//            GenomeSpan yakInterval = new GenomeSpan("3L", 42085, 42088, false);
//        GenomeSpan melInterval = new GenomeSpan("2R", 12743706, 12747879, false);
//        GenomeSpan yakInterval = new GenomeSpan("2R", 16228220, 16231888, false);
//        GenomeSpan virInterval = new GenomeSpan("scaffold_12875", 10639626, 10643889, false);
                    Map<Genome, GenomeSpan> displayedSpans = new HashMap<>();
                    displayedSpans.put(g1, from);
                    displayedSpans.put(g2, to);

                    int basesPerColumn = 1;
                    logger.info("constructing alignment sources");
                    Map<Pair<Genome, Genome>, AlignmentSource> alignmentSources = new HashMap<>();
                    alignmentSources.put(new Pair<>(g1, g2), cgas);

                    ChainAlignmentOverlay instance = new ChainAlignmentOverlay(alignmentSources);

                    List<Map<Genome, Pair<Double, Double>>> alignmentColumnsRelativeX = instance.getAlignmentColumnsRelativeX(displayedSpans, basesPerColumn);

                    List<Genome> gOrder = Arrays.asList(g1, g2);
//        List<Genome> gOrder = Arrays.asList(g1, g2);

                    SplitPane sp = new SplitPane();
                    sp.getItems().addAll(
                            new BorderPane(new Label(g1.toString())),
                            new BorderPane(new Label(g1.toString() + " to " + g2.toString())),
                            new BorderPane(new Label(g2.toString()))
                    );

                    dividerPositionProperties = sp.getDividers().stream().map(d -> d.positionProperty()).collect(Collectors.toList());

                    sp.setOrientation(Orientation.VERTICAL);

                    AlignmentOverlay overlay = new AlignmentOverlay();
                    overlay.setMouseTransparent(true);
                    StackPane stackPane = new StackPane(sp, overlay);

                    Map<Genome, BooleanProperty> genomeFlipped = gOrder.stream().collect(Collectors.toMap(g -> g, g -> new SimpleBooleanProperty(false)));

                    
                    List<Node> overlayPaths = instance.getOverlayPaths(gOrder, displayedSpans, gOrder.stream().collect(Collectors.toMap(g -> g, g -> genomeFlipped.get(g))), basesPerColumn, dividerPositionProperties, stackPane.widthProperty(), stackPane.heightProperty());
                    overlay.getChildren().setAll(overlayPaths);

                    Scene scene = new Scene(stackPane);

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();
                }
        );
        l.await();
    }

//    /**
//     * Test of getAlignmentColumnsRelativeX method, of class ChainAlignmentOverlay.
//     */
//    @Test
//    public void testGetAlignmentColumnsRelativeX() {
//        System.out.println("getAlignmentColumnsRelativeX");
//        Map<Genome, GenomeSpan> displayedSpans = null;
//        int basesPerColumn = 0;
//        ChainAlignmentOverlay instance = null;
//        List expResult = null;
//        List result = instance.getAlignmentColumnsRelativeX(displayedSpans, basesPerColumn);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
    public void testAlignmentColumnsOverlay() {
//        launch();
    }

    @Test

    public void testMap() {

        ObservableMap<Object, Object> observableHashMap = FXCollections.observableHashMap();
        observableHashMap.addListener(new MapChangeListener<Object, Object>() {

            @Override
            public void onChanged(MapChangeListener.Change<? extends Object, ? extends Object> change) {
                logger.info("map changed");
            }
        });
//        logger.info("contents {}",observableHashMap);
        observableHashMap.put("a", "b");
//        logger.info("contents {}",observableHashMap);
    }
    List<DoubleProperty> dividerPositionProperties;

//    @Override
    public void start(Stage primaryStage) throws Exception {

//        {
//
//            ChainParser parser = new ChainParser(new File("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droYak3.net.chain"));
//            List<LocalAlignment> chainIntersections = parser.getChainIntersections(new GenomeSpan("2R", 12743706, 12747879, false));
//            for(LocalAlignment l : chainIntersections){
//                logger.info("DEK {} {} {}",l.getToSequenceName(),l.getToBlock(0), l.getToBlock(l.getNBlocks()-1));
//            }
//        }
//        
//        {
//
//            ChainParser parser = new ChainParser(new File("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droVir3.net.chain"));
//            List<LocalAlignment> chainIntersections = parser.getChainIntersections(new GenomeSpan("2R", 12743706, 12747879, false));
//            for(LocalAlignment l : chainIntersections){
//                logger.info("DEK {} {} {}",l.getToSequenceName(),l.getToBlock(0), l.getToBlock(l.getNBlocks()-1));
//            }
//        }
//        System.exit(0);
        Genome g1 = new Genome("dm3", "mel");
        Genome g2 = new Genome("yak", "yak");
        Genome g3 = new Genome("vir", "vir");

        GenomeSpan melInterval = new GenomeSpan("3L", 74548, 74869, false);
//        GenomeSpan yakInterval = new GenomeSpan("3L", 42088, 42438, false);
        GenomeSpan yakInterval = new GenomeSpan("3L", 41808, 42438, false);
        GenomeSpan virInterval = new GenomeSpan("scaffold_13049", 2917506, 2917933, false);

//              GenomeSpan melInterval = new GenomeSpan("3L", 74548, 74554, false);
//            GenomeSpan yakInterval = new GenomeSpan("3L", 42084, 42097, false);
//            GenomeSpan melInterval = new GenomeSpan("3L", 74548, 74551, false);
//            GenomeSpan yakInterval = new GenomeSpan("3L", 42085, 42088, false);
//        GenomeSpan melInterval = new GenomeSpan("2R", 12743706, 12747879, false);
//        GenomeSpan yakInterval = new GenomeSpan("2R", 16228220, 16231888, false);
//        GenomeSpan virInterval = new GenomeSpan("scaffold_12875", 10639626, 10643889, false);
        Map<Genome, GenomeSpan> displayedSpans = new HashMap<>();
        displayedSpans.put(g1, melInterval);
        displayedSpans.put(g2, yakInterval);
        displayedSpans.put(g3, virInterval);

        int basesPerColumn = 20;
        logger.info("constructing alignment sources");
        Map<Pair<Genome, Genome>, AlignmentSource> alignmentSources = new HashMap<>();
        alignmentSources.put(new Pair<>(g1, g2), new ChainAlignmentSource("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droYak3.net.chain"));
        alignmentSources.put(new Pair<>(g1, g3), new ChainAlignmentSource("/home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droVir3.net.chain"));

        ChainAlignmentOverlay instance = new ChainAlignmentOverlay(alignmentSources);

        List<Map<Genome, Pair<Double, Double>>> alignmentColumnsRelativeX = instance.getAlignmentColumnsRelativeX(displayedSpans, basesPerColumn);

        List<Genome> gOrder = Arrays.asList(g1, g2, g3);
//        List<Genome> gOrder = Arrays.asList(g1, g2);

        SplitPane sp = new SplitPane();
        sp.getItems().addAll(
                new BorderPane(new Label(g1.toString())),
                new BorderPane(new Label(g1.toString() + " to " + g2.toString())),
                new BorderPane(new Label(g2.toString())),
                new BorderPane(new Label(g2.toString() + " to " + g3.toString())),
                new BorderPane(new Label(g3.toString()))
        );

        dividerPositionProperties = sp.getDividers().stream().map(d -> d.positionProperty()).collect(Collectors.toList());

        sp.setOrientation(Orientation.VERTICAL);

        AlignmentOverlay overlay = new AlignmentOverlay();
        overlay.setMouseTransparent(true);
        StackPane stackPane = new StackPane(sp, overlay);

        Map<Genome, SimpleBooleanProperty> genomeFlipped = gOrder.stream().collect(Collectors.toMap(g -> g, g -> new SimpleBooleanProperty(false)));

        List<BooleanProperty> flips = gOrder.stream().map(g -> genomeFlipped.get(g)).collect(Collectors.toList());

//        genomeFlipped.get(g2).setValue(true);
        genomeFlipped.get(g3).setValue(true);

        for (Map<Genome, Pair<Double, Double>> column : alignmentColumnsRelativeX) {
            logger.info("{}", column);

            boolean allRowsAligned = true;
            for (int i = 0; i < gOrder.size(); i++) {
                Genome g = gOrder.get(i);
                Pair<Double, Double> calculatedRelativeX = column.get(g);
                allRowsAligned &= calculatedRelativeX.getValue() - calculatedRelativeX.getKey() > 0;
            }

            if (!allRowsAligned) {
                continue;
            }

            CurvedOverlayPath cop = new CurvedOverlayPath(gOrder.size());

            for (int i = 0; i < gOrder.size(); i++) {
                cop.getGenomeFlipped().get(i).bind(flips.get(i));
            }

            overlay.getChildren().add(cop.getPath());
            cop.getXScale().bind(stackPane.widthProperty());
            cop.getYScale().bind(stackPane.heightProperty());

//            cop.getPath().setF
//            cop.getPath().setStroke(new Color(0, 0, 0, 0));
//            cop.getPath().setStrokeWidth(3.);
            cop.getPath().setFill(new Color(Math.random(), Math.random(), Math.random(), .1));
            for (int i = 0; i < gOrder.size(); i++) {
                Genome g = gOrder.get(i);
                Pair<Double, Double> calculatedRelativeX = column.get(g);
                logger.info("adding column {} {}", g, calculatedRelativeX);
                Pair<DoubleProperty, DoubleProperty> relativeX = cop.getRelativeXCoords().get(i);
                relativeX.getKey().setValue(calculatedRelativeX.getKey());
                relativeX.getValue().setValue(calculatedRelativeX.getValue());
                Pair<DoubleProperty, DoubleProperty> relativeY = cop.getRelativeYCoords().get(i);

                if (i == 0) {
                    logger.info("case 1 : {} i={}", g, i);
                    relativeY.getKey().setValue(0);
                    relativeY.getValue().bind(dividerPositionProperties.get(0));
                } else if (i == gOrder.size() - 1) {
                    logger.info("case 2 : {} i={}", g, i);
                    relativeY.getKey().bind(dividerPositionProperties.get(dividerPositionProperties.size() - 1));
                    relativeY.getValue().setValue(1);
                } else {
                    logger.info("case 3 : {} i={}", g, i);
                    relativeY.getKey().bind(dividerPositionProperties.get((i * 2) - 1));
                    relativeY.getValue().bind(dividerPositionProperties.get(i * 2));
                }
            }
            logger.info(cop.getPath().getElements());

        }

        Scene scene = new Scene(stackPane);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
