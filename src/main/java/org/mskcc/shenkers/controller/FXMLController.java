package org.mskcc.shenkers.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.mskcc.shenkers.model.datatypes.Genome;

public class FXMLController implements Initializable {

    @FXML
    GridPane gridpane;

    @FXML
    ListView<String> lv;

    int rowIndex = 0;

    @FXML
    SplitPane sp;
    
    @FXML
    ListView lv1, lv2, lv3;
   
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }

    @FXML
    private void handleButtonAction2(ActionEvent event) {
        System.out.println("You clicked me2!");
        Label child = new Label("c=0,r=" + rowIndex);
        Label child2 = new Label("c=1,r=" + rowIndex);

        gridpane.addRow(rowIndex, child, child2);
//        gridpane.setConstraints(child, 0, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
        gridpane.getRowConstraints().add(new RowConstraints());
        gridpane.getRowConstraints().get(rowIndex).setValignment(VPos.TOP);
        gridpane.getRowConstraints().get(rowIndex).setVgrow(Priority.ALWAYS);
        rowIndex++;
        lv.getItems().add(child2.getText());
        
    }

    @FXML
    private void loadGenome(ActionEvent event) {
        System.out.println("loading genome...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Reference sequence (FASTA)"), 0, 0);
        Button selectFasta = new Button("Select");
        TextField selectedFasta = new TextField("");
        gp1.add(selectedFasta, 1, 0);
        gp1.add(selectFasta, 2, 0);

        gp1.add(new Label("Reference sequence ID"), 0, 1);
        TextField genomeID = new TextField("");
        gp1.add(genomeID, 1, 1);

        gp1.add(new Label("Reference sequence name"), 0, 2);
        TextField genomeName = new TextField("");
        gp1.add(genomeName, 1, 2);

        selectFasta.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        System.out.println(selection.getAbsolutePath());
                        selectedFasta.setText(selection.getAbsolutePath());
                    }
                }
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load genome");
        alert.setHeaderText("Configure new genome");
        alert.getDialogPane().setContent(gp1);

//        ButtonType loginButtonType = new ButtonType("Create genome", ButtonData.FINISH);
//        alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
//
//        ((Button)alert.getDialogPane().lookupButton(ButtonData.OK_DONE)).setText("Create genome");
        alert.showAndWait().ifPresent(buttonType -> {
            System.err.println(buttonType);
            System.err.println(buttonType.getText());
            if (buttonType.equals(ButtonType.FINISH)) {
                System.err.println("finished");
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });

    }

    @FXML
    private void loadTrack(ActionEvent event) {
        System.out.println("loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (BAM,BigWig)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        System.out.println(selection.getAbsolutePath());
                        selectedFile.setText(selection.getAbsolutePath());
                    }
                }
        );

        gp1.add(new Label("Reference sequence ID"), 0, 1);

        ObservableList<Genome> genomes
                = FXCollections.observableArrayList(
                        new Genome("dm3", "melanogaster"),
                        new Genome("dya", "yakuba")
                );

        ComboBox<Genome> genomeID = new ComboBox<>(genomes);

        genomeID.setConverter(
                new StringConverter<Genome>() {

                    public String toString(Genome object) {
                        return object.getId();
                    }

                    @Override
                    public Genome fromString(String string) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }
        );

        gp1.add(genomeID, 1, 1);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load track");
        alert.setHeaderText("Configure new track");
        alert.getDialogPane().setContent(gp1);

//        ButtonType loginButtonType = new ButtonType("Create genome", ButtonData.FINISH);
//        alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
//
//        ((Button)alert.getDialogPane().lookupButton(ButtonData.OK_DONE)).setText("Create genome");
        alert.showAndWait().ifPresent(buttonType -> {
            System.err.println(buttonType);
            System.err.println(buttonType.getText());
            if (buttonType.equals(ButtonType.FINISH)) {
                System.err.println("finished");
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lv1.widthProperty().addListener(
                new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.err.println("listening1");
                double[] d = sp.getDividerPositions();
                sp.setDividerPosition(1, 1-(d[0]/2));
            }
        }
        );
        lv2.widthProperty().addListener(
                new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.err.println("listening2");
            }
        }
        );
        lv3.widthProperty().addListener(
                new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.err.println("listening3");
            }
        }
        );
    }
}
