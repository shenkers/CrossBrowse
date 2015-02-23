/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.liftover.ChainConverter;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.util.AbstractAsyncWriter;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.BasicFeature;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.Feature;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.util.TabixUtils;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.samtools.liftover.AlignmentContext;
import htsjdk.samtools.liftover.GFFAlignmentContextCodec;
import htsjdk.samtools.liftover.GFFAlignmentContextEncoder;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.LocationAware;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.TribbleIndexedFeatureReader;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.readers.PositionalBufferedStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.CharacterStringConverter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.HiddenSidesPane;
import org.fxmisc.easybind.EasyBind;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sol
 */
public class VerticalOverlayNGTest {

    public VerticalOverlayNGTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("starting JFX");
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @AfterClass
    public static void tearDownClass() {
        Platform.exit();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void encodeChain() throws FileNotFoundException, IOException{
        String chain = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain";
        String out = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff.gz";
//        String out = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff";
        
        Writer w = new OutputStreamWriter(new BlockCompressedOutputStream(new File(out)));
//        Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(out)));
        GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(w);
        try {
            ChainConverter cp = new ChainConverter(gace);
            cp.convert(new File(chain));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    @Test
    public void indexGFFChain() throws IOException{
        String filegz = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff.gz";
//        TabixIndexCreator tic = new TabixIndexCreator(TabixFormat.GFF);
//        
         AbstractFeatureReader<AlignmentContext, LineIterator> featureReader = AbstractFeatureReader.getFeatureReader(filegz, new GFFAlignmentContextCodec(), false);
        CloseableTribbleIterator<AlignmentContext> it = featureReader.iterator();
        Map<String,Integer> m = new HashMap<>();
        while(it.hasNext()){
            AlignmentContext next = it.next();
            String key = next.getChr();
            if(m.containsKey(key)){
                m.put(key, Math.max(m.get(key),next.getEnd()));
            }
            else{
                m.put(key, next.getEnd());
            }
            
        }
        System.out.println(m);
        List<SAMSequenceRecord> records = new ArrayList<>();
        for(String key : m.keySet()){
            records.add(new SAMSequenceRecord(key, m.get(key)));
        }
        
       
        SAMSequenceDictionary ssd = new SAMSequenceDictionary(records);
        String file = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff";
//        IndexFactory.createIndex(new File(file), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        FeatureIterator<AlignmentContext, LineIterator> featureIterator = new FeatureIterator<>(new File(filegz), new GFFAlignmentContextCodec());
        TabixIndexCreator indexCreator = new TabixIndexCreator(ssd,TabixFormat.GFF);
        TabixIndex tabixIndex = (TabixIndex) createIndex(new File(filegz), featureIterator, indexCreator);
        tabixIndex.writeBasedOnFeatureFile(new File(filegz));
        
        TabixIndex tabixIndex2 = IndexFactory.createTabixIndex(new File(file), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        TribbleIndexedFeatureReader<AlignmentContext,LineIterator> tifr = new TribbleIndexedFeatureReader(file, new GFFAlignmentContextCodec(), tabixIndex2);
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chrX", 143027611, 143027611);
                 
        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed "+sw.toString());
        }
    }
    
    private static Index createIndex(final File inputFile, final FeatureIterator iterator, final IndexCreator creator) {
        Feature lastFeature = null;
        Feature currentFeature;
        final Map<String, Feature> visitedChromos = new HashMap<String, Feature>(40);
        while (iterator.hasNext()) {
            final long position = iterator.getPosition();
            currentFeature = iterator.next();

            checkSorted(inputFile, lastFeature, currentFeature);
            //should only visit chromosomes once
            final String curChr = currentFeature.getChr();
            final String lastChr = lastFeature != null ? lastFeature.getChr() : null;
            if(!curChr.equals(lastChr)){
                if(visitedChromos.containsKey(curChr)){
                    String msg = "Input file must have contiguous chromosomes.";
                    msg += " Saw feature " + featToString(visitedChromos.get(curChr));
                    msg += " followed later by " + featToString(lastFeature);
                    msg += " and then " + featToString(currentFeature);
                    throw new TribbleException.MalformedFeatureFile(msg, inputFile.getAbsolutePath());
                }else{
                    visitedChromos.put(curChr, currentFeature);
                }
            }

            creator.addFeature(currentFeature, position);

            lastFeature = currentFeature;
        }

        iterator.close();
        return creator.finalizeIndex(iterator.getPosition());
    }

    private static String featToString(final Feature feature){
        return feature.getChr() + ":" + feature.getStart() + "-" + feature.getEnd();
    }

    private static void checkSorted(final File inputFile, final Feature lastFeature, final Feature currentFeature){
        // if the last currentFeature is after the current currentFeature, exception out
        if (lastFeature != null && currentFeature.getStart() < lastFeature.getStart() && lastFeature.getChr().equals(currentFeature.getChr()))
            throw new TribbleException.MalformedFeatureFile("Input file is not sorted by start position. \n" +
                    "We saw a record with a start of " + currentFeature.getChr() + ":" + currentFeature.getStart() +
                    " after a record with a start of " + lastFeature.getChr() + ":" + lastFeature.getStart(), inputFile.getAbsolutePath());
    }


    /**
     * Iterator for reading features from a file, given a {@code FeatureCodec}.
     */
    static class FeatureIterator<FEATURE_TYPE extends Feature, SOURCE> implements CloseableTribbleIterator<Feature> {
        // the stream we use to get features
        private final SOURCE source;
        // the next feature
        private Feature nextFeature;
        // our codec
        private final FeatureCodec<FEATURE_TYPE, SOURCE> codec;
        private final File inputFile;

        // we also need cache our position
        private long cachedPosition;

        /**
         *
         * @param inputFile The file from which to read. Stream for reading is opened on construction.
         * @param codec
         */
        public FeatureIterator(final File inputFile, final FeatureCodec<FEATURE_TYPE, SOURCE> codec) {
            this.codec = codec;
            this.inputFile = inputFile;
            final FeatureCodecHeader header = readHeader();
            source = (SOURCE) codec.makeIndexableSourceFromStream(initStream(inputFile, header.getHeaderEnd()));
            readNextFeature();
        }

        /**
         * Some codecs,  e.g. VCF files,  need the header to decode features.  This is a rather poor design,
         * the internal header is set as a side-affect of reading it, but we have to live with it for now.
         */
        private FeatureCodecHeader readHeader() {
            try {
                final SOURCE source = this.codec.makeSourceFromStream(initStream(inputFile, 0));
                final FeatureCodecHeader header = this.codec.readHeader(source);
                codec.close(source);
                return header;
            } catch (final IOException e) {
                throw new TribbleException.InvalidHeader("Error reading header " + e.getMessage());
            }
        }

        private PositionalBufferedStream initStream(final File inputFile, final long skip) {
            try {
                final FileInputStream is = new FileInputStream(inputFile);
                final PositionalBufferedStream pbs = new PositionalBufferedStream(new BlockCompressedInputStream(is));
                if ( skip > 0 ) pbs.skip(skip);
                return pbs;
            } catch (final FileNotFoundException e) {
                throw new TribbleException.FeatureFileDoesntExist("Unable to open the input file, most likely the file doesn't exist.", inputFile.getAbsolutePath());
            } catch (final IOException e) {
                throw new TribbleException.MalformedFeatureFile("Error initializing stream", inputFile.getAbsolutePath(), e);
            }
        }

        public boolean hasNext() {
            return nextFeature != null;
        }

        public Feature next() {
            final Feature ret = nextFeature;
            readNextFeature();
            return ret;
        }

        /**
         * @throws UnsupportedOperationException
         */
        public void remove() {
            throw new UnsupportedOperationException("We cannot remove");
        }


        /**
         * @return the file position from the underlying reader
         */
        public long getPosition() {
            return (hasNext()) ? cachedPosition : ((LocationAware) source).getPosition();
        }

        @Override
        public Iterator<Feature> iterator() {
            return this;
        }

        @Override
        public void close() {
            codec.close(source);
        }

        /**
         * Read the next feature from the stream
         * @throws TribbleException.MalformedFeatureFile
         */
        private void readNextFeature() {
            cachedPosition = ((LocationAware) source).getPosition();
            try {
                nextFeature = null;
                while (nextFeature == null && !codec.isDone(source)) {
                    nextFeature = codec.decodeLoc(source);
                }
            } catch (final IOException e) {
                throw new TribbleException.MalformedFeatureFile("Unable to read a line from the file", inputFile.getAbsolutePath(), e);
            }
        }
    }
    
//    @Test
//    public void decodeGFFChain() throws FileNotFoundException, IOException{
//         String file = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff.gz";
//       
//         
//         AbstractFeatureReader<AlignmentContext, LineIterator> featureReader = AbstractFeatureReader.getFeatureReader(file, new GFFAlignmentContextCodec(), true);
//        CloseableTribbleIterator<AlignmentContext> it
//               // = //                featureReader.iterator();
//                //        chr2R	sol	8mer	5089312	5089319	.	+	.	sequence "CACGCACC";co
//                = featureReader.query("chrX", 143027611, 143027611);
//        while (it.hasNext()) {
//            it.next();
//        }
//    }

    @Test
    public void testTabix() throws IOException {
        String file = "C:/Users/Soma/Downloads/drosophilia.conserved.8mers.gff.gz";

        class cfc extends AsciiFeatureCodec<AlignmentContext> {

            protected cfc(Class<AlignmentContext> c) {
                super(c);
            }

            @Override
            public Object readActualHeader(LineIterator reader) {
                return null;
            }

            @Override
            public AlignmentContext decode(String s) {
                System.out.println("decoding -> " + s);
                return new AlignmentContext("x", 1, 2);
            }
        };

        // can implement one of these to write our format
//                AbstractAsyncWriter
        AbstractFeatureReader<AlignmentContext, LineIterator> featureReader = AbstractFeatureReader.getFeatureReader(file, new cfc(AlignmentContext.class), true);
        CloseableTribbleIterator<AlignmentContext> it
                = //                featureReader.iterator();
                //        chr2R	sol	8mer	5089312	5089319	.	+	.	sequence "CACGCACC";co
                it = featureReader.query("chr2R", 5089312, 5089312);
        while (it.hasNext()) {
            it.next();
        }
//        TabixFeatureReader tfr = new TabixFeatureReader("", afc);
//        TabixUtils.less64(u, v)
//                TabixIndex
//        new TabixIndexCreator(TabixFormat.PSLTBL)
    }

//    @Test
    public void testHidden() throws InterruptedException {
        System.out.println("testHidden");
        HiddenSidesPane p = new HiddenSidesPane();

        p.setContent(new Label("middle"));
        ScrollBar sb = new ScrollBar();
        sb.setOrientation(Orientation.HORIZONTAL);
        p.setBottom(sb);
        p.setAnimationDuration(Duration.millis(1));
        p.setAnimationDelay(Duration.ZERO);
        p.setOnMouseEntered(e -> {
            p.setPinnedSide(Side.BOTTOM);
        });
        p.setOnMouseExited(e -> {
            p.setPinnedSide(null);
        });
        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    System.err.println("running");
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }

    @Test
    public void testStackIntervalView() throws InterruptedException {
        System.out.println("testStackIntervalView");
        int[][] d = new int[][]{
            {2, 6}, {7, 10}, {1, 3}, {4, 6}, {8, 10}, {1, 2}, {3, 7},
            {9, 10}, {1, 2}, {3, 5}, {6, 7}, {8, 10}, {2, 5}, {8, 10}
        };
        List<int[]> asList = Arrays.asList(d);
        Collections.sort(asList, new Comparator<int[]>() {

            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });
        List<TreeRangeSet<Integer>> rows = new ArrayList<>();
        rows.add(TreeRangeSet.create());
        for (int[] r : d) {
            Range<Integer> R = Range.closed(r[0], r[1]);
            int i = 0;
            added:
            {
                while (i < rows.size()) {
                    TreeRangeSet<Integer> set = rows.get(i);
                    RangeSet<Integer> intersection = set.subRangeSet(Range.closed(r[0] - 1, r[1] + 1));
                    if (intersection.isEmpty()) {
                        set.add(R);
                        break added;
                    }
                    i++;
                }
//                Stri i = ;
                TreeRangeSet<Integer> row = TreeRangeSet.create();
                row.add(R);
                rows.add(row);
            }
        }
        TilePane p = new TilePane();
        p.setSnapToPixel(false);
        for (int i = 0; i < rows.size(); i++) {
            p.getChildren().add(get(rows.get(i),0,11));
            System.out.println(rows.get(i).toString());
            StringBuilder sb = new StringBuilder(11);
            sb.append(StringUtils.repeat(".", 11));
            for (int j = 0; j < 11; j++) {
                if (rows.get(i).contains(j)) {
                    sb.setCharAt(j, 'X');
                }
            }
            System.out.println(sb.toString());
        }
//        p.prefWidth(100);
//        p.prefHeight(100);
        ScrollPane sp = new ScrollPane(p);
        ScrollBar sb = new ScrollBar();
        sb.maxProperty().bind(sp.vmaxProperty());
        sb.minProperty().bind(sp.vminProperty());
        sb.visibleAmountProperty().bind(sp.heightProperty().divide(p.prefHeightProperty()));
        sb.setOrientation(Orientation.VERTICAL);
        sp.vvalueProperty().bindBidirectional(sb.valueProperty());
        HiddenSidesPane hsp = new HiddenSidesPane();
        hsp.setContent(sp);
        hsp.setRight(sb);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        p.setOrientation(Orientation.VERTICAL);
        p.prefTileHeightProperty().bind(new SimpleDoubleProperty(40));
//        p.minHeightProperty().bind(new SimpleDoubleProperty(20).multiply(Bindings.size(p.getChildren())));
        p.prefTileWidthProperty().bind(sp.widthProperty());
        p.prefHeightProperty().bind(new SimpleDoubleProperty(50).multiply(Bindings.size(p.getChildren())).subtract(10));
        p.prefWidthProperty().bind(sp.widthProperty());
        sp.setPadding(Insets.EMPTY);
        p.setVgap(10);
        
        
        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                   
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                    });
                    Scene scene = new Scene(hsp, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        l.await();
    }

    public Node get(RangeSet<Integer> intervals, int start, int end) {
        RangeSet<Integer> view = intervals.subRangeSet(Range.closed(start, end));
        double l = end - start + 1.;
        Pane p = new Pane();
        for (Range<Integer> interval : view.asRanges()) {
            Rectangle r = new Rectangle();
            r.widthProperty().bind(p.widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint() + 1).divide(l));
            r.heightProperty().bind(p.heightProperty());
            r.xProperty().bind(p.widthProperty().multiply(interval.lowerEndpoint()).divide(l));
//            System.out.println(r);
            p.getChildren().add(r);
        }
        return p;
    }

//    @Test
    public void testIntervalView() throws InterruptedException {
        System.out.println("testIntervalView");
        Pane p = new Pane();

        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    System.out.println("running");
                    double[][] intervals = {
                        {.1, .2}
                    };
//                    Range r = null;
                    RangeSet<Double> rs = TreeRangeSet.create();
                    rs.add(Range.closed(.1, .2));
                    rs.add(Range.closed(.2, .3));
                    rs.add(Range.closed(.32, .35));
                    rs.add(Range.closed(.6, .8));

                    for (Range<Double> r : rs.asRanges()) {
                        System.out.println(r.lowerEndpoint() + " - " + r.upperEndpoint());
                    }
                    for (Range<Double> interval : rs.asRanges()) {
                        Rectangle r = new Rectangle();
                        r.widthProperty().bind(p.widthProperty().multiply(interval.upperEndpoint() - interval.lowerEndpoint()));
                        r.heightProperty().bind(p.heightProperty());
                        r.xProperty().bind(p.widthProperty().multiply(interval.lowerEndpoint()));
                        p.getChildren().add(r);
                    }
//                    p.prefTileHeightProperty().bind(p.heightProperty());
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }

//    @Test
    public void testStrechString2() throws InterruptedException {
        System.out.println("testStrechString2");
//         DoubleProperty xProperty = r.xProperty();
//        DoubleProperty yProperty = r.yProperty();
//        DoubleProperty mouseX = new SimpleDoubleProperty();
//        DoubleProperty anchorX = new SimpleDoubleProperty();
//        DoubleProperty mouseY = new SimpleDoubleProperty();
//        DoubleProperty anchorY = new SimpleDoubleProperty();
//        
//        p.setOnMousePressed(new EventHandler<MouseEvent>(){
//
//            @Override
//            public void handle(MouseEvent event) {
//                System.out.println("starting drag");
//                mouseX.setValue(event.getX());
//                anchorX.setValue(r.getX());
//      mouseY.setValue(event.getY());
//                anchorY.setValue(r.getY());
//            }
//        });
//        DoubleProperty draggedX = new SimpleDoubleProperty();
//        DoubleProperty draggedY = new SimpleDoubleProperty();
//        
//        
//        DoubleBinding offsetX = Bindings.createDoubleBinding(()->{
//            double delta = draggedX.get() - mouseX.get();
//             double floored = 50*Math.round(delta/50);
//           return anchorX.get() + floored;}, anchorX, draggedX, mouseX);
//        DoubleBinding offsetY = Bindings.createDoubleBinding(()->{
//             double delta = draggedY.get() - mouseY.get();
//            double floored = 50*Math.round(delta/50);
//            return anchorY.get() + floored;}, anchorY, draggedY, mouseY);
////        xProperty.bind(offsetX);
////        yProperty.bind(offsetY);
////        xProperty.set(anchorX.getValue()+e.getX()-mouseX.doubleValue());
////       yProperty.set(anchorY.getValue()+e.getY()-mouseY.doubleValue());
//       
//        
//        // todo : separate flip from genomic span
//        // enable dragging
//        // predictive features of apa
//        // tissue specificity by regression
//        // one class svm
//        p.setOnMouseDragged((MouseEvent e)->{
//            draggedX.set(e.getX());
//            draggedY.set(e.getY());
//        
//            xProperty.set(offsetX.get());
//            yProperty.set(offsetY.get());
//        });
        TilePane p = new TilePane();

        class cc implements EventHandler<MouseEvent> {

            double pressedX;
            double releasedX;
            DoubleProperty deltaX = new SimpleDoubleProperty();
            DoubleBinding flooredDelta;
            IntegerBinding nucleotideDelta;

            public cc(ObservableIntegerValue nucleotideWidth, ObservableDoubleValue viewWidth) {
                nucleotideDelta = Bindings.createIntegerBinding(() -> {
                    double width = viewWidth.get() / nucleotideWidth.get();
                    int nucleotideDelta = (int) Math.round(deltaX.get() / width);
                    return nucleotideDelta;
                }, deltaX, nucleotideWidth, viewWidth);
                flooredDelta = Bindings.createDoubleBinding(() -> {
                    double width = viewWidth.get() / nucleotideWidth.get();
                    double floored = width * Math.round(deltaX.get() / width);
                    return floored;
                }, deltaX, nucleotideWidth, viewWidth);
            }

            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    System.out.println("pressed");
                    pressedX = event.getScreenX();
                }
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    System.out.println("dragged " + deltaX);
                    deltaX.set(event.getScreenX() - pressedX);
                }
                if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    System.out.println("released, offsetting coordinates by " + nucleotideDelta.get());

                    int offset = nucleotideDelta.get();
                    System.out.println("total offset " + offset);
                    List<Node> nodes = new ArrayList<>();
                    ObservableList<Node> children = p.getChildren();
                    int shift = ((offset % 26) + 26) % 26;
                    System.out.println("shift " + shift);
                    nodes.addAll(children.subList(26 - shift, 26));
                    nodes.addAll(children.subList(0, 26 - shift));

//                     for (int i = 0; i < 26; i++) {
//                        Text txt = new Text((char) ('A' + ((i-offset+26)%26)) + "");
//                        txt.setFont(Font.font("Monospaced"));
////      txt.setX(10);
////      txt.setY(10);
//                        System.out.println(txt.getBoundsInLocal().getWidth());
//                        StackPane pane = new StackPane();
//                        pane.getChildren().add(txt);
//                        pane.setBackground(new Background(new BackgroundFill(new Color(Math.random(), Math.random(), Math.random(), .1), CornerRadii.EMPTY, Insets.EMPTY)));
////                pane.getChildren().add(txt);
//                        nodes.add(pane);
//                    }
                    p.getChildren().setAll(nodes);

                    deltaX.set(0);
                }
            }

        }

        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    IntegerBinding nChars = Bindings.size(p.getChildren());
                    cc dragListener = new cc(nChars, p.widthProperty());

                    p.translateXProperty().bind(dragListener.flooredDelta);

