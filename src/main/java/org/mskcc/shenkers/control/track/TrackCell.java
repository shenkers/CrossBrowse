/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class TrackCell<T extends AbstractContext> extends ListCell<Track<T>> {

    final static Logger logger = LogManager.getLogger();
    
    public TrackCell() {
        setPadding(new Insets(2,0,2,0));
    }

    protected void updateItem(Track<T> track, boolean empty) {
        super.updateItem(track, empty);

        if (!empty && track != null) {
            
            setGraphic(new BorderPane(new Label("EMPTY")));

            
            
            Service<Pane> renderStrategy = track.getRenderStrategy();
            
            
            renderStrategy.setOnScheduled((e) -> {
                setGraphic(new BorderPane(new Label("preparing to load track")));
            });
            renderStrategy.setOnRunning((e) -> {
                setGraphic(new BorderPane(new Label("loading track")));
            });
            
            renderStrategy.setOnFailed((e) -> {
                setGraphic(new BorderPane(new Label("Failed: " + renderStrategy.getException().getMessage())));
            });
            
            renderStrategy.setOnCancelled((e) -> {
                setGraphic(new BorderPane(new Label("Cancelled")));
            });
            
            renderStrategy.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent event) {
                    System.out.println("succeeded");
                    Pane value = renderStrategy.getValue();
                    BorderPane pane = new BorderPane(value);
//                    pane.getStyleClass().add("track");
//                    pane.getStyleClass().add("track");
                    pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                    setGraphic(pane);
                }
            });
            
            track.update();
            
            
//            Thread th = new Thread(node);
//            th.setDaemon(true);
//            th.start();

                        // set the graphic for the track
//                        setGraphic(new BorderPane(node));
                        // add the context menu so the track can be configured 
            // and the view strategy changed
            MenuItem[] items = track.getViews().stream().map(
                    (View<T> v) -> {
                        MenuItem mi = new MenuItem(v.toString());
                        mi.setOnAction(
                                (ActionEvent event) -> {
                                    logger.info("view change detected");
                                    track.setView(v);
                                    track.update();
                                }
                        );
                        return mi;
                    }
            ).collect(Collectors.toList()).toArray(new MenuItem[0]);
            ContextMenu menu = new ContextMenu(items);
            setContextMenu(menu);

        }
    }
}
