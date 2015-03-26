/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.rest;

import org.mskcc.shenkers.control.track.gene.*;
import com.google.common.collect.Range;
import org.mskcc.shenkers.control.track.bigwig.*;
import org.mskcc.shenkers.control.track.bam.*;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.controlsfx.control.HiddenSidesPane;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.control.track.DomainFlippable;
import org.mskcc.shenkers.control.track.View;
import static org.mskcc.shenkers.control.track.bam.BamView1.coverage;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.view.GeneViewBuilder;
import org.mskcc.shenkers.view.GenericIntervalView;
import org.mskcc.shenkers.view.GenericStackedIntervalView;
import org.mskcc.shenkers.view.LineHistogramView;
import org.mskcc.shenkers.view.RectangleIntervalNode;
import org.mskcc.shenkers.view.SparseLineHistogramView;

/**
 *
 * @author sol
 */
public class RestIntervalView implements View<RestIntervalContext> {

    private final Logger logger = LogManager.getLogger();

    @Override
    public Task<Pane> getContent(RestIntervalContext context) {
        return context.spanProperty().getValue().map(i -> {

            String chr = i.getChr();
            int start = i.getStart();
            int end = i.getEnd();

            Task<Pane> task = new PaneTask(context, chr, start, end);

            logger.info("returning task");
            return task;
        }
        ).orElse(
                new Task<Pane>() {

                    @Override
                    protected Pane call() throws Exception {
                        return new BorderPane(new Label("coordinates not set"));
                    }
                }
        //                new BorderPane(new Label("bamview1: span not set"))
        //                new Pane()
        );
    }

    public class PaneTask<T extends Pane & DomainFlippable> extends Task<Pane> {

        RestIntervalContext context;
        RestIntervalProvider modelProvider;
        String chr;
        int start;
        int end;
        BooleanBinding flipBinding;
        Semaphore semaphore;

        public PaneTask(RestIntervalContext context, String chr, int start, int end) {
            super();
            this.context = context;
            this.modelProvider = context.readerProperty().getValue();
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Pane call() throws Exception {
            try {
                logger.info("calculating coverage for region {}:{}-{}", chr, start, end);

                logger.info("acquiring semaphore for gene model provider");
                context.acquireReader();
                logger.info("querying gene models");

                List<Pair<Integer, Integer>> modelSpans = new ArrayList<>();
                List<T> modelPanes = new ArrayList<>();

                GenomeSpan span = modelProvider.query();

                modelSpans.add(new Pair(span.getStart(), span.getEnd()));
                GenericIntervalView<T> view = new GenericIntervalView<T>(span.getStart(), span.getEnd());
                view.setData(Arrays.asList(new Pair(span.getStart(), span.getEnd())), Arrays.asList((T)new RectangleIntervalNode()));

                modelPanes.add((T)view);

                GenericStackedIntervalView stackedIntervalView = new GenericStackedIntervalView(start, end);
                stackedIntervalView.getStyleClass().add("track");
                stackedIntervalView.setData(modelSpans, modelPanes);

                class FlipBinding extends BooleanBinding {

                    private final Property<Optional<GenomeSpan>> span;

                    private FlipBinding(Property<Optional<GenomeSpan>> spanProperty) {
                        this.span = spanProperty;
                    }

                    protected boolean computeValue() {

                        if (span.getValue().isPresent()) {
                            return span.getValue().get().isNegativeStrand();
                        } else {
                            return false;
                        }
                    }

                }

                flipBinding = new FlipBinding(context.spanProperty());

                ScrollPane scrollWrapper = new ScrollPane(stackedIntervalView);

                HiddenSidesPane hsp = new HiddenSidesPane();
                BorderPane pane = new BorderPane(scrollWrapper);
//                pane.getStyleClass().add("track");
//                scrollWrapper.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
//                stackedIntervalView.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                scrollWrapper.setPadding(Insets.EMPTY);

                int tile_height = 20;
                int gap_pixels = 10;
                stackedIntervalView.flipDomainProperty().bind(flipBinding);
                stackedIntervalView.setOrientation(Orientation.VERTICAL);
                stackedIntervalView.setPrefTileHeight(tile_height);
                stackedIntervalView.prefHeightProperty().bind(Bindings.max(new SimpleDoubleProperty(tile_height + gap_pixels).multiply(Bindings.size(stackedIntervalView.getChildren())).subtract(gap_pixels), 100));
                stackedIntervalView.prefTileWidthProperty().bind(pane.widthProperty());
                stackedIntervalView.prefWidthProperty().bind(pane.widthProperty());
                stackedIntervalView.setVgap(gap_pixels);

                ScrollBar scrollBar = new ScrollBar();
                scrollBar.maxProperty().bind(scrollWrapper.vmaxProperty());
                scrollBar.minProperty().bind(scrollWrapper.vminProperty());
                scrollBar.visibleAmountProperty().bind(scrollWrapper.heightProperty().divide(stackedIntervalView.prefHeightProperty()));
                scrollBar.setOrientation(Orientation.VERTICAL);
                scrollWrapper.vvalueProperty().bindBidirectional(scrollBar.valueProperty());
//                hsp.setContent(scrollWrapper);
//                hsp.setRight(scrollBar);

                scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                return pane;
            } catch (Throwable t) {
                logger.info("task {} threw exception {}", this, t.getMessage());
                logger.info("exception: ", t);
                throw t;
            } finally {
                logger.info("{} releasing semaphore for task {}", Thread.currentThread().getName(), this);
                context.releaseReader();
                logger.info("released semaphore");
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            logger.info("Cancelling task");
        }

    }

}
