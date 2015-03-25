package org.mskcc.shenkers.controller;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.mskcc.shenkers.control.alignment.AlignmentRenderer;
import org.mskcc.shenkers.control.alignment.io.AlignmentLoader;
import org.mskcc.shenkers.control.alignment.AlignmentType;
import org.mskcc.shenkers.control.track.AbstractContext;
import org.mskcc.shenkers.control.track.FileType;
import org.mskcc.shenkers.control.track.TrackBuilder;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.control.track.TrackCell;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.alignment.chain.ChainAlignmentOverlay;
import org.mskcc.shenkers.control.track.bigwig.BigWigContext;
import org.mskcc.shenkers.control.track.gene.GeneModelContext;
import org.mskcc.shenkers.control.track.rest.RestIntervalContext;
import org.mskcc.shenkers.control.track.rest.RestIntervalProvider;
import org.mskcc.shenkers.control.track.rest.RestIntervalView;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.SimpleTrack;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.view.LineHistogramView;
import org.mskcc.shenkers.view.VerticalHiddenScrollPane;
import org.reactfx.EventSource;

public class FXMLController implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    ModelSingleton model;

    @FXML
    BorderPane histogram;

    @FXML
    BorderPane histogram2;

    int rowIndex = 0;

    @FXML
    StackPane info;

    @FXML
    Pane overlay;

    @FXML
    SplitPane genomeSplitPane;

    @FXML
    Pane alignmentOverlay;

    TrackBuilder trackBuilder;

    AlignmentLoader alignmentLoader;
    Optional<ChainAlignmentOverlay> cao;
    ObjectBinding spanBinding;

    @Inject
    private void setModel(ModelSingleton model) {
        logger.info("injecting model");
        this.model = model;
    }

    @Inject
    private void setAlignmentLoader(AlignmentLoader b) {
        logger.info("injecting track builder");
        this.alignmentLoader = b;
    }

    @Inject
    private void setTrackBuilder(TrackBuilder b) {
        logger.info("injecting track builder");
        this.trackBuilder = b;
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
        logger.info("Loading track...");
        System.out.println("loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (BAM)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    if (lastDir != null) {
                        fc.setInitialDirectory(lastDir);
                    }
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        lastDir = selection.isDirectory() ? selection : selection.getParentFile();
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
//                Track t = new Track() {
//                    public String getName() {
//                        return selectedFile.getText();
//                    }
//                };

                Track<BamContext> t = trackBuilder.load(FileType.BAM, selectedFile.getText());

                Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();

                // bind the span property of this track to the model
                t.getSpan().bind(model.getSpan(selectedGenome));

                t.getSpan().addListener(new ChangeListener<Optional<GenomeSpan>>() {
                    public void changed(ObservableValue<? extends Optional<GenomeSpan>> observable, Optional<GenomeSpan> oldValue, Optional<GenomeSpan> newValue) {
                        logger.info("span change detected in track " + t);
                    }
                });

                model.addTrack(selectedGenome, t);

            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    @FXML
    private void loadTracksByExtension(ActionEvent event) {
        logger.info("Loading track...");
        System.out.println("loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (BAM)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    if (lastDir != null) {
                        fc.setInitialDirectory(lastDir);
                    }
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    List<File> list = fc.showOpenMultipleDialog(s);

                    if (list != null) {
                        // TODO error if path contains a comma
                        selectedFile.setText(StringUtils.join(list.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()), ","));
                        for (File selection : list) {
                            lastDir = selection.isDirectory() ? selection : selection.getParentFile();
                            System.out.println(selection.getAbsolutePath());

                        }
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
//                Track t = new Track() {
//                    public String getName() {
//                        return selectedFile.getText();
//                    }
//                };
                String[] text = selectedFile.getText().split(",");
                for (String path : text) {
                    Track<BamContext> t = null;

                    if (path.matches(".*.bam$")) {
                        t = trackBuilder.load(FileType.BAM, path);
                    }
                    if (path.matches(".*.bw$")) {
                        t = trackBuilder.load(FileType.WIG, path);
                    }
                    if (path.matches(".*.gtf$")) {
                        t = trackBuilder.load(FileType.GTF, path);
                    }

                    Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();

                    // bind the span property of this track to the model
                    t.getSpan().bind(model.getSpan(selectedGenome));

                    t.getSpan().addListener(new ChangeListener<Optional<GenomeSpan>>() {
                        public void changed(ObservableValue<? extends Optional<GenomeSpan>> observable, Optional<GenomeSpan> oldValue, Optional<GenomeSpan> newValue) {
                            logger.info("span change detected in track " + observable);
                        }
                    });

                    model.addTrack(selectedGenome, t);
                }
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    File lastDir;

    @FXML
    private void loadBigWigTrack(ActionEvent event) {
        logger.info("Loading track...");
        System.out.println("loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (BigWig)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    if (lastDir != null) {
                        fc.setInitialDirectory(lastDir);
                    }
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        lastDir = selection.isDirectory() ? selection : selection.getParentFile();
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
//                Track t = new Track() {
//                    public String getName() {
//                        return selectedFile.getText();
//                    }
//                };

                Track<BigWigContext> t = trackBuilder.load(FileType.WIG, selectedFile.getText());

                Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();

                // bind the span property of this track to the model
                t.getSpan().bind(model.getSpan(selectedGenome));

                t.getSpan().addListener(new ChangeListener<Optional<GenomeSpan>>() {
                    public void changed(ObservableValue<? extends Optional<GenomeSpan>> observable, Optional<GenomeSpan> oldValue, Optional<GenomeSpan> newValue) {
                        logger.info("span change detected in track " + t);
                    }
                });

                model.addTrack(selectedGenome, t);

            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    @FXML
    private void loadGtfGeneTrack(ActionEvent event) {
        logger.info("Loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (GTF)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    if (lastDir != null) {
                        fc.setInitialDirectory(lastDir);
                    }
                    Stage s = new Stage();
                    fc.setTitle("Load GTF gene model file");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        lastDir = selection.isDirectory() ? selection : selection.getParentFile();
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
//                Track t = new Track() {
//                    public String getName() {
//                        return selectedFile.getText();
//                    }
//                };

                Track<GeneModelContext> t = trackBuilder.load(FileType.GTF, selectedFile.getText());

                Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();

                // bind the span property of this track to the model
                t.getSpan().bind(model.getSpan(selectedGenome));

                t.getSpan().addListener(new ChangeListener<Optional<GenomeSpan>>() {
                    public void changed(ObservableValue<? extends Optional<GenomeSpan>> observable, Optional<GenomeSpan> oldValue, Optional<GenomeSpan> newValue) {
                        logger.info("span change detected in track " + t);
                    }
                });

                model.addTrack(selectedGenome, t);

            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    AlignmentRenderer ar;
    Property<Worker.State> alignmentRenderState = new SimpleObjectProperty<>(Worker.State.SUCCEEDED);

    @FXML
    private void loadChainAlignment(ActionEvent event) {
        logger.info("loading alignments");

        GridPane gp1 = new GridPane();
        ObservableList<Genome> genomes = model.getGenomes();

        List<ComboBox<Pair<Genome, Genome>>> genomePairs = new ArrayList<>();
        List<TextField> textFields = new ArrayList<>();
        List<Button> selectFiles = new ArrayList<>();

        ValidationSupport validation = new ValidationSupport();

        List<Pair<Genome, Genome>> all_pairs = new ArrayList<>();
        for (int i = 0; i < genomes.size(); i++) {
            Genome g1 = genomes.get(i);
            for (int j = i + 1; j < genomes.size(); j++) {
                Genome g2 = genomes.get(j);
                all_pairs.add(new Pair<>(g1, g2));
                all_pairs.add(new Pair<>(g2, g1));
            }
        }
        for (int i = 0; i < genomes.size() - 1; i++) {
            ComboBox<Pair<Genome, Genome>> liftPair = new ComboBox<>();
            liftPair.getItems().addAll(all_pairs);
            liftPair.setConverter(
                    new StringConverter<Pair<Genome, Genome>>() {

                        public String toString(Pair<Genome, Genome> object) {
                            return String.format("%s -> %s", object.getKey().getId(), object.getValue().getId());
                        }

                        @Override
                        public Pair<Genome, Genome> fromString(String string) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    }
            );

            genomePairs.add(liftPair);

            TextField selectedFile = new TextField("");
            textFields.add(selectedFile);
            Button button = new Button("Select");
            selectFiles.add(button);

            validation.registerValidator(liftPair, Validator.createEmptyValidator(String.format("Specify alignment direction liftOver file")));
            validation.registerValidator(selectedFile, Validator.createEmptyValidator(String.format("Specify chain for genomes")));

            button.setOnAction(
                    actionEvent -> {
                        FileChooser fc = new FileChooser();
                        Stage s = new Stage();
                        fc.setTitle("Select a fasta reference sequence");
                        File selection = fc.showOpenDialog(s);
                        if (selection != null) {
                            logger.info("Selected alignment file: " + selection.getAbsolutePath());
                            selectedFile.setText(selection.getAbsolutePath());
                        }
                    }
            );
        }

        for (int i = 0; i < genomePairs.size(); i++) {
            gp1.addRow(i, genomePairs.get(i), textFields.get(i), selectFiles.get(i));
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.getg
//        alert.setHeight(800);
        alert.setTitle("Load alignment");
//        alert.setHeaderText("Configure pairwise alignments");
//        MonadicBinding<Alert.AlertType> map = EasyBind.map(validation.invalidProperty(), (isInvalid) -> {return isInvalid ? Alert.AlertType.ERROR : Alert.AlertType.NONE; });
//        alert.alertTypeProperty().bind(map);

        alert.headerTextProperty().bind(EasyBind.map(validation.invalidProperty(), invalid -> invalid ? "Complete required fields" : "Configure pairwise alignments"));
        alert.getDialogPane().setContent(gp1);

//        Alert.AlertType
        alert.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(validation.invalidProperty());
//        ButtonType loginButtonType = new ButtonType("Create genome", ButtonData.FINISH);
//        alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
//
//        ((Button)alert.getDialogPane().lookupButton(ButtonData.OK_DONE)).setText("Create genome");
        alert.showAndWait().ifPresent(buttonType -> {
            logger.info("{}", buttonType);
            logger.info("{}", buttonType.getText());
            if (buttonType.equals(ButtonType.OK)) {
                logger.info("preparing to load alignments");
                List<AlignmentSource> alignmentSources = textFields.stream().map(textField -> alignmentLoader.load(textField.getText())).collect(Collectors.toList());
                List<Pair<Genome, Genome>> selectedPairs = genomePairs.stream().map(comboBox -> comboBox.getSelectionModel().getSelectedItem()).collect(Collectors.toList());
                model.setAlignments(selectedPairs, alignmentSources);
                ChainAlignmentOverlay CAO = new ChainAlignmentOverlay(model.getAlignments());
                cao = Optional.of(CAO);

                List<DoubleProperty> dividerPositions = genomeSplitPane.getDividers().stream().map(d -> d.positionProperty()).collect(Collectors.toList());
                ar = new AlignmentRenderer(overlay, model, CAO, dividerPositions, viewportWidthProperty, genomeSplitPane.heightProperty());
                alignmentRenderState.unbind();
                alignmentRenderState.bind(ar.stateProperty());
                ar.start();

                ObservableValue[] spanProperties = model.getGenomes().stream().map(g -> model.getSpan(g)).collect(Collectors.toList()).toArray(new ObservableValue[0]);

                class SpanBinding extends ObjectBinding {

                    public SpanBinding(Observable[] dependencies) {
                        super.bind(dependencies);
                    }

                    @Override
                    protected Object computeValue() {
                        return null;
                    }

                }
                spanBinding = new SpanBinding(spanProperties);
                spanBinding.addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        logger.info("span change detected, updating overlay");
                        ar.restart();
                        spanBinding.get();
                    }
                });

                /*
                 Runnable r = () -> {

                 // TODO check if all spans are set, otherwise need to pass.
                 overlay.getChildren().clear();
                 logger.info("checking if all spans set");
                 boolean allSpansSet = model.getGenomes().stream().allMatch(g -> model.getSpan(g).getValue().isPresent());
                 if (allSpansSet) {
                 logger.info("all spans set, building alignment component");
                 Map<Genome, GenomeSpan> spans = model.getGenomes().stream().collect(Collectors.toMap(g -> g, g -> model.getSpan(g).getValue().get()));

                 //                2R:12743706-12747879
                 //                2R:16228220-16231888:-
                 //                        /home/sol/lailab/sol/genomes/chains/netChainSubset/dm3.droYak3.net.chain
                 //                    /home/sol/lailab/sol/mel_yak_vir/total_rna/M/T.bam
                 //                    /home/sol/lailab/sol/mel_yak_vir/total_rna/Y/T.bam
                 //                    /home/sol/lailab/sol/mel_yak_vir/total_rna/V/T.bam
                 //                    /home/sol/lailab/sol/genomes/chains/netChainSubset/M.Y.chain
                 //                    /home/sol/lailab/sol/genomes/chains/netChainSubset/M.V.chain
                 //                    /home/sol/lailab/sol/genomes/chains/netChainSubset/Y.V.chain
                 //3R:4892065-4900435
                 //3R:8938890-8947395
                 //                    scaffold_12855:8568616-8577467
                 //3R:4895365-4897435
                 //3R:8942090-8944295
                 //                    scaffold_12855:8570616-8573467
                 List<DoubleProperty> dividerPositions = genomeSplitPane.getDividers().stream().map(d -> d.positionProperty()).collect(Collectors.toList());

                 logger.info("spans {}", spans);
                 logger.info("flips {}", flips);
                 logger.info("divider {}", dividerPositions);

                 try {
                 overlay.getChildren().setAll(cao.get().getOverlayPaths(model.getGenomes(), spans, flips, 100, dividerPositions, viewportWidthProperty, genomeSplitPane.heightProperty()));
                 } catch (Exception e) {
                 logger.info("caught alignment error");
                 logger.info("message: ", e);
                 }
                 } else {
                 logger.info("not all spans set, not building alignment");
                 }
                 };

                 r.run();
                 */
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                logger.info("canceling loading alignments");
            }
        });

    }

    IntegerProperty x = new SimpleIntegerProperty(0);
    ReadOnlyDoubleProperty viewportWidthProperty;
    ObservableList<Node> genomeSplitPaneNodes;
    List<EventSource<Integer>> coordinateClicks = new ArrayList<>();
    ListView<String> trackListView;
    ObservableBooleanValue isBusy;

    public <S, T> ObservableList<T> createMapValueBinding(ObservableMap<S, ObservableList<T>> om) {

        // an observable list that is invalidated anytime one of the component lists is invalidated
        ObservableList<ObservableList<T>> observableValues = FXCollections.observableArrayList(new Callback<ObservableList<T>, Observable[]>() {

            @Override
            public Observable[] call(ObservableList<T> param) {
                return new Observable[]{param};
            }
        });

        om.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                List<ObservableList<T>> tmp = new ArrayList<>();
                for (S key : om.keySet()) {
                    tmp.add(om.get(key));
                }
                observableValues.setAll(tmp);
            }
        });

        ObservableList<T> flat = FXCollections.observableArrayList();
        observableValues.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                List<T> tmp = new ArrayList<>();
                for (ObservableList<T> ol : observableValues) {
                    tmp.addAll(ol);
                }
                flat.setAll(tmp);
            }
        });

        observableValues.setAll(om.values());

        return flat;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Genome> genomes = model.getGenomes();

        ObservableList<Track<AbstractContext>> createMapValueBinding = createMapValueBinding(model.getTracks());
        ObservableList<Track> stateBound = FXCollections.observableArrayList(new Callback<Track, Observable[]>() {

            @Override
            public Observable[] call(Track param) {
                return new Observable[]{param.getRenderStrategy().stateProperty()};
            }
        });

        Bindings.bindContent(stateBound, createMapValueBinding);

        isBusy = Bindings.createBooleanBinding(() -> {
            logger.info("track render states " + createMapValueBinding.stream()
                    .map(track -> track.getRenderStrategy().getState())
                    .collect(Collectors.toList()));
            logger.info("alignment render state " + alignmentRenderState.getValue());
            Boolean busy
                    = // if either the alignment render state
                    // or the track renderers are not finished rendering
                    Stream.concat(Stream.of(alignmentRenderState),
                            createMapValueBinding.stream()
                            .map(track -> track.getRenderStrategy().getState()))
                    .map(state
                            -> state != Worker.State.SUCCEEDED
                            && state != Worker.State.CANCELLED
                            && state != Worker.State.FAILED)
                    .reduce(false, (b1, b2) -> b1 || b2);

            return busy;
        }, stateBound, alignmentRenderState);

        isBusy.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.info("busy listener " + newValue);
            }
        });

        trackListView = new ListView<String>();

        {
            overlay.setMouseTransparent(true);
        }
        {
            info.setMouseTransparent(true);

            Label label = new Label("initialize alignment");
            label.setOpacity(.2);
            label.setFont(new Font(15));
            label.setTextFill(Color.RED);
            info.getChildren().add(new BorderPane(label));

            model.getGenomes().addListener(new ListChangeListener<Genome>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends Genome> c) {
                    StringExpression sp = new SimpleStringProperty("initialize alignment: ");

                    for (Genome g : c.getList()) {
                        sp = sp.concat(Bindings.format("[ %s %s ]", g, model.getSpan(g)));
                    }
                    Label label = new Label();
                    label.setOpacity(.2);
                    label.setFont(new Font(15));
                    label.setTextFill(Color.RED);
                    label.textProperty().bind(sp);
                    info.getChildren().setAll(new BorderPane(label));
                }
            });
        }

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
        Function<Genome, Node> f = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return new BorderPane(new Label(text));
        };

        Function<String, BorderPane> f2 = (String text) -> {
            return new BorderPane(new Label(text));
        };

        Function<Genome, String> f3 = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return text;
        };

        genomeSplitPaneNodes = FXCollections.observableArrayList();
        viewportWidthProperty = genomeSplitPane.widthProperty();

        // listen for changes in the set of genomes and update the list of nodes to be 
        // displayed in the split pane
        genomes.addListener(new ListChangeListener<Genome>() {

            public void onChanged(ListChangeListener.Change<? extends Genome> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed genomes");
                genomeSplitPaneNodes.clear();
                coordinateClicks.clear();

                Function<Genome, ListView<Track<AbstractContext>>> createListView = (Genome g) -> {
                    ListView<Track<AbstractContext>> trackListView = new ListView<>();

//                    model.getTracks(g).addListener(new ListChangeListener<Track>() {
//                        public void onChanged(ListChangeListener.Change<? extends Track> c) {
////                            List<String> map = model.getTracks(g).stream().map(Track::getName).collect(Collectors.toList());
//                            List<Track> map = model.getTracks(g);
//                            
//                            trackListView.getItems().setAll(map);
//                        }
//                    });
                    ObservableList<Track<AbstractContext>> tracks = model.getTracks(g);

                    tracks.addListener(new ListChangeListener<Track<AbstractContext>>() {

                        @Override
                        public void onChanged(ListChangeListener.Change<? extends Track<AbstractContext>> c) {
                            while (c.next()) {
                                if (c.wasAdded() || c.wasRemoved()) {
                                    logger.info("adjusting height for trackListView {}", trackListView);
                                    trackListView.setPrefHeight((tracks.size() * 100) + 2);
                                }
                            }
                        }
                    });
                    trackListView.setPrefHeight(tracks.isEmpty() ? 0 : (tracks.size() * 100) + 2);

                    Callback<Track<AbstractContext>, Observable[]> extractor = new Callback<Track<AbstractContext>, Observable[]>() {

                        @Override
                        public Observable[] call(Track<AbstractContext> param) {
                            // TODO if there are other features that need to trigger updates, add them here
                            return new Observable[]{param.getSpan(), param.getView()};
                        }
                    };

                    ObservableList<Track<AbstractContext>> observableTrackList = FXCollections.observableArrayList(extractor);
                    Bindings.bindContent(observableTrackList, tracks);
                    observableTrackList.addListener(new ListChangeListener<Track>() {

                        @Override
                        public void onChanged(ListChangeListener.Change<? extends Track> c) {
                            while (c.next()) {
                                if (c.wasUpdated()) {
                                    System.out.println("list update (span) detected");
                                }
                            }
                        }
                    });

                    trackListView.setItems(observableTrackList);

                    trackListView.setCellFactory((ListView<Track<AbstractContext>> view) -> {
                        TrackCell<AbstractContext> cell = new TrackCell<AbstractContext>();
//                        cell.getStyleClass().add("track");
//                        cell.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                        cell.setPrefWidth(Region.USE_COMPUTED_SIZE);
                        return cell;
                    });

                    return trackListView;
                };

                if (genomes.size() > 0) {
                    Genome g = genomes.get(0);
                    ListView<Track<AbstractContext>> trackListView = createListView.apply(g);
                    trackListView.getStyleClass().add("genome-list-view");
//                    trackListView.getStylesheets().add("/styles/TrackStyles.css");
                    trackListView.fixedCellSizeProperty().setValue(100);
                    EventSource<Integer> build = new CoordinateClickStreamBuilder(trackListView, model.getSpan(g)).build();
                    build.subscribe(coord -> {
                        Optional<GenomeSpan> span = model.getSpan(g).getValue();
                        span.ifPresent(s -> {
                            int w = s.getEnd() - s.getStart() + 1;
                            int wl = w / 2;
                            int wr = w - wl - 1;
                            model.setSpan(g, Optional.of(new GenomeSpan(s.getChr(), coord - wl, coord + wr, s.isNegativeStrand())));
                        });
                    });
                    VerticalHiddenScrollPane vhsp = new VerticalHiddenScrollPane();
                    vhsp.build(trackListView);

                    genomeSplitPaneNodes.add(vhsp);
                    coordinateClicks.add(build);
                }
                for (int i = 1; i < genomes.size(); i++) {
                    // add a track to represent the alignment between sequential genomes
                    {
                        BorderPane apply = f2.apply(f3.apply(genomes.get(i - 1)) + " aligned to " + f3.apply(genomes.get(i)));
                        genomeSplitPaneNodes.add(apply);
                    }
//                    genomeSplitPaneNodes.add(f.apply(genomes.get(i)));
                    Genome g = genomes.get(i);
                    ListView<Track<AbstractContext>> trackListView = createListView.apply(g);
                    trackListView.getStyleClass().add("genome-list-view");
                    trackListView.fixedCellSizeProperty().setValue(100);
                    EventSource<Integer> build = new CoordinateClickStreamBuilder(trackListView, model.getSpan(g)).build();
                    build.subscribe(coord -> {
                        Optional<GenomeSpan> span = model.getSpan(g).getValue();
                        span.ifPresent(s -> {
                            int w = s.getEnd() - s.getStart() + 1;
                            int wl = w / 2;
                            int wr = w - wl - 1;
                            model.setSpan(g, Optional.of(new GenomeSpan(s.getChr(), coord - wl, coord + wr, s.isNegativeStrand())));
                        });
                    });

                    VerticalHiddenScrollPane vhsp = new VerticalHiddenScrollPane();
                    vhsp.build(trackListView);

                    genomeSplitPaneNodes.add(vhsp);
                    coordinateClicks.add(build);
                }
            }
        });

        genomeSplitPaneNodes.addListener(new ListChangeListener<Node>() {

            public void onChanged(ListChangeListener.Change<? extends Node> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed map");
            }
        });

        genomeSplitPane.getStyleClass().add("genome-split-pane");
