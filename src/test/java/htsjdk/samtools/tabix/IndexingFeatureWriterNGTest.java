/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.tabix;

import htsjdk.samtools.tabix.AlignmentContext;
import htsjdk.samtools.tabix.GFFAlignmentContextEncoder;
import htsjdk.samtools.tabix.GFFAlignmentContextCodec;
import htsjdk.samtools.tabix.IndexingFeatureWriter;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.TribbleIndexedFeatureReader;
import htsjdk.tribble.index.DynamicIndexCreator;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.util.TabixUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
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
public class IndexingFeatureWriterNGTest {

    public IndexingFeatureWriterNGTest() {
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

    @Test
    public void blockCompressFile() throws FileNotFoundException {
        String file = "/home/sol/Downloads/test.chain.gff";
        String out = "/home/sol/Downloads/t.bgz";
        BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(new File(out));
        PrintStream ps = new PrintStream(bcos);
        Scanner scan = new Scanner(new File(file));
        while(scan.hasNext()){
            ps.println(scan.nextLine());
        }
        ps.close();
    }

    @Test
    public void indexCompressedFile() throws IOException {
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        String file = "/home/sol/Downloads/t.bgz";
        TabixIndexCreator tic = new TabixIndexCreator(TabixFormat.GFF);
        BlockCompressedInputStream bcis = new BlockCompressedInputStream(new File(file));
        long p = 0;
        String line = bcis.readLine();

        while (line != null) {
            AlignmentContext decode = codec.decode(line);

            tic.addFeature(decode, p);
//            System.out.println(String.format("%d %d %d %s", p, BlockCompressedFilePointerUtil.getBlockAddress(p), BlockCompressedFilePointerUtil.getBlockOffset(p),line));
            p = bcis.getFilePointer();
            line = bcis.readLine();
        }
        Index index = tic.finalizeIndex(bcis.getFilePointer());
        index.writeBasedOnFeatureFile(new File(file));

        TabixFeatureReader<AlignmentContext, LineIterator> tifr = new TabixFeatureReader(file, new GFFAlignmentContextCodec());
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed from already block-compressed file" + sw.toString());
        }
    }

    /**
     * Test of close method, of class IndexingFeatureWriter.
     */
    @Test
    public void test() throws Exception {
        System.out.println("test");

        String file = "/home/sol/Downloads/test.chain.gff.gz";

        TabixIndexCreator tabixIndexCreator = new TabixIndexCreator(TabixFormat.GFF);
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        Function<AlignmentContext, String> encoder = codec::encodeToString;
        IndexingFeatureWriter instance = new IndexingFeatureWriter(new File(file), IndexingFeatureWriter.Compression.BGZF, encoder, tabixIndexCreator, null);

        AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File("/home/sol/Downloads/test.gff"))));
        String line = asciiLineReader.readLine();

        Map<String, Integer> m = new HashMap<>();
        while (line != null) {
//            System.out.println(line);
            AlignmentContext next = codec.decode(line);
            instance.add(next);
            String key = next.getChr();
            if (m.containsKey(key)) {
                m.put(key, Math.max(m.get(key), next.getEnd()));
            } else {
                m.put(key, next.getEnd());
            }
            line = asciiLineReader.readLine();
        }
        instance.close();

//             TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, file + TabixUtils.STANDARD_INDEX_EXTENSION, new GFFAlignmentContextCodec(), true);
        TabixFeatureReader<AlignmentContext, LineIterator> tifr = new TabixFeatureReader(file, new GFFAlignmentContextCodec());
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed " + sw.toString());
        }

    }

    @Test
    public void test2() throws Exception {
        System.out.println("test");

        String file = "/home/sol/Downloads/test.chain.gff";

        TabixIndexCreator tabixIndexCreator = new TabixIndexCreator(TabixFormat.GFF);
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        Function<AlignmentContext, String> encoder = codec::encodeToString;
        IndexingFeatureWriter instance = new IndexingFeatureWriter(new File(file), IndexingFeatureWriter.Compression.NONE, encoder, tabixIndexCreator, null);

        AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File("/home/sol/Downloads/test.gff"))));
        String line = asciiLineReader.readLine();

        Map<String, Integer> m = new HashMap<>();
        while (line != null) {
//            System.out.println(line);
            AlignmentContext next = codec.decode(line);
            instance.add(next);
            String key = next.getChr();
            if (m.containsKey(key)) {
                m.put(key, Math.max(m.get(key), next.getEnd()));
            } else {
                m.put(key, next.getEnd());
            }
            line = asciiLineReader.readLine();
        }
        instance.close();

//             TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, file + TabixUtils.STANDARD_INDEX_EXTENSION, new GFFAlignmentContextCodec(), true);
        AbstractFeatureReader<AlignmentContext, LineIterator> tifr = AbstractFeatureReader.getFeatureReader(file, file + TabixUtils.STANDARD_INDEX_EXTENSION, codec, false);
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed from uncompressed " + sw.toString());
        }

    }

    @Test
    public void test3() throws Exception {
        System.out.println("test");

        String file = "/home/sol/Downloads/test.chain.gff";

        IndexCreator tabixIndexCreator = new DynamicIndexCreator(new File(file + ".idx"), IndexFactory.IndexBalanceApproach.FOR_SIZE);
        GFFAlignmentContextCodec codec = new GFFAlignmentContextCodec();
        Function<AlignmentContext, String> encoder = codec::encodeToString;

        SAMSequenceDictionary ssd = null;
        {
            AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File("/home/sol/Downloads/test.gff"))));
            String line = asciiLineReader.readLine();

            Map<String, Integer> m = new HashMap<>();
            while (line != null) {
//            System.out.println(line);
                AlignmentContext next = codec.decode(line);
                String key = next.getChr();
                if (m.containsKey(key)) {
                    m.put(key, Math.max(m.get(key), next.getEnd()));
                } else {
                    m.put(key, next.getEnd());
                }
                line = asciiLineReader.readLine();
            }

            List<SAMSequenceRecord> records = new ArrayList<>();
            for (String key : m.keySet()) {
                records.add(new SAMSequenceRecord(key, m.get(key)));
            }
            ssd = new SAMSequenceDictionary(records);
        }

        IndexingFeatureWriter instance = new IndexingFeatureWriter(new File(file), IndexingFeatureWriter.Compression.NONE, encoder, tabixIndexCreator, ssd);

        AsciiLineReader asciiLineReader = new AsciiLineReader(new BufferedInputStream(new FileInputStream(new File("/home/sol/Downloads/test.gff"))));
        String line = asciiLineReader.readLine();

        while (line != null) {
            AlignmentContext next = codec.decode(line);
            instance.add(next);
            line = asciiLineReader.readLine();
        }
        instance.close();

//             TribbleIndexedFeatureReader<AlignmentContext, LineIterator> tifr = new TribbleIndexedFeatureReader(file, file + TabixUtils.STANDARD_INDEX_EXTENSION, new GFFAlignmentContextCodec(), true);
        AbstractFeatureReader<AlignmentContext, LineIterator> tifr = AbstractFeatureReader.getFeatureReader(file, file + ".idx", codec, false);
        CloseableTribbleIterator<AlignmentContext> itt = tifr.query("chr15", 23307198, 23307198);

        while (itt.hasNext()) {
            AlignmentContext next = itt.next();
            StringWriter sw = new StringWriter();
            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            gace.encode(next);
            System.out.println("successfully parsed from uncompressed " + sw.toString());
        }

    }

}
