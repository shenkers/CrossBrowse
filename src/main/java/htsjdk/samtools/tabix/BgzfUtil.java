/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package htsjdk.samtools.tabix;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.LineIterator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.data.interval.CoordinateOrderComparator;

/**
 *
 * @author sol
 */
public class BgzfUtil {

    private static final Logger logger = LogManager.getLogger ();
    
    public static <T extends Feature> void createBgzFile(
            File gtf_file, 
            File gtf_bgz_file, 
            FeatureCodec<T,LineIterator> codec, 
            Function<T,String> encoder) throws IOException {
        logger.info("reading {}", gtf_file.getAbsolutePath());
        AbstractFeatureReader<T, LineIterator> afr = AbstractFeatureReader.getFeatureReader(gtf_file.getAbsolutePath(), codec, false);
        CloseableTribbleIterator<T> iterator = afr.iterator();
        List<T> gtf = new ArrayList<>();
        while (iterator.hasNext()) {
            T next = iterator.next();
            gtf.add(next);
        }

        logger.info("sorting");
        Collections.sort(gtf, new CoordinateOrderComparator());
        logger.info("writing to compressed output stream");
        BlockCompressedOutputStream os = new BlockCompressedOutputStream(gtf_bgz_file);
        Writer w = new OutputStreamWriter(os);
        for (T feature : gtf) {
            w.write(encoder.apply(feature));
        }
        w.close();
    }
    
    public static void createTabixIndex(File chain_gff_bgz, Function<String,Feature> codec, TabixFormat format) throws IOException {
        TabixIndexCreator indexCreator = new TabixIndexCreator(format);
        BlockCompressedInputStream inputStream = new BlockCompressedInputStream(chain_gff_bgz);

        long p = 0;
        String line = inputStream.readLine();
        

        while (line != null) {
            //add the feature to the index
            Feature decode = codec.apply(line);
            indexCreator.addFeature(decode, p);
            // read the next line if available
            p = inputStream.getFilePointer();
            line = inputStream.readLine();
        }
        // write the index to a file
        Index index = indexCreator.finalizeIndex(inputStream.getFilePointer());
        index.writeBasedOnFeatureFile(chain_gff_bgz);
    }
   
}
