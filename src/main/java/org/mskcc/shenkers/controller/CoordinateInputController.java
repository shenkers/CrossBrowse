/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import javafx.fxml.FXML;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.model.ModelSingleton;

/**
 * FXML Controller class
 *
 * @author sol
 */
public class CoordinateInputController implements Initializable {
    
    @FXML
    private FlowPane child;

    @FXML
    Button goButton;

    @FXML
    TextField coordinateField;
    
    Pattern coordinatePattern;
    
    ModelSingleton model;
		 
    @FXML
    public void parseAndUpdateCoordinates(ActionEvent event) {
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
            model.setSpan(new GenomeSpan(chr, start, end, isNegativeStrand));
        } else {
            // notify error
            System.out.println("no-match");
        }

    }

    @FXML
    public void zoomIn(ActionEvent event) {
        int nTracks = model.getnTracks();
        Label child = new Label(coordinateField.getText()+" r="+nTracks);
        Label child2 = new Label(coordinateField.getText()+" r="+nTracks);
        model.setnTracks(nTracks+1);
        
         
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
    }

}
