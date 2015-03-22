/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import htsjdk.samtools.tabix.BgzfUtil;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.LineReader;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIterator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.mskcc.shenkers.control.track.DomainFlippable;
import org.mskcc.shenkers.view.GenePartIntervalNode;
import org.mskcc.shenkers.view.GeneViewBuilder;
import org.mskcc.shenkers.view.GenericStackedIntervalView;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.ListMultiMap;

/**
 *
 * @author sol
 */
public class GTFCodecNGTest {

    public GTFCodecNGTest() {
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
    public void testBlend() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);

        Pane p = new Pane();
        Color paint = Color.BLACK;
        Circle c = new Circle(150, 150, 100);
        c.setFill(paint);
//        c.setBlendMode(BlendMode.ADD);
//        Rectangle t = new Rectangle(40, 150, 40, 40);
//        Rectangle t = new Rectangle(40, 150, 40, 40);
        Text t = new Text(40, 150, "text");

        t.setFill(paint);

//        g.setBlendMode(BlendMode.DIFFERENCE);
        Shape intersect = Shape.intersect(c, t);
        intersect.setFill(Color.RED);
        Shape subtract = Shape.subtract(t, c);
        subtract.setFill(Color.GREEN);
        Shape subtract2 = Shape.subtract(c, t);
        subtract2.setFill(Color.BLUE);
//        intersect.setFill(paint.invert());
        Group g = new Group(subtract, subtract2, intersect);
        p.getChildren().setAll(g);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });

                    Scene scene = new Scene(p, 300, 300, Color.GRAY);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
    }

    /**
     * Test of readActualHeader method, of class GTFGeneCodec.
     */
//    @Test
    public <T extends Pane & DomainFlippable> void testReadActualHeader() throws IOException, InterruptedException {
        System.out.println("readActualHeader");
        GTFCodec codec = new GTFCodec();
        AbstractFeatureReader<GTFContext, LineIterator> afr = AbstractFeatureReader.getFeatureReader("/home/sol/data/gene_models/Drosophila_melanogaster.BDGP5.69.gtf", codec, false);
        CloseableTribbleIterator<GTFContext> iterator = afr.iterator();
        List<GTFContext> gtf = new ArrayList<>();
        while (iterator.hasNext()) {
            GTFContext next = iterator.next();
            gtf.add(next);
//            System.out.println(
//            codec.encodeToString(next));
        }
        System.out.println("sorting");
        Collections.sort(gtf, new CoordinateOrderComparator());
        System.out.println("done, bgzipping");
        File file_bgz = new File("test.gtf.gz");
        BlockCompressedOutputStream os = new BlockCompressedOutputStream(file_bgz);
        Writer w = new OutputStreamWriter(os);
        for (GTFContext feature : gtf) {
            w.write(codec.encodeToString(feature));
        }
        w.close();
        System.out.println("done, indexing");
        BgzfUtil.createTabixIndex(file_bgz, codec::decode, TabixFormat.GFF);
        System.out.println("done, querying");
        AbstractFeatureReader<GTFContext, LineIterator> featureReader = AbstractFeatureReader.getFeatureReader(file_bgz.getAbsolutePath(), codec, true);
//        3R:1050932-1065576
//        chr3R:1058118-1063042
        int start = 1058118;
        int end = 1063042;
        CloseableTribbleIterator<GTFContext> query = featureReader.query("chr3R", start, end);
        Pattern p = Pattern.compile("([^ ]+) \"([^\"]*)\"");

        Stream<GTFContext> filter = StreamSupport.stream(Spliterators.spliteratorUnknownSize(query, Spliterator.CONCURRENT), true)
                .filter((c) -> {
                    System.out.println(c);
                    System.out.println(codec.encodeToString(c));
                    String attributes = c.getAttributes();
                    String[] split = attributes.split(";");
                    Set<String> attributeKeys = Stream.of(split).map(str -> {
                        Matcher matcher = p.matcher(str);
                        System.out.println("matching: " + str);
                        matcher.find();
                        return matcher.group(1);
                    }
                    ).collect(Collectors.toSet());
                    return attributeKeys.contains("transcript_id");
                });

        ImmutableListMultimap<String, GTFContext> transcript_id_multimap = Multimaps.index(filter.iterator(), c -> {
            String attributes = c.getAttributes();
            String[] split = attributes.split(";");
            Map<String, String> attributeMap = Stream.of(split).map(str -> {
                System.out.println("mapping: " + str);
                Matcher matcher = p.matcher(str);
                matcher.find();
                return new String[]{matcher.group(1), matcher.group(2)};
            }
            ).collect(Collectors.toMap(s -> s[0], s -> s[1]));
            return attributeMap.get("transcript_id");
        });

        // write to bgzipped file
        // index file
        // query file
        // aggregate by transcript id
        // create separate gene model for each transcript
        // stack into stacked interval view
        class RangeSetCollector implements Collector<Range<Integer>, RangeSet<Integer>, RangeSet<Integer>> {

            @Override
            public Supplier<RangeSet<Integer>> supplier() {
                return () -> TreeRangeSet.create();
            }

            @Override
            public BiConsumer<RangeSet<Integer>, Range<Integer>> accumulator() {
                return (set, range) -> set.add(range);
            }

            @Override
            public BinaryOperator<RangeSet<Integer>> combiner() {
                return (set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                };
            }

            @Override
            public Function<RangeSet<Integer>, RangeSet<Integer>> finisher() {
                return set -> set;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED);
            }

        }

        List<Pair<Integer, Integer>> range = new ArrayList<>();
        List<T> nodes = new ArrayList<>();
        for (String key : transcript_id_multimap.keySet()) {
            System.out.println(key);
            ImmutableListMultimap<String, GTFContext> transcript_features = Multimaps.index(transcript_id_multimap.get(key), GTFContext::getFeature);
            Map<String, RangeSet<Integer>> collect = transcript_features.keySet().stream().collect(Collectors.toMap(s -> s, feature -> {
                return transcript_features.get(feature)
                        .stream()
                        .map(c -> Range.closed(c.getStart(), c.getEnd()))
                        .collect(new RangeSetCollector());
            }));

            Range<Integer> span = collect.get("exon").span();
            range.add(new Pair(span.lowerEndpoint(), span.upperEndpoint()));
            System.out.println("span: " + span);
            T view = (T) new GeneViewBuilder(collect.get("transcript").span(), Optional.ofNullable(collect.get("exon")), Optional.ofNullable(collect.get("CDS")).map(c -> c.span())).getView(start, end);
            nodes.add(view);

        }

        GenericStackedIntervalView gsiv2 = new GenericStackedIntervalView(start, end);
        gsiv2.setData(range, nodes);

        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(
                () -> {

                    Stage stage = new Stage();
                    stage.setOnHidden(e -> {
                        l.countDown();
                        System.out.println("count " + l.getCount());
                    });
                    BorderPane bp = new BorderPane(gsiv2);

                    gsiv2.setPrefTileHeight(20);
                    gsiv2.prefTileWidthProperty().bind(bp.widthProperty());

                    gsiv2.setVgap(5);
                    Scene scene = new Scene(bp, 300, 300, Color.GRAY);
                    stage.setTitle("SimpleIntervalView");
                    stage.setScene(scene);
                    stage.show();

                }
        );
        System.out.println("after");
        l.await();
    }

}
