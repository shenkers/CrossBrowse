/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import htsjdk.samtools.tabix.BgzfUtil;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.bed.FullBEDFeature;
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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.gene.GTFCodec;
import org.mskcc.shenkers.control.track.gene.GTFContext;
import org.mskcc.shenkers.control.track.gene.GeneModel;
import org.mskcc.shenkers.control.track.gene.RangeSetCollector;
import org.mskcc.shenkers.data.interval.CoordinateOrderComparator;
import org.mskcc.shenkers.data.interval.IntervalFeature;
import org.mskcc.shenkers.data.interval.IntervalFeatureImpl;

/**
 *
 * @author sol
 */
public class BEDIntervalProvider implements IntervalProvider{

    private static final Logger logger = LogManager.getLogger();

    BEDCodec codec = new BEDCodec();
    
    Function<BEDFeature,String> encoder = (t) -> {
                return String.format("%s\t%d\t%d\n", t.getChr(), t.getStart() - 1, t.getEnd());
            };

    TabixFeatureReader<BEDFeature, LineIterator> features;

    public BEDIntervalProvider(String uri, boolean force_convert, boolean force_index) throws IOException {

        File chain_gff_bgz = new File(uri.concat(".bgz"));
        File chain_gff_bgz_tbi = new File(uri.concat(".bgz.tbi"));

        logger.info("converting {} to {}", uri, chain_gff_bgz.getAbsoluteFile());
        logger.info("{} already exists? {}", chain_gff_bgz.getAbsoluteFile(), chain_gff_bgz.exists());
        if (force_convert || !chain_gff_bgz.exists()) {
            logger.info("bgzipping...");
            BgzfUtil.createBgzFile(new File(uri), chain_gff_bgz, codec, encoder);
        }
        logger.info("{} already exists? {}", chain_gff_bgz_tbi.getAbsoluteFile(), chain_gff_bgz_tbi.exists());
        if (force_index || !chain_gff_bgz_tbi.exists()) {
            logger.info("creating tabix index");
            BgzfUtil.createTabixIndex(chain_gff_bgz, codec::decode, TabixFormat.BED);
        }
        features = new TabixFeatureReader(chain_gff_bgz.getAbsolutePath(), codec);
    }

    @Override
    public Iterable<IntervalFeature> query(String chr, int start, int end) {
        CloseableTribbleIterator<BEDFeature> query = null;
        try {
            query = features.query(chr, start, end);

            int characteristics = 0;
            Stream<IntervalFeature> features = StreamSupport
                    .stream(Spliterators
                            .spliteratorUnknownSize(query, characteristics), true)
                            .map(b -> new IntervalFeatureImpl(b.getChr(), b.getStart(), b.getEnd(), b.getStrand()));
            
            return features::iterator;
        } catch (IOException ex) {
            logger.error("exception reading gene models ", ex);
            throw new RuntimeException(ex);
        } finally {
            query.close();
        }
    }
}
