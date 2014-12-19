/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author sol
 */
public class CustomContainerController extends Button {

    @FXML
    private void handleCButtonAction(ActionEvent event) {
        System.out.println("You got the custom controller!");
        
    }

    public CustomContainerController() {
        
     FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/CustomContainer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(new Object(){@FXML
    private void handleCButtonAction(ActionEvent event) {
        System.out.println("You got the internal controller!");
        
    }});

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
     
    
}
