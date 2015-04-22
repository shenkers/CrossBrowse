/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import org.mskcc.shenkers.data.interval.CoordinateOrderComparator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import htsjdk.samtools.tabix.BgzfUtil;
import htsjdk.samtools.tabix.ChainContext;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.LineIterator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.DomainFlippable;
import org.mskcc.shenkers.view.GeneViewBuilder;

/**
 *
 * @author sol
 */
public class GTFGeneModelProvider<T extends Pane & DomainFlippable> implements GeneModelProvider {

    private static final Logger logger = LogManager.getLogger();

    GTFCodec codec = new GTFCodec();

    TabixFeatureReader<GTFContext, LineIterator> features;

    public GTFGeneModelProvider(String uri, boolean force_convert, boolean force_index) throws IOException {

        File chain_gff_bgz = new File(uri.concat(".bgz"));
        File chain_gff_bgz_tbi = new File(uri.concat(".bgz.tbi"));

        logger.info("converting {} to {}", uri, chain_gff_bgz.getAbsoluteFile());
        logger.info("{} already exists? {}", chain_gff_bgz.getAbsoluteFile(), chain_gff_bgz.exists());
        if (force_convert || !chain_gff_bgz.exists()) {
            logger.info("bgzipping...");
            createGtfBgz(new File(uri), chain_gff_bgz);
        }
        logger.info("{} already exists? {}", chain_gff_bgz_tbi.getAbsoluteFile(), chain_gff_bgz_tbi.exists());
        if (force_index || !chain_gff_bgz_tbi.exists()) {
            logger.info("creating tabix index");
            BgzfUtil.createTabixIndex(chain_gff_bgz, codec::decode, TabixFormat.GFF);
        }
        features = new TabixFeatureReader(chain_gff_bgz.getAbsolutePath(), codec);
    }

    @Override
    public Iterable<GeneModel> query(String chr, int start, int end) {
        CloseableTribbleIterator<GTFContext> query = null;
        try {
            query = features.query(chr, start, end);

            Stream<GTFContext> filter = StreamSupport.stream(Spliterators.spliteratorUnknownSize(query, Spliterator.CONCURRENT), true)
                    .filter(c -> c.getTranscriptId() != null);

            ImmutableListMultimap<String, GTFContext> transcript_id_multimap = Multimaps.index(filter.iterator(), GTFContext::getTranscriptId);

            Stream<GeneModel> map = transcript_id_multimap.keySet().stream().map(key -> {
                System.out.println(key);
                ImmutableListMultimap<String, GTFContext> transcript_features = Multimaps.index(transcript_id_multimap.get(key), GTFContext::getFeature);
                Map<String, RangeSet<Integer>> collect = transcript_features.keySet().stream().collect(Collectors.toMap(s -> s, feature -> {
                    return transcript_features.get(feature)
                            .stream()
                            .map(c -> Range.closed(c.getStart(), c.getEnd()))
                            .collect(new RangeSetCollector());
                }));

                GeneModel model = new GeneModel(collect.get("transcript").span(), Optional.ofNullable(collect.get("exon")), Optional.ofNullable(collect.get("CDS")).map(c -> c.span()));
                return model;
            });

            return map::iterator;
        } catch (IOException ex) {
            logger.error("exception reading gene models ", ex);
            throw new RuntimeException(ex);
        } finally {
            query.close();
        }
    }

    private void createGtfBgz(File gtf_file, File gtf_bgz_file) throws IOException {
        logger.info("reading {}", gtf_file.getAbsolutePath());
        AbstractFeatureReader<GTFContext, LineIterator> afr = AbstractFeatureReader.getFeatureReader(gtf_file.getAbsolutePath(), codec, false);
        CloseableTribbleIterator<GTFContext> iterator = afr.iterator();
        List<GTFContext> gtf = new ArrayList<>();
        while (iterator.hasNext()) {
            GTFContext next = iterator.next();
            gtf.add(next);
        }
        ImmutableListMultimap<String, GTFContext> transcript_id_multimap = Multimaps.index(gtf.iterator(), GTFContext::getTranscriptId);

        logger.info("adding transcript ranges");
        gtf.addAll(transcript_id_multimap.keySet().stream().map(key -> {
            System.out.println(key);
            ImmutableList<GTFContext> contexts = transcript_id_multimap.get(key);
            Range<Integer> span
                    = contexts
                    .stream()
                    .map(c -> Range.closed(c.getStart(), c.getEnd()))
                    .collect(new RangeSetCollector()).span();

            GTFContext context = new GTFContext(contexts.get(0).getChr(), span.lowerEndpoint(), span.upperEndpoint());
            context.setFeature("transcript");
            context.setFrame(".");
            context.setName(".");
            context.setScore(".");
            context.setSource(".");
            context.setStrand('.');
            context.setAttributes(String.format("transcript_id \"%s\";", key));
            return context;
        }).collect(Collectors.toList()));

        logger.info("sorting");
        Collections.sort(gtf, new CoordinateOrderComparator());
        logger.info("writing to compressed output stream");
        BlockCompressedOutputStream os = new BlockCompressedOutputStream(gtf_bgz_file);
        Writer w = new OutputStreamWriter(os);
        for (GTFContext feature : gtf) {
            w.write(codec.encodeToString(feature));
        }
        w.close();
    }

    /*
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
     T view = (T) new GeneViewBuilder(collect.get("exon"), Optional.of(collect.get("CDS")).map(c -> c.span())).getView(start, end);
     nodes.add(view);

     }
     */
}