//            p.layoutXProperty().bind(dragListener.deltaX);
                    p.addEventHandler(MouseEvent.ANY, dragListener);
//                    for (int i = 0; i < 26; i++) {
//                        Text txt = new Text((char) ('a' + i) + "");
//                        txt.setFont(Font.font("Monospaced"));
//
//                        StackPane pane = new StackPane();
//                        pane.getChildren().add(txt);
//                        pane.setBackground(new Background(new BackgroundFill(new Color(Math.random(), Math.random(), Math.random(), .1), CornerRadii.EMPTY, Insets.EMPTY)));
////                pane.getChildren().add(txt);
//                        p.getChildren().add(pane);
//                    }
                    for (int i = 0; i < 26; i++) {
                        Text txt = new Text((char) ('A' + i) + "");
                        txt.setFont(Font.font("Monospaced"));
//      txt.setX(10);
//      txt.setY(10);
                        System.out.println(txt.getBoundsInLocal().getWidth());
                        StackPane pane = new StackPane();
                        pane.getChildren().add(txt);
                        pane.setBackground(new Background(new BackgroundFill(new Color(Math.random(), Math.random(), Math.random(), .1), CornerRadii.EMPTY, Insets.EMPTY)));
//                pane.getChildren().add(txt);
                        p.getChildren().add(pane);
                    }
                    System.out.println(p.getHgap());
                    p.setSnapToPixel(false);
                    p.prefTileWidthProperty().bind(p.widthProperty().subtract(1).divide(nChars));
                    p.visibleProperty().bind(p.widthProperty().subtract(1).divide(nChars).greaterThan(7.));
                    p.setTileAlignment(Pos.CENTER);
//                    p.prefTileHeightProperty().bind(p.heightProperty());
                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }

//    @Test
    public void testStrechString() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);
        System.out.println("before");
        Platform.runLater(
                () -> {
                    Pane r1 = new Pane();
                    Text lbl = new Text("H");

                    List<Node> collect = StringUtils.repeat('a', 20).chars().mapToObj(new IntFunction<StretchStringPane>() {

                        @Override
                        public StretchStringPane apply(int value) {
                            return new StretchStringPane("" + ((char) value));
                        }
                    }).collect(Collectors.toList());

                    VerticalOverlay ao = new VerticalOverlay();
                    ao.setChildren(collect);
                    ao.flip();

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> l.countDown());
                    Scene scene = new Scene(ao.getContent(), 300, 300, Color.GRAY);
                    stage.setTitle("JavaFX Scene Graph Demo");
                    stage.setScene(scene);
                    stage.show();
                    System.out.println("done");

                }
        );
        System.out.println("after");
        l.await();
        Thread.sleep(1000);
    }

//    @Test
    public void testSomeMethod() {
//        launch(new String[0]);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
