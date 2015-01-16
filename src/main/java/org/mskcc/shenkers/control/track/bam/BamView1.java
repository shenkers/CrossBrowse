/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bam;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.view.LineHistogramView;

/**
 *
 * @author sol
 */
public class BamView1 implements View<BamContext> {

    private final Logger logger = LogManager.getLogger();

////    BAMCoverageService bcs;
////    LineHistogramView lhv;
////    Pane graphic;
//    public BamView1() {
////        bcs = new BAMCoverageService();
////        lhv = new LineHistogramView();
////        graphic = lhv.getGraphic();
////
////        bcs.setOnCancelled((WorkerStateEvent event) -> {
////            lhv.clearData();
////        });
////        bcs.setOnScheduled(
////                (WorkerStateEvent e)
////                -> lhv.clearData()
////        );
//    }
    @Override
    public Task<Pane> getContent(BamContext context) {

        return context.spanProperty().getValue().map(i -> {

            if (false) {
                SamReader reader = context.readerProperty().getValue();
                int[] coverage = coverage(reader, i.getChr(), i.getStart(), i.getEnd());
                String chr = i.getChr();
                int start = i.getStart();
                int end = i.getEnd();
                PaneTask task = new PaneTask(context, chr, start, end);
//            LineHistogramView lhv = new LineHistogramView();
//                lhv.setMin(0);
//                OptionalInt max = IntStream.of(coverage).max();
//                max.ifPresent(m -> lhv.setMax(m));
//                double[] data = ArrayUtils.toPrimitive(IntStream.of(coverage).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
//            lhv.setData(data);
//                logger.info("coverage {}", IntStream.of(coverage).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toString());
            }

            SamReader reader = context.readerProperty().getValue();

            String chr = i.getChr();
            int start = i.getStart();
            int end = i.getEnd();
            Task<Pane> task = new PaneTask(context, chr, start, end);

//            LineHistogramView lhv = new LineHistogramView();
//            SamReader reader = context.readerProperty().getValue();
//
//            Pane content = get(reader, i.getChr(), i.getStart(), i.getEnd());
//            Task<Pane> task = new BAMCoverageTask(lhv, reader, i.getChr(), i.getStart(), i.getEnd());
            // start a background thread to load the track
//            logger.info("canceling");
//            bcs.cancel();
//            logger.info("reseting");
//            bcs.reset();
//
//            logger.info("reading context");
//            SamReader reader = context.readerProperty().getValue();
//            logger.info("setting region");
//            bcs.setRegion(reader, i.getChr(), i.getStart(), i.getEnd());
//
//            logger.info("starting service");
//            bcs.start();
            logger.info("returning task");
            return task;
        }
        ).orElse(
                new Task<Pane>() {

                    @Override
                    protected Pane call() throws Exception {
                        return new BorderPane(new Label("bamview1: span not set"));
                    }
                }
        //                new BorderPane(new Label("bamview1: span not set"))
        //                new Pane()
        );

    }

//    class BAMCoverageService extends Service<Void> {
//
//        SamReader SAMReader;
//        String chr;
//        int start;
//        int end;
//
//        public void setRegion(SamReader reader, String chr, int start, int end) {
//            this.SAMReader = reader;
//            this.chr = chr;
//            this.start = start;
//            this.end = end;
//        }
//
//        @Override
//        protected Task<Void> createTask() {
//            return new Task<Void>() {
//
//            };
//        }
//
//    }
    class BAMCoverageTask extends Task<Pane> {

        LineHistogramView lhv;

        SamReader SAMReader;
        String chr;
        int start;
        int end;

