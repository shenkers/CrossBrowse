/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.tabix;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.tabix.PositionalOutputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.LocationAware;
import htsjdk.tribble.Feature;
import htsjdk.tribble.index.DynamicIndexCreator;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.TribbleIndexCreator;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.PositionalBufferedStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 * @param <T> the type of the feature to be written
 */
public class IndexingFeatureWriter<T extends Feature> {

    Logger logger = LogManager.getLogger();

    File featureFile;
    OutputStream outputStream;
    LocationAware filePosition;
    IndexCreator indexCreator;
    Function<T, String> encoder;
    SAMSequenceDictionary refDict;

    /*
     * IndexingTabixWriter writer uses an internal Writer, based by the ByteArrayOutputStream lineBuffer,
     * to temp. buffer the header and per-site output before flushing the per line output
     * in one go to the super.getOutputStream.  This results in high-performance, proper encoding,
     * and allows us to avoid flushing explicitly the output stream getOutputStream, which
     * allows us to properly compress vcfs in gz format without breaking indexing on the fly
     * for uncompressed streams.
     */
    private static final int INITIAL_BUFFER_SIZE = 1024 * 16;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
    /* Wrapping in a {@link BufferedWriter} avoids frequent conversions with individual writes to OutputStreamWriter. */
    private final Writer writer;

    public enum Compression {
        GZIP, BGZF, NONE
    }

    public IndexingFeatureWriter(File featureFile, Compression compression, Function<T, String> encoder, IndexCreator indexCreator, SAMSequenceDictionary refDict) throws FileNotFoundException {
        this.featureFile = featureFile;
        switch (compression) {
            case BGZF: {
                BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(lineBuffer, null);
                filePosition = bcos;
                writer = new BufferedWriter(new OutputStreamWriter(bcos));
                break;
            }
            case GZIP: {
                throw new RuntimeException("GZIP compression not implemented yet");
            }
            case NONE: {
                PositionalOutputStream pos = new PositionalOutputStream(lineBuffer);
                filePosition = pos;
                writer = new BufferedWriter(new OutputStreamWriter(pos));
                break;
            }
            default:{
                throw new RuntimeException(String.format("%s compression not implemented yet",compression.toString()));
            }
        }

        this.outputStream = new BufferedOutputStream(new FileOutputStream(featureFile));
        this.indexCreator = indexCreator;
        this.encoder = encoder;
        this.refDict = refDict;
    }

    /*
     * Write String s to the internal buffered writer.
     *
     * writeAndResetBuffer() must be called to actually write the data to the true output stream.
     *
     * @param s the string to write
     * @throws IOException
     */
    public void add(T feature) throws IOException {
        indexCreator.addFeature(feature, filePosition.getPosition());
        writer.write(encoder.apply(feature));
        writer.write("\n");

        //Actually write the line buffer contents to the destination output 
        //stream.After calling this function the line buffer is reset so the 
        //contents of the buffer can be reused 
        writer.flush();
        outputStream.write(lineBuffer.toByteArray());
        lineBuffer.reset();
    }

    // a constant we use for marking sequence dictionary entries in the Tribble index property list
    private static final String SequenceDictionaryPropertyPredicate = "DICT:";

    public void close() throws IOException {
        outputStream.close();

        // Tribble indexes require a SAMSequenceDictionary
        if (indexCreator instanceof TribbleIndexCreator) {
            for (final SAMSequenceRecord seq : refDict.getSequences()) {
                final String contig = SequenceDictionaryPropertyPredicate + seq.getSequenceName();
                final String length = String.valueOf(seq.getSequenceLength());
                ((TribbleIndexCreator) indexCreator).addProperty(contig, length);
            }
        }

        // close the index stream 
        final Index index = indexCreator.finalizeIndex(filePosition.getPosition());
        index.writeBasedOnFeatureFile(featureFile);
    }
}