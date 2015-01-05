package org.mskcc.shenkers.controller;

import com.sun.javafx.binding.ContentBinding;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.imodel.Track;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.SimpleTrack;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

public class FXMLController implements Initializable {

    ModelSingleton model = ModelSingleton.getInstance();

    @FXML
    GridPane gridpane;

    @FXML
    ListView<String> lv;

    int rowIndex = 0;

    @FXML
    SplitPane sp;

//    @FXML
//    ListView lv1, lv2, lv3;
    @FXML
    Pane canvas;

    @FXML
    BorderPane stp;

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

        x.set((x.getValue() + 10) % 100);

    }
    SimpleTrack sim;

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
            if (buttonType.equals(ButtonType.OK)) {
                System.err.println("finished");
                String fastaFile = selectedFasta.getText();
                String name = genomeName.getText();
                String id = genomeID.getText();
                String text = String.format("%s %s %s", selectedFasta.getText(), genomeID.getText(), genomeName.getText());
                Genome newGenome = new Genome(id, name);
                model.addGenome(newGenome);
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

        ObservableList<Genome> genomes = model.getGenomes();
//                = FXCollections.observableArrayList(
//                        new Genome("dm3", "melanogaster"),
//                        new Genome("dya", "yakuba")
//                );

        ComboBox<Genome> genomeSelector = new ComboBox<>(genomes);

        genomeSelector.setConverter(
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

        gp1.add(genomeSelector, 1, 1);

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
            if (buttonType.equals(ButtonType.OK)) {
                System.err.println("finished");
                Track t = new Track() {
                    public String getName() {
                        return selectedFile.getText();
                    }
                };
                Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();
                model.addTrack(selectedGenome, t);
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    IntegerProperty x = new SimpleIntegerProperty(0);
    ObservableList<Node> genomeSplitPaneNodes;
    ListView<String> trackListView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Genome> genomes = model.getGenomes();
        trackListView = new ListView<String>();

        System.out.println("genomes: " + genomes);

        /*
         private final WeakReference<List<E>> listRef;

         public ListContentBinding(List<E> list) {
         this.listRef = new WeakReference<List<E>>(list);
         }

         @Override
         public void onChanged(Change<? extends E> change) {
         final List<E> list = listRef.get();
         if (list == null) {
         change.getList().removeListener(this);
         } else {
         while (change.next()) {
         if (change.wasPermutated()) {
         list.subList(change.getFrom(), change.getTo()).clear();
         list.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
         } else {
         if (change.wasRemoved()) {
         list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
         }
         if (change.wasAdded()) {
         list.addAll(change.getFrom(), change.getAddedSubList());
         }
         }
         }
         }
         }
         */
        genomeSplitPaneNodes = EasyBind.map(genomes,
                (Genome g) -> {
                    String text = String.format("%s:%s", g.getId(), g.getDescription());
                    System.out.println(text);
                    return new BorderPane(new Label(text));
//                        return text;
                });

        Function<Genome, Node> f = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return new BorderPane(new Label(text));
        };

        Function<String, Node> f2 = (String text) -> {
            return new BorderPane(new Label(text));
        };

        Function<Genome, String> f3 = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return text;
        };

        genomeSplitPaneNodes = FXCollections.observableArrayList();

        // listen for changes in the set of genomes and update the list of nodes to be 
        // displayed in the split pane
        genomes.addListener(new ListChangeListener<Genome>() {

            class TrackCell extends ListCell<Track> {

                protected void updateItem(Track item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setGraphic(new Label(item.getName()));
                    }
                }
            }

            public void onChanged(ListChangeListener.Change<? extends Genome> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed genomes");
                genomeSplitPaneNodes.clear();

                Function<Genome, ListView<String>> createListView = (Genome g) -> {
                    ListView<String> trackListView = new ListView<>();

                    model.getTracks(g).addListener(new ListChangeListener<Track>() {
                        public void onChanged(ListChangeListener.Change<? extends Track> c) {
                            List<String> map = model.getTracks(g).stream().map(Track::getName).collect(Collectors.toList());
                            trackListView.getItems().setAll(map);
                        }
                    });
                    
                    List<String> map = model.getTracks(g).stream().map(Track::getName).collect(Collectors.toList());
                    trackListView.getItems().setAll(map);
                    return trackListView;
                };

                if (genomes.size() > 0) {
                    Genome g = genomes.get(0);
                    ListView<String> apply = createListView.apply(g);
                    genomeSplitPaneNodes.add(apply);
                }
                for (int i = 1; i < genomes.size(); i++) {
                    genomeSplitPaneNodes.add(f2.apply(f3.apply(genomes.get(i - 1)) + " aligned to " + f3.apply(genomes.get(i))));
//                    genomeSplitPaneNodes.add(f.apply(genomes.get(i)));
                    Genome g = genomes.get(i);
                    ListView<String> apply = createListView.apply(g);
                    genomeSplitPaneNodes.add(apply);
                }
            }
        });

        genomeSplitPaneNodes.addListener(new ListChangeListener<Node>() {

            public void onChanged(ListChangeListener.Change<? extends Node> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed map");
            }
        });

        sp.getStyleClass().add("genome-split-pane");
//        sp.getItems().addListener((ListChangeListener.Change<? extends Node> e)->{System.out.println("changed view");});
        Bindings.bindContentBidirectional(sp.getItems(), genomeSplitPaneNodes);

//        ObservableList<Integer> oal = FXCollections.observableArrayList(1,2,3);
//        
//        class abc {
//            int i;
//
//            public abc(int i) {
//                this.i = i;
//            }
//
//            @Override
//            public String toString() {
//                return i+"";
//            }
//            
//            
//        }
//        ObservableList<abc> mapped_oal = EasyBind.map(oal, 
//                (Integer i) -> { return new abc(i); }
//        );
//        mapped_oal.addListener(new ListChangeListener<abc>() {
//
//            @Override
//            public void onChanged(ListChangeListener.Change<? extends abc> c) {
//                System.out.println("changed mapped");
//            }
//        });
//        
//        List<abc> l = new ArrayList<>();
//        Bindings.bindContent(l, mapped_oal);
//        System.out.println(mapped_oal);
////        mapped_oal.remove("2");
//        oal.remove(new Integer(2));
//        oal.add(4);
//        System.out.println(mapped_oal);
//        System.out.println(l);
//        System.exit(0);
        Rectangle r = new Rectangle(10, 20);

        Rectangle r2 = new Rectangle(30, 40, 10, 20);

        r.xProperty().bind(x);
        r2.xProperty().bind(x.add(10));

        canvas.getChildren().addAll(r, r2);

        Rectangle r3 = new Rectangle(10, 20);
        Rectangle r4 = new Rectangle(30, 40, 10, 20);
        r3.xProperty().bind(x);
        r4.xProperty().bind(x.add(10));

        Label lbl = new Label("hi");

        sim = new SimpleTrack();
        ModelSingleton.getInstance().genomeSpanProperty().addListener(sim);
        stp.setCenter(sim);

//        lv1.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening1");
//                double[] d = sp.getDividerPositions();
//                sp.setDividerPosition(1, 1-(d[0]/2));
//            }
//        }
//        );
//        lv2.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening2");
//            }
//        }
//        );
//        lv3.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening3");
//            }
//        }
//                
//        );
    }
}
