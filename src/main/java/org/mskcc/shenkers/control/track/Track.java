/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class Track<T extends AbstractContext> {
    
    Logger logger = LogManager.getLogger();

    T dataContext;
    
    RenderStrategy<T> renderStrategy;
            
    Property<View<T>> displayedView;

    List<View<T>> availableViews;

    Property<Optional<GenomeSpan>> span;
    
    Executor executor;

    public Track(T context, List<View<T>> availableViews, Executor executor) {
        this.executor = executor;
        this.dataContext = context;
        this.availableViews = availableViews;
        this.displayedView = new SimpleObjectProperty(availableViews.isEmpty() ? new View<T>() {

            @Override
            public Task<Pane> getContent(T context) {
                
                class initialContent extends Task<Pane>{
                        @Override
                        protected Pane call() throws Exception {
                            return new BorderPane(new Label("No view available for "+dataContext.getClass().getName()));
                        }
                    }

                return new initialContent();
            }
        } : availableViews.get(0));
        
        renderStrategy = new RenderStrategy<>();
        renderStrategy.setExecutor(executor);
        renderStrategy.setView(displayedView.getValue());

        span = new SimpleObjectProperty<>(Optional.empty());
        context.spanProperty().bind(span);
    }

    public void setView(View<T> v) {
        renderStrategy.setView(v);
        displayedView.setValue(v);
    }

    public Property<View<T>> getView() {
        return displayedView;
    }

    public List<View<T>> getViews() {
        return availableViews;
    }

    public Property<Optional<GenomeSpan>> getSpan() {
        return span;
    }

    public String getName() {
        return this.toString();
    }

    public void update() {
        logger.info("restarting the render service");
        renderStrategy.setContext(dataContext);
        renderStrategy.restart();
    }
    
    public Service<Pane> getRenderStrategy(){
        return renderStrategy;
    }
}
