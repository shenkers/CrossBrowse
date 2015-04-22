/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class FastaViewNGTest {

    public FastaViewNGTest() {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @Test
    public void testGetGraphic() throws InterruptedException {
        String title = "";
        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    String st = "CGATCGCCATTGAGCAAGTAAGCCAACTTTCGGCTCGCGTGTACGCGATAAGTAGGTGCCCTCTGCATCCGACGCACTTCAGCCGAACCACTTGCGGGAATTTGGGGGAGTGCTGATACGACGGCATAGGAATGGAGCTCTTTAAGTGCGTCTACACACGGACCGTACTTGGCCAAATCGGCAGTCAGTTGTATT";
                    FastaView tp = new FastaView();
                    tp.flip.setValue(true);
                    tp.setSequence(0,st);
                    
                    Scene scene = new Scene(tp, 300, 300, Color.GRAY);
                    Stage stage = new Stage();
                    stage.setScene(scene);

                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    stage.show();

                });
        l.await();
    }

}


/*

 package org.mskcc.shenkers.view;

 import com.google.common.collect.Range;
 import com.google.common.collect.RangeSet;
 import com.google.common.collect.TreeRangeSet;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningScheduledExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.itextpdf.awt.DefaultFontMapper;
 import com.itextpdf.awt.PdfGraphics2D;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfWriter;
 import htsjdk.samtools.reference.IndexedFastaSequenceFile;
 import java.awt.Graphics2D;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Future;
 import java.util.function.Function;
 import java.util.function.Supplier;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;
 import javafx.application.Platform;
 import javafx.beans.binding.Bindings;
 import javafx.beans.binding.DoubleBinding;
 import javafx.beans.binding.IntegerBinding;
 import javafx.beans.property.DoubleProperty;
 import javafx.beans.property.IntegerProperty;
 import javafx.beans.property.Property;
 import javafx.beans.property.SimpleDoubleProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.embed.swing.JFXPanel;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Orientation;
 import javafx.geometry.Pos;
 import javafx.scene.Scene;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.Background;
 import javafx.scene.layout.BackgroundFill;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.CornerRadii;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.StackPane;
 import javafx.scene.layout.TilePane;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import javafx.util.Pair;
 import javax.swing.JFrame;
 import org.mskcc.shenkers.control.track.DomainFlippable;
 import static org.testng.Assert.*;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;


 public FastaViewNGTest() {
 }

 @BeforeClass
 public static void setUpClass() throws Exception {
 new JFXPanel();
 Platform.setImplicitExit(false);
 }

 @AfterClass
 public static void tearDownClass() throws Exception {
 Platform.exit();
 }

 @BeforeMethod
 public void setUpMethod() throws Exception {
 }

 @AfterMethod
 public void tearDownMethod() throws Exception {
 }

    

   

 @Test
 public void testBuilder() throws InterruptedException {
 CountDownLatch l = new CountDownLatch(1);

 Platform.runLater(
 () -> {
 String st = Stream.iterate(0, i -> (i + 1) % 10).limit(36)
 .map(i -> i.toString())
 .reduce("", (a, b) -> a + b);
 VBox tp = new VBox();

 Seq v = new TSeqBuilder(st, Frame.p1).build();
 tp.getChildren().setAll(
 new SeqBuilder(st).build(),
 new TSeqBuilder(st, Frame.p0).build(),
 v,
 new TSeqBuilder(st, Frame.p2).build()
 );
 Scene scene = new Scene(tp, Color.ALICEBLUE);
 Stage stage = new Stage();
 stage.setScene(scene);
 stage.show();
 }
 );
 l.await();

 }

 //    @Test
 public void testSomeMethod() throws InterruptedException {
 CountDownLatch l = new CountDownLatch(1);

 Platform.runLater(
 () -> {

 TilePane v = new TilePane(Orientation.VERTICAL);
 TilePane tp = new tp("123456789");
 TilePane tp1 = new tp("147");
 TilePane tp2 = new tp("2589");
 TilePane tp3 = new tp("369");
 Supplier<Pane> ca = () -> {
 StackPane stackPane = new StackPane();
 stackPane.setPrefSize(30, 8);
 stackPane.minWidthProperty().bind(v.widthProperty().divide(3));
 stackPane.prefWidthProperty().bind(v.widthProperty().divide(3));
 stackPane.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, new Insets(1))));
 return stackPane;
 };
 Function<Double, Pane> cad = (d) -> {
 StackPane stackPane = new StackPane();
 stackPane.setPrefSize(30, 8);
 //                        stackPane.minWidthProperty().bind(v.widthProperty().multiply(d));
 stackPane.prefWidthProperty().bind(v.widthProperty().multiply(d));
 stackPane.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, new Insets(1))));
 return stackPane;
 };
 HBox hb0 = new HBox(ca.get(), ca.get(), ca.get());
 hb0.setSnapToPixel(false);
 HBox hb = new HBox(cad.apply(1. / 9), ca.get(), ca.get(), cad.apply(2. / 9));
 hb.setSnapToPixel(false);
 HBox hb2 = new HBox(cad.apply(2. / 9), ca.get(), ca.get(), cad.apply(1. / 9));
 hb2.setSnapToPixel(false);
 //                    hb.setf
 tp.prefWidthProperty().bind(v.widthProperty());
 tp1.prefWidthProperty().bind(v.widthProperty());
 tp2.prefWidthProperty().bind(v.widthProperty());
 tp3.prefWidthProperty().bind(v.widthProperty());
 //                    tp3.translateXProperty().bind(v.widthProperty().divide(9));
 tp2.translateXProperty().bind(v.widthProperty().multiply(1. / 9));
 tp3.translateXProperty().bind(v.widthProperty().multiply(2. / 9));
 v.getChildren().setAll(tp, tp1, tp2, tp3, hb0, hb, hb2);
 v.setSnapToPixel(false);
 Scene scene = new Scene(v, Color.ALICEBLUE);
 Stage stage = new Stage();
 stage.setScene(scene);
 stage.show();
 }
 );
 JFXPanel jfp = new JFXPanel();

 jfp.paint(null);
 //        System.out.println("after");
 l.await();
 }

 //    @Test
 public void testPdf() throws DocumentException, FileNotFoundException, InterruptedException {
 CountDownLatch l = new CountDownLatch(1);
 String filename = "test.pdf";
 // step 1
 Document document = new Document(new Rectangle(300, 300));
 // step 2
 PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
 // step 3
 document.open();
 // step 4
 PdfContentByte canvas = writer.getDirectContent();

 JFXPanel jp = new JFXPanel();
 System.out.println("new pane");
 Pane p = new StackPane();
 p.getChildren().add(new Text("jfxtext"));
 p.getChildren().setAll(new javafx.scene.shape.Rectangle(100, 100, Color.BLACK));
 System.out.println("setting scene");

 Property<Scene> s = new SimpleObjectProperty<>();
 Platform.runLater(() -> {
 s.setValue(new Scene(p, 300, 300));

 l.countDown();
 });
 //        text.paint(g2);
 //        g2.dispose();
 //        // step 5
 l.await();
 jp.setScene(s.getValue());

 System.out.println("creating panel");
 // Create a Graphics2D object
 float width = 300, height = 300;

 //            JFrame f = new JFrame();
 //            f.add(jp);
 //            f.setVisible(true);
 //            f.pack();
 System.out.println("creating graphics");
 Graphics2D g = new PdfGraphics2D(canvas, width, height);
 // write the text to the Graphics2D
 System.out.println("painting");
 //        jp.paint(g);
 jp.setBounds(0, 0, 300, 300);
 jp.paint(g);

 //        g.drawString("hi", 100, 100);
 //        g.fillRect(0, 0, 100, 100);
 System.out.println("dispose");
 g.dispose();
 CountDownLatch l2 = new CountDownLatch(1);
 System.out.println("close");
 document.close();
 }

 }
 */