        public BAMCoverageTask(LineHistogramView lhv, SamReader SAMReader, String chr, int start, int end) {
            this.lhv = lhv;
            this.SAMReader = SAMReader;
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        protected Pane call() throws Exception {

            logger.info("calculating coverage for region {}:{}-{}", chr, start, end);
            SAMRecordIterator sri = SAMReader.query(chr, start, end, false);

            int length = 1 + end - start;
            int[] cov = new int[length];

            try {
                while (sri.hasNext()) {
                    SAMRecord sr = sri.next();
//                            logger.info("read: " + sr);

                    int alignmentPosition = sr.getAlignmentStart();

//                            logger.info("ap {} lastStart {}", alignmentPosition, lastStart);
//
//                            // update all positions for which we won't see any more reads
//                                for (int i = lastStart; i < alignmentPosition; i++) {
//                                    max = Math.max(max, cov[i-start]);
//                                    final int j = i;
//                                    final double MAX = Math.max(max, cov[i-start]);
//                                    logger.info("emitting: " + cov[j - start]);
//                          
//                                    );
//                                }
//                                lastStart = alignmentPosition;
//                            }
                    Cigar cigar = sr.getCigar();

                    for (int i = 0; i < cigar.numCigarElements(); i++) {
                        CigarElement cigarElement = cigar.getCigarElement(i);
                        if (cigarElement.getOperator().consumesReferenceBases()) {
                            boolean consumesReadBases = cigarElement.getOperator().consumesReadBases();
                            for (int j = 0; j < cigarElement.getLength(); j++) {
                                if (consumesReadBases && alignmentPosition >= start && alignmentPosition <= end) {
                                    cov[alignmentPosition - start]++;
                                }
                                alignmentPosition++;
                            }
                        }
                    }
                }

//                Platform.runLater(() -> {
////                            double MAX = 0;
////                            for (double d : cov) {
////                                MAX = Math.max(d, MAX);
////                                lhv.addData(d);
////                            }
//
//                    double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
//                    lhv.setData(data);
//                    IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));
//
//                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            sri.close();

            double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
            lhv.setData(data, null);
            IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));
//                    if(lastStart<end)
//                    for (int i = lastStart; i <= end; i++) {
//                                final int j = i;
//                                final double MAX = Math.max(max, cov[i]);
//                                logger.info("emitting: "+cov[j-start]);
//                                Platform.runLater(() -> {
//                                    lhv.addData(cov[j - start]);
//                                    lhv.setMax(MAX);
//                                }
//                                );
//                            }
//                    Platform.runLater(() -> {
//                        lhv.setMin(0);
//                        lhv.setMax(1);
//                    });
//                    while (true) {
//                        Platform.runLater(()
//                                -> lhv.addData(Math.random()));
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            if (isCancelled()) {
//                                break;
//                            }
//                        }
//                    }
            return lhv.getGraphic();
        }
    }

    /**
     *
     *
     * @param SAMReader
     * @param chromosome
     * @param start
     * @param end
     * @return
     */
    public static int[] coverage(SamReader SAMReader, String chromosome, int start, int end) {
        SAMRecordIterator sri = SAMReader.query(chromosome, start, end, false);

        int length = 1 + end - start;
        int[] cov = new int[length];

        while (sri.hasNext()) {
            SAMRecord sr = sri.next();

            int alignmentPosition = sr.getAlignmentStart();
            Cigar cigar = sr.getCigar();

            for (int i = 0; i < cigar.numCigarElements(); i++) {
                CigarElement cigarElement = cigar.getCigarElement(i);
                if (cigarElement.getOperator().consumesReferenceBases()) {
                    boolean consumesReadBases = cigarElement.getOperator().consumesReadBases();
                    for (int j = 0; j < cigarElement.getLength(); j++) {
                        if (consumesReadBases && alignmentPosition >= start && alignmentPosition <= end) {
                            cov[alignmentPosition - start]++;
                        }
                        alignmentPosition++;
                    }
                }
            }
        }

        sri.close();

        return cov;
    }

    public class PaneTask extends Task<Pane> {

        BamContext context;
        SamReader SAMReader;
        String chr;
        int start;
        int end;
        Semaphore semaphore;

