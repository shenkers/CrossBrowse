/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package htsjdk.samtools.tabix;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class BgzfUtil {

    private static final Logger logger = LogManager.getLogger ();
    
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
