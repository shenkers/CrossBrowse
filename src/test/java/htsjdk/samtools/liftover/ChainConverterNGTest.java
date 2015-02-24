/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.samtools.tabix.AlignmentContext;
import htsjdk.samtools.tabix.GFFAlignmentContextEncoder;
import htsjdk.samtools.tabix.GFFAlignmentContextCodec;
import htsjdk.samtools.SAMFileWriterImpl;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.LocationAware;
import htsjdk.samtools.util.SortingCollection;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.Tribble;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.TribbleIndexedFeatureReader;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.LineReaderUtil;
import htsjdk.tribble.readers.PositionalBufferedStream;
import htsjdk.tribble.util.TabixUtils;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ChainConverterNGTest {

    public ChainConverterNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    /**
     * Test of convert method, of class ChainConverter.
     */
    @Test
    public void testConvert() throws Exception {
        System.out.println("convert");

        String chain = "/home/sol/Downloads/hg38ToHg19.over.chain";
//        String out = "/home/sol/Downloads/hg38ToHg19.over.chain.gff.gz";
        String out = "/home/sol/Downloads/hg38ToHg19.over.chain.gff";
//        String out = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff";

//        Writer w = new OutputStreamWriter(new BlockCompressedOutputStream(new File(out)));
        Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(out)));
        GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(w);
        try {
            ChainGFFConverter cp = new ChainGFFConverter(new GFFAlignmentContextCodec(), gace);
            cp.convert(new File(chain));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

//    @Test(dependsOnMethods = {"testConvert"})
    public void indexGFFChain() throws IOException {
//        String filegz = "/home/sol/Downloads/hg38ToHg19.over.chain.gff.gz";
        String file = "/home/sol/Downloads/hg38ToHg19.over.chain.gff";
        AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File(file))));
        String line = asciiLineReader.readLine();
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        Map<String, Integer> m = new HashMap<>();
        while (line != null) {
            AlignmentContext next = codec.decode(line);
            String key = next.getChr();
            if (m.containsKey(key)) {
                m.put(key, Math.max(m.get(key), next.getEnd()));
            } else {
                m.put(key, next.getEnd());
            }
            line = asciiLineReader.readLine();
        }

        System.out.println(m);
        List<SAMSequenceRecord> records = new ArrayList<>();
        for (String key : m.keySet()) {
            records.add(new SAMSequenceRecord(key, m.get(key)));
        }

//       tfr.
        SAMSequenceDictionary ssd = new SAMSequenceDictionary(records);

        TabixIndex tabixIndex2 = IndexFactory.createTabixIndex(new File(file), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        tabixIndex2.writeBasedOnFeatureFile(new File(file));

        TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, new GFFAlignmentContextCodec(), tabixIndex2);
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed " + sw.toString());
        }

//        IndexFactory.createTabixIndex(null, codec, TabixFormat.GFF, ssd) //
        ////        IndexFactory.createIndex(new File(file), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        //        FeatureIterator<AlignmentContext, LineIterator> featureIterator = new FeatureIterator<>(new File(filegz), new GFFAlignmentContextCodec());
        //        TabixIndexCreator indexCreator = new TabixIndexCreator(ssd, TabixFormat.GFF);
        //        TabixIndex tabixIndex = (TabixIndex) createIndex(new File(filegz), featureIterator, indexCreator);
        //        tabixIndex.writeBasedOnFeatureFile(new File(filegz));
        //
        //        if (false) {
        //            String file = "C:/Users/Soma/git/hts/testdata/htsjdk/samtools/liftover/hg18ToHg19.over.chain.gff";
        //            TabixIndex tabixIndex2 = IndexFactory.createTabixIndex(new File(file), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        //            TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, new GFFAlignmentContextCodec(), tabixIndex2);
        //            CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chrX", 143027611, 143027611);
        //
        //            while (itt.hasNext()) {
        //                AlignmentContext next = itt.next();
        //                StringWriter sw = new StringWriter();
        //                GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
        //                gace.encode(next);
        //                System.out.println("successfully parsed " + sw.toString());
        //            }
        //        }
        //
        //        AbstractFeatureReader<AlignmentContext, LineIterator> featureReader2 = AbstractFeatureReader.getFeatureReader(filegz, new GFFAlignmentContextCodec(), true);
        //        CloseableTribbleIterator<AlignmentContext> it2
        //                // = //                featureReader.iterator();
        //                //        chr2R	sol	8mer	5089312	5089319	.	+	.	sequence "CACGCACC";co
        //                = featureReader2.query("chr15", 23307198, 23307198);
        //        while (it2.hasNext()) {
        //            AlignmentContext next = it2.next();
        //            StringWriter sw = new StringWriter();
        //            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
        //            gace.encode(next);
        //            System.out.println("successfully parsed " + sw.toString());
        //        }
        //
        //////        TabixIndex tabixIndex2 = IndexFactory.createTabixIndex(new File(filegz), new GFFAlignmentContextCodec(), TabixFormat.GFF, ssd);
        ////        TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(filegz, new GFFAlignmentContextCodec(), tabixIndex);
        //////13:58:51.989 [main] INFO  htsjdk.samtools.liftover.GFFAlignmentContextCodec - decoding -> chr15	CGV	chain	23307198	23307645	29868.000000	+	.	id "2666817"; targetChr "chr15"; targetStart "29083061"; targetEnd "29083508"; toNegativeStrand "false"; queryStarts "23307198,23307358,23307581"; targetStarts "29083061,29083221,29083444"; blockLengths "145,108,64";
        ////        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);
        ////
        ////        while (itt.hasNext()) {
        ////            AlignmentContext next = itt.next();
        ////            StringWriter sw = new StringWriter();
        ////            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
        ////            gace.encode(next);
        ////            System.out.println("successfully parsed " + sw.toString());
        ////        }
    }

    @Test
    public void queryTabixGFF() throws IOException {
        String file = "/home/sol/Downloads/hg38ToHg19.over.chain.gff";
        System.out.println(file + TabixUtils.STANDARD_INDEX_EXTENSION);

        TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, file + TabixUtils.STANDARD_INDEX_EXTENSION, new GFFAlignmentContextCodec(), true);
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed " + sw.toString());
        }
    }
    
   

