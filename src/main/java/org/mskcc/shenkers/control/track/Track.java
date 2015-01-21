/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.List;
import java.util.Optional;
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

    public Track(T context, List<View<T>> availableViews) {
        this.dataContext = context;
        this.availableViews = availableViews;
        this.displayedView = new SimpleObjectProperty(new View<T>() {

            @Override
            public Task<Pane> getContent(T context) {

                Label label = new Label("EMPTY");
//                  MenuItem[] items = availableViews.stream().map((View<T> t) ->{
//                    MenuItem mi = new MenuItem(t.toString(), t.getContent(dataContext));
//                    mi.setOnAction(
//                            (ActionEvent event) -> {
//                            setView(t);
//                        }
//                    );
//                    return mi;
//                }).collect(Collectors.toList()).toArray(new MenuItem[0]);
//                
//                ContextMenu menu = new ContextMenu(items);
//                label.setContextMenu(menu);
                BorderPane p = new BorderPane();
                MonadicBinding<String> map = EasyBind.map(p.widthProperty(), v -> String.format("%.1f", v));

                label.textProperty().bind(map);
                p.setCenter(label);
                
                class initialContent extends Task<Pane>{
                        @Override
                        protected Pane call() throws Exception {
                            return p;
                        }
                    }

//                Service<Pane> task = new Service<Pane>() {
//                    
//                    @Override
//                    protected Task<Pane> createTask() {
//                        return new initialContent();
//                    }
//
//                };

                return new initialContent();
            }
        });
        
        renderStrategy = new RenderStrategy<>();
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
