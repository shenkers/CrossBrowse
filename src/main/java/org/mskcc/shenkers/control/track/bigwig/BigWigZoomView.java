/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bigwig;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import org.mskcc.shenkers.control.track.bam.*;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.control.track.View;
import static org.mskcc.shenkers.control.track.bam.BamView1.coverage;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.view.LineHistogramView;
import org.mskcc.shenkers.view.RangeMapHistogramView;
import org.mskcc.shenkers.view.SparseLineHistogramView;

/**
 *
 * @author sol
 */
public class BigWigZoomView implements View<BigWigContext> {

    private final Logger logger = LogManager.getLogger();

    @Override
    public Task<Pane> getContent(BigWigContext context) {
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

    public class PaneTask extends Task<Pane> {

        BigWigContext context;
        BBFileReader bbfr;
        List<Double> zoomFeatureDensities;
        String chr;
        int start;
        int end;
        BooleanBinding flipBinding;
        Semaphore semaphore;

        public PaneTask(BigWigContext context, String chr, int start, int end) {
            super();
            this.context = context;
            this.bbfr = context.readerProperty().getValue();
            zoomFeatureDensities = BigWigUtil.zoomFeatureDensities(bbfr);
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Pane call() throws Exception {
            SAMRecordIterator sri = null;
            try {
                logger.info("calculating coverage for region {}:{}-{}", chr, start, end);

                
                logger.info("acquiring semaphore for bigwig reader");
                context.acquireReader();
                logger.info("parsing bigwig file");

                int nPixels = 1000;
                int zoom = BigWigUtil.selectZoomLevel(nPixels, end - start + 1, zoomFeatureDensities);

                RangeMap<Integer, Double> values = BigWigUtil.values(bbfr, chr, start, end, zoom);
                RangeMapHistogramView lhv = new RangeMapHistogramView();

//            double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
                logger.info("setting data");

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

                lhv.flipDomainProperty().bind(flipBinding);

                lhv.setData(Range.closed(start, end), values);

                logger.info("calculating max");
                values.asMapOfRanges().values().stream()
                        .max(Double::compare)
                        .ifPresent(m -> lhv.setMax(m));
//            IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));

                logger.info("returning graphic");

                return lhv.getGraphic();
            } catch (Throwable t) {
                logger.info("task {} threw exception {}", this, t.getMessage());
                logger.info("exception: ", t);
                throw t;
            } finally {

                if (sri != null) {
                    sri.close();
                }
                logger.info("{} releasing semaphore for task {}", Thread.currentThread().getName(), this);
                context.releaseReader();
                logger.info("released bigwig reader semaphore");
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            logger.info("Cancelling task");
        }

    }

}