//    @Test
    public void queryTabixGFF3() throws IOException {
        String file = "/home/sol/Downloads/hg38ToHg19.over.chain.gff";

        String filegz = "/home/sol/Downloads/hg38ToHg19.over.chain.gff2.gz";

        AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File(file))));
        String line = asciiLineReader.readLine();
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        Map<String, Integer> m = new HashMap<>();
        while (line != null) {
            AlignmentContext next = codec.decode(line);
            String key = next.getChr();
            if (m.containsKey(key)) {
                m.put(key, Math.max(m.get(key), next.getEnd()));
            } else {
                m.put(key, next.getEnd());
            }
            line = asciiLineReader.readLine();
        }

        System.out.println(m);
        List<SAMSequenceRecord> records = new ArrayList<>();
        for (String key : m.keySet()) {
            records.add(new SAMSequenceRecord(key, m.get(key)));
        }

//       tfr.
        SAMSequenceDictionary ssd = new SAMSequenceDictionary(records);

        FeatureIterator featureIterator = new FeatureIterator(new File(filegz), new GFFAlignmentContextCodec());
        TabixIndexCreator tic = new TabixIndexCreator(ssd, TabixFormat.GFF);
        Index createIndex = createIndex(new File(filegz), featureIterator, tic);
        createIndex.writeBasedOnFeatureFile(new File(filegz));
//        tabixIndex2.writeBasedOnFeatureFile(new File(filegz));
    }

//    @Test
    public void queryTabixGFF2() throws IOException {
        String filegz = "/home/sol/Downloads/hg38ToHg19.over.chain.gff2.gz";
        AbstractFeatureReader<AlignmentContext, LineIterator> featureReader2 = AbstractFeatureReader.getFeatureReader(filegz, new GFFAlignmentContextCodec(), true);
        CloseableTribbleIterator<AlignmentContext> it2
                // = //                featureReader.iterator();
                //        chr2R	sol	8mer	5089312	5089319	.	+	.	sequence "CACGCACC";co
                = featureReader2.query("chr15", 23307198, 23307198);
        while (it2.hasNext()) {
            AlignmentContext next = it2.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed " + sw.toString());
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
            if (!curChr.equals(lastChr)) {
                if (visitedChromos.containsKey(curChr)) {
                    String msg = "Input file must have contiguous chromosomes.";
                    msg += " Saw feature " + featToString(visitedChromos.get(curChr));
                    msg += " followed later by " + featToString(lastFeature);
                    msg += " and then " + featToString(currentFeature);
                    throw new TribbleException.MalformedFeatureFile(msg, inputFile.getAbsolutePath());
                } else {
                    visitedChromos.put(curChr, currentFeature);
                }
            }

            creator.addFeature(currentFeature, position);

            lastFeature = currentFeature;
        }

        iterator.close();
        return creator.finalizeIndex(iterator.getPosition());
    }

    private static String featToString(final Feature feature) {
        return feature.getChr() + ":" + feature.getStart() + "-" + feature.getEnd();
    }

    private static void checkSorted(final File inputFile, final Feature lastFeature, final Feature currentFeature) {
        // if the last currentFeature is after the current currentFeature, exception out
        if (lastFeature != null && currentFeature.getStart() < lastFeature.getStart() && lastFeature.getChr().equals(currentFeature.getChr())) {
            throw new TribbleException.MalformedFeatureFile("Input file is not sorted by start position. \n"
                    + "We saw a record with a start of " + currentFeature.getChr() + ":" + currentFeature.getStart()
                    + " after a record with a start of " + lastFeature.getChr() + ":" + lastFeature.getStart(), inputFile.getAbsolutePath());
        }
    }

    static Logger logger = LogManager.getLogger();

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
        BlockCompressedInputStream bcis;
        PositionalBufferedStream pbs;
        // we also need cache our position
        private long cachedPosition;

        /**
         *
         * @param inputFile The file from which to read. Stream for reading is
         * opened on construction.
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
         * Some codecs, e.g. VCF files, need the header to decode features. This
         * is a rather poor design, the internal header is set as a side-affect
         * of reading it, but we have to live with it for now.
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

        private InputStream initStream(final File inputFile, final long skip) {
            try {
                final FileInputStream is = new FileInputStream(inputFile);

                pbs = new PositionalBufferedStream(is);
                if (skip > 0) {
                    pbs.skip(skip);
                }
                return new BlockCompressedInputStream(pbs);
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
            long pos = (hasNext()) ? cachedPosition : ((LocationAware) source).getPosition();
            logger.info("returning pos {} vs {}", pos, pbs.getPosition());
            return pos;
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
         *
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

}
