/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.CoordinateChange;
import org.mskcc.shenkers.model.CoordinateChangeEvent;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.reactfx.EventSource;

/**
 * FXML Controller class
 *
 * @author sol
 */
public class CoordinateInputController implements Initializable {

    Logger logger = LogManager.getLogger();

    @FXML
    private FlowPane child;

    @FXML
    Button goButton;

    @FXML
    TextField coordinateField;

    @FXML
    TextField currentCoordinate;

    @FXML
    ComboBox<Genome> genomeSelected;

    EventSource<CoordinateChangeEvent> coordinateChangeEvents;

    Pattern coordinatePattern;

    ModelSingleton model;

    @FXML
    public void flipSelectedSpan(ActionEvent event) {
        logger.info("processing flip");
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        if (g != null) {

            ObservableValue<Optional<GenomeSpan>> span = model.getSpan(g);
            span.getValue().ifPresent(s -> {
                model.setSpan(g, Optional.of(new GenomeSpan(s.getChr(), s.getStart(), s.getEnd(), !s.isNegativeStrand())));
            });

        }
    }

    @FXML
    public void parseAndUpdateCoordinates(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        if (g != null) {
            String text = coordinateField.getText();
            Matcher mat = coordinatePattern.matcher(text);

            System.out.println(event.getEventType());

            if (mat.find()) {
                // set coordinate in model
                System.out.println("match");

                String chr = mat.group(1);
                int start = Integer.parseInt(mat.group(2));
                int end = mat.group(4) == null ? start : Integer.parseInt(mat.group(4));
                boolean isNegativeStrand = mat.group(6) == null ? false : mat.group(6).equals("-");

                System.out.printf("%s %d %d %b\n", chr, start, end, isNegativeStrand);

                GenomeSpan span = new GenomeSpan(chr, start, end, isNegativeStrand);

                model.setSpan(g, Optional.of(span));
            } else {
                // notify error
                System.out.println("no-match");
            }
        }
    }

    @FXML
    public void zoomIn(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        model.getSpan(g).getValue().ifPresent(span -> {
            int width = span.getEnd() - span.getStart() + 1;
            if (width > 1) {
                int newWidth = (int) (.75 * width);
                int middle = (span.getStart() + span.getEnd()) / 2;
                GenomeSpan s = new GenomeSpan(span.getChr(), middle - (newWidth / 2), middle + (newWidth / 2), span.isNegativeStrand());
                model.setSpan(g, Optional.of(s));
            }
        });
    }

    @FXML
    public void zoomOut(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        model.getSpan(g).getValue().ifPresent(span -> {
            int width = span.getEnd() - span.getStart() + 1;
            if (width > 1) {
                int newWidth = (int) Math.ceil(4. / 3 * width);
                int middle = (span.getStart() + span.getEnd()) / 2;
                GenomeSpan s = new GenomeSpan(span.getChr(), middle - (newWidth / 2), middle + (newWidth / 2), span.isNegativeStrand());
                model.setSpan(g, Optional.of(s));
            }
        });
    }

    @FXML
    public void shiftLeft(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        model.getSpan(g).getValue().ifPresent(span -> {
            int width = span.getEnd() - span.getStart() + 1;
            if (width > 1) {
                int shift = (int) Math.ceil(.1 * width);
                GenomeSpan s = new GenomeSpan(span.getChr(), span.getStart() - shift, span.getEnd() - shift, span.isNegativeStrand());
                model.setSpan(g, Optional.of(s));
            }
        });
    }

    @FXML
    public void shiftRight(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        model.getSpan(g).getValue().ifPresent(span -> {
            int width = span.getEnd() - span.getStart() + 1;
            if (width > 1) {
                int shift = (int) Math.ceil(.1 * width);
                GenomeSpan s = new GenomeSpan(span.getChr(), span.getStart() + shift, span.getEnd() + shift, span.isNegativeStrand());
                model.setSpan(g, Optional.of(s));
            }
        });
    }

    @Inject
    public void setModel(ModelSingleton model) {
        logger.info("injecting model");
        this.model = model;
    }

    @Inject
    public void setCoordinateChangeEvents(@CoordinateChange EventSource<CoordinateChangeEvent> coordinateChangeEvents) {
        logger.info("injecting coordinate change events");
        this.coordinateChangeEvents = coordinateChangeEvents;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        coordinatePattern = Pattern.compile("^([^:]+):([0-9]+)(-([0-9]+))?(:([+-]))?$");

        Callback<ListView<Genome>, ListCell<Genome>> s = new Callback<ListView<Genome>, ListCell<Genome>>() {

            @Override
            public ListCell<Genome> call(ListView<Genome> param) {
                return new ListCell<Genome>() {

                    @Override
                    protected void updateItem(Genome item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(String.format("%s (%s)", item.getDescription(), item.getId()));
                        } else {
                            // this sets the button cell when the list is empty
                            setText("Genome");
//                            setTextFill(null);
                        }
                    }

                };
            }
        };

        currentCoordinate.getStyleClass().add("current-coord");
        currentCoordinate.setEditable(false);
        genomeSelected.setOnAction(e -> {
            Genome selected = genomeSelected.getSelectionModel().getSelectedItem();
            Optional<GenomeSpan> selectedSpan = model.getSpan(selected).getValue();
            currentCoordinate.textProperty().setValue(selectedSpan.map(span -> span.toString()).orElse("Coordinate not set"));
        });

        logger.info("subscribing to coordinate change events");
        coordinateChangeEvents.subscribe((CoordinateChangeEvent t) -> {
            logger.info("received coordinate change event {}", t);
            genomeSelected.getSelectionModel().select(t.getGenome());
            Optional<GenomeSpan> selectedSpan = t.getNext();
            currentCoordinate.textProperty().setValue(selectedSpan.map(span -> span.toString()).orElse("Coordinate not set"));
        });

        currentCoordinate.setOnMouseClicked(e -> {
            logger.info("clicked, copying coordinates to input field if coordinate is set");
            Genome selected = genomeSelected.getSelectionModel().getSelectedItem();
            if (selected != null) {
                model.getSpan(selected).getValue().ifPresent((span)->coordinateField.setText(currentCoordinate.getText()));
            }
        });

//        currentCoordinate.setOnMouseClicked(
////                new EventHandler<MOUSE_CLICKED>() {
////
////            @Override
////            public void handle(MouseEvent event) {
////               Genome selected = genomeSelected.getSelectionModel().getSelectedItem();
////                    if (selected != null) {
////                        model.getSpan(selected).getValue().ifPresent(coordinateField.setText(currentCoordinate.getText()));
////                    }
////            }
////        }
//                (MouseEvent.MOUSE_CLICKED e) -> {
//                    Genome selected = genomeSelected.getSelectionModel().getSelectedItem();
//                    if (selected != null) {
//                        model.getSpan(selected).getValue().ifPresent(coordinateField.setText(currentCoordinate.getText()));
//                    }
//                }
//        );
        genomeSelected.setButtonCell(s.call(null));
        genomeSelected.setCellFactory(s);

        Bindings.bindContent(genomeSelected.getItems(), model.getGenomes());
    }

}