//        sp.getItems().addListener((ListChangeListener.Change<? extends Node> e)->{System.out.println("changed view");});
        Bindings.bindContentBidirectional(genomeSplitPane.getItems(), genomeSplitPaneNodes);

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

        Rectangle r3 = new Rectangle(10, 20);
        Rectangle r4 = new Rectangle(30, 40, 10, 20);
        r3.xProperty().bind(x);
        r4.xProperty().bind(x.add(10));

        Label lbl = new Label("hi");

        sim = new SimpleTrack();
//        ModelSingleton.getInstance().genomeSpanProperty().addListener(sim);

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
        Genome g = new Genome("g", "genome");
        model.addGenome(g);
        model.setSpan(g, Optional.of(new GenomeSpan("X", 1, 10, false)));
        for (int i = 4; i <= 7; i++) {
            final int j = i;
            Track trck = new Track<>(new RestIntervalContext(new RestIntervalProvider() {

                @Override
                public GenomeSpan query() {
                    return new GenomeSpan("X", j, j, false);
                }
            }), Arrays.asList(new RestIntervalView()));
            model.addTrack(g, trck);
        }
        RestIntervalContext ric = new RestIntervalContext(new RestIntervalProvider() {
            
            @Override
            public GenomeSpan query() {
                return new GenomeSpan("X", 4, 7, false);
            }
        });
        Track trck = new Track<>(ric, Arrays.asList(new RestIntervalView()));
        model.addTrack(g, trck);
        new Thread(()->{
        while(true){
            
           
                ric.setReader(new RestIntervalProvider() {
                    
                    @Override
                    public GenomeSpan query() {
                        Random r = new Random();
                        int i = r.nextInt(4);
                        return new GenomeSpan("X", i, i+4, false);
                    }
                });
          
                Platform.runLater(()->{
            trck.update();
                });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                
            }
        }
        }).start();
    }
}