        public PaneTask(BamContext context, String chr, int start, int end) {
            super();
            this.context = context;
            this.SAMReader = context.readerProperty().getValue();
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Pane call() throws Exception {
            LineHistogramView lhv = new LineHistogramView();

            logger.info("calculating coverage for region {}:{}-{}", chr, start, end);

            int length = 1 + end - start;
            logger.info("allocating coverage array");
            List<Double> cov = new ArrayList<>();

            logger.info("parking sam reader");
            context.acquireReader();
            logger.info("parsing sam file");
            SAMRecordIterator sri = SAMReader.query(chr, start, end, false);
            while (sri.hasNext()) {

                if (isCancelled()) {
                    logger.info("recieved cancel and terminating");
                    break;
                }

                LockSupport.parkNanos(1);

                SAMRecord sr = sri.next();
//                            logger.info("read: " + sr);

                int alignmentPosition = sr.getAlignmentStart();

                Cigar cigar = sr.getCigar();

                for (int i = 0; i < cigar.numCigarElements(); i++) {
                    CigarElement cigarElement = cigar.getCigarElement(i);
                    if (cigarElement.getOperator().consumesReferenceBases()) {
                        boolean consumesReadBases = cigarElement.getOperator().consumesReadBases();
                        for (int j = 0; j < cigarElement.getLength(); j++) {
                            if (consumesReadBases && alignmentPosition >= start && alignmentPosition <= end) {
                                int k = alignmentPosition - start;
                                while (cov.size() < k + 1) {
                                    cov.add(0.);
                                }
                                cov.set(k, cov.get(k) + 1);
                            }
                            alignmentPosition++;
                        }
                    }
                }
            }

            sri.close();
            context.releaseReader();
            logger.info("unparking sam reader");

//            double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
            while (cov.size() < end - start + 1) {
                cov.add(0.);
            }
            
            double[] data = ArrayUtils.toPrimitive(cov.toArray(new Double[0]));
            logger.info("setting data");
            lhv.setData(data, this);

            logger.info("calculating max");
            cov.stream().max((Double d1, Double d2)
                    -> Double.compare(d1, d2)
            ).ifPresent(m -> lhv.setMax(m));
//            IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));

            logger.info("returning graphic");

            return lhv.getGraphic();
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            logger.info("Cancelling task");
        }

    }

    public Pane get(SamReader SAMReader, String chr, int start, int end) {
        LineHistogramView lhv = new LineHistogramView();

        logger.info("calculating coverage for region {}:{}-{}", chr, start, end);
        SAMRecordIterator sri = SAMReader.query(chr, start, end, false);

        int length = 1 + end - start;
        int[] cov = new int[length];

        try {
            while (sri.hasNext()) {
                SAMRecord sr = sri.next();
//                            logger.info("read: " + sr);

                int alignmentPosition = sr.getAlignmentStart();

//                            logger.info("ap {} lastStart {}", alignmentPosition, lastStart);
//
//                            // update all positions for which we won't see any more reads
//                                for (int i = lastStart; i < alignmentPosition; i++) {
//                                    max = Math.max(max, cov[i-start]);
//                                    final int j = i;
//                                    final double MAX = Math.max(max, cov[i-start]);
//                                    logger.info("emitting: " + cov[j - start]);
//                          
//                                    );
//                                }
//                                lastStart = alignmentPosition;
//                            }
                Cigar cigar = sr.getCigar();

                for (int i = 0; i < cigar.numCigarElements(); i++) {
                    CigarElement cigarElement = cigar.getCigarElement(i);
                    if (cigarElement.getOperator().consumesReferenceBases()) {
                        boolean consumesReadBases = cigarElement.getOperator().consumesReadBases();
                        for (int j = 0; j < cigarElement.getLength(); j++) {
                            if (consumesReadBases && alignmentPosition >= start && alignmentPosition <= end) {
                                cov[alignmentPosition - start]++;
                            }
                            alignmentPosition++;
                        }
                    }
                }
            }

//                Platform.runLater(() -> {
////                            double MAX = 0;
////                            for (double d : cov) {
////                                MAX = Math.max(d, MAX);
////                                lhv.addData(d);
////                            }
//
//                    double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
//                    lhv.setData(data);
//                    IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));
//
//                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        sri.close();

        double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
        lhv.setData(data, null);
        IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));
//                    if(lastStart<end)
//                    for (int i = lastStart; i <= end; i++) {
//                                final int j = i;
//                                final double MAX = Math.max(max, cov[i]);
//                                logger.info("emitting: "+cov[j-start]);
//                                Platform.runLater(() -> {
//                                    lhv.addData(cov[j - start]);
//                                    lhv.setMax(MAX);
//                                }
//                                );
//                            }
//                    Platform.runLater(() -> {
//                        lhv.setMin(0);
//                        lhv.setMax(1);
//                    });
//                    while (true) {
//                        Platform.runLater(()
//                                -> lhv.addData(Math.random()));
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            if (isCancelled()) {
//                                break;
//                            }
//                        }
//                    }
        return lhv.getGraphic();
    }

}
