/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import javafx.fxml.FXML;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;

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
    ComboBox<Genome> genomeSelected;

    Pattern coordinatePattern;

    ModelSingleton model;
    
     @FXML
    public void flipSelectedSpan(ActionEvent event) {
        logger.info("processing flip");
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        if(g!=null){
        
            ObservableValue<Optional<GenomeSpan>> span = model.getSpan(g);
            span.getValue().ifPresent(s -> { model.setSpan(g, Optional.of(new GenomeSpan(s.getChr(), s.getStart(), s.getEnd(), !s.isNegativeStrand())));});

        }
    }

    @FXML
    public void parseAndUpdateCoordinates(ActionEvent event) {
        Genome g = genomeSelected.getSelectionModel().getSelectedItem();
        if(g!=null){
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
        int nTracks = model.getnTracks();
        Label child = new Label(coordinateField.getText() + " r=" + nTracks);
        Label child2 = new Label(coordinateField.getText() + " r=" + nTracks);
        model.setnTracks(nTracks + 1);

        GridPane gridpane = (GridPane) this.child.getParent().lookup("#gridpane");

        gridpane.addRow(nTracks, child, child2);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        coordinatePattern = Pattern.compile("^([^:]+):([0-9]+)(-([0-9]+))?(:([+-]))?$");
        model = ModelSingleton.getInstance();

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
        
        genomeSelected.setButtonCell(s.call(null));
        genomeSelected.setCellFactory(s);

        Bindings.bindContent(genomeSelected.getItems(), model.getGenomes());
    }

}
