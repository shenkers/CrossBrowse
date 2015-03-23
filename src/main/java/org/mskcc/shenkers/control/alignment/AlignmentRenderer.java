/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.chain.ChainAlignmentOverlay;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class AlignmentRenderer extends Service<List<Node>> {

    private static final Logger logger = LogManager.getLogger();

    Pane parent;

    ModelSingleton model;
    ChainAlignmentOverlay cao;
    ReadOnlyDoubleProperty widthProperty;
    ReadOnlyDoubleProperty heightProperty;
    List<DoubleProperty> dividerPositions;

    public AlignmentRenderer(Pane parent, ModelSingleton model, ChainAlignmentOverlay cao, List<DoubleProperty> dividerPositions, ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty heightProperty) {
        super();
        this.parent = parent;
        this.model = model;
        this.cao = cao;
        this.dividerPositions = dividerPositions;
        this.widthProperty = widthProperty;
        this.heightProperty = heightProperty;
        
        setOnScheduled((e) -> {
            logger.info("preparing to load track");
        });
        setOnRunning((e) -> {
            logger.info("loading track");
        });

        setOnFailed((e) -> {
            logger.info("failed ", getException());
        });

        setOnCancelled((e) -> {
            logger.info("cancelled");
            parent.getChildren().clear();
        });

        setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                logger.info("succeeded");
                List<Node> value = getValue();
                parent.getChildren().setAll(value);
            }
        });

    }

    @Override
    protected Task<List<Node>> createTask() {
        return new Task<List<Node>>() {

            @Override
            protected List<Node> call() throws Exception {
                try {
                    boolean allSpansSet = model.getGenomes().stream().allMatch(g -> model.getSpan(g).getValue().isPresent());
                    if (allSpansSet) {
                        logger.info("all spans set, building alignment component");
                        
                        class flipBinding extends BooleanBinding implements ObservableValue<Boolean> {

                            ObservableValue<Optional<GenomeSpan>> g;

                            public flipBinding(ObservableValue<Optional<GenomeSpan>> g) {
                                super.bind(g);
                                this.g = g;
                            }

                            @Override
                            protected boolean computeValue() {
                                logger.info("recomputing flip binding {} isneg {}", g, g.getValue().get().isNegativeStrand());
                                return g.getValue().get().isNegativeStrand();
                            }

                        }

                        Map<Genome, ObservableValue<? extends Boolean>> flips = model.getGenomes().stream().collect(Collectors.toMap(g -> g, g -> new flipBinding(model.getSpan(g))));
                        Map<Genome, GenomeSpan> spans = model.getGenomes().stream().collect(Collectors.toMap(g -> g, g -> model.getSpan(g).getValue().get()));

                        return cao.getOverlayPaths(model.getGenomes(), spans, flips, 100, dividerPositions, widthProperty, heightProperty);
                    } else {
                        logger.info("not all spans set, not building alignment");
                    }
                } catch (Exception e) {
                    logger.info("caught alignment error");
                    logger.info("message: ", e);
                }
                logger.info("returning empty list");
                return new ArrayList<>();
            }
        };
    }

}
