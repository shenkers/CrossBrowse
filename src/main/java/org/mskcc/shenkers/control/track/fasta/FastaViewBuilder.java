/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.control.track.bigwig.BigWigZoomView;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class FastaViewBuilder implements View<FastaContext> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Task<Pane> getContent(FastaContext context) {
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

    class ComplementSetter extends CheckMenuItem {

        public ComplementSetter() {
            super("Complement sequence");
        }
    }

    @Override
    public Collection<MenuItem> getMenuItems(FastaContext context) {
        ComplementSetter complementer = new ComplementSetter();
        complementer.selectedProperty().setValue(context.getComplement().get());
        complementer.setOnAction(e -> context.getComplement().setValue(complementer.isSelected()));
        return Arrays.asList(complementer);
    }

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

    public class PaneTask extends Task<Pane> {

        String chr;
        int start;
        int end;
        BooleanBinding flipBinding;
        private final FastaContext context;

        public PaneTask(FastaContext context, String chr, int start, int end) {
            super();
            this.context = context;
            this.chr = chr;
            this.start = start;
            this.end = end;
            flipBinding = new FlipBinding(context.spanProperty());
        }

        @Override
        protected Pane call() throws Exception {
            boolean haveReader = false;
            if (end - start <= 2000) {
                logger.info("synchronizing on ifsf");
                synchronized (context.getFasta()) {
                    try {
                        logger.info("getting for region {}:{}-{}", chr, start, end);

                        logger.info("acquiring semaphore for reader");
                        while (!haveReader) {
                            haveReader = context.acquireReader();
                            if (!haveReader) {
                                logger.info("waiting...");
                                Thread.yield();
                            }
                        }
                        logger.info("parsing");

//            double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
                        logger.info("setting data");

                        FastaView view = new FastaView();
                        view.flip.bind(flipBinding);
                        view.complement.bind(context.getComplement());

                        // wrapping in future will prevent the Service restart()
                        // from interrupting the filechannel operations.
                        Future<String> readSeq = context.ioService.submit(() -> {
                            if (Thread.interrupted()) {
                                logger.info("thread was previously interrupted");
                            }
                            byte[] sequence = context.getFasta().getSubsequenceAt(chr, start, end).getBases();
                            StringBuilder seqBuffer = new StringBuilder();

                            for (int i = 0; i < sequence.length; i++) {
                                seqBuffer.append((char) sequence[i]);
                            }

                            String seq = seqBuffer.toString();
                            return seq;
                        });

                        view.setSequence(start % 3, readSeq.get());

                        logger.info("returning");

                        return view;
                    } catch (Throwable t) {
                        logger.info("task {} threw exception {}", this, t.getMessage());
                        logger.info("exception: ", t);
                        throw t;
                    } finally {
                        if (haveReader) {
                            logger.info("{} releasing semaphore for task {}", Thread.currentThread().getName(), this);

                            context.releaseReader();
                            logger.info("released semaphore");
                        } else {
                            logger.info("don't have reader, returning");
                        }
                    }
                }
            } else {
                return new BorderPane(new Label("Zoom in to see sequence"));
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            logger.info("Cancelling task");
        }

    }

}
