/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.samtools.tabix.AlignmentContext;
import htsjdk.samtools.tabix.ChainContext;
import htsjdk.samtools.tabix.GFFAlignmentContextCodec;
import htsjdk.samtools.tabix.GFFAlignmentContextEncoder;
import htsjdk.samtools.tabix.GFFChainContextCodec;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.util.LittleEndianOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
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
public class ChainGFFConverter1NGTest {

    public ChainGFFConverter1NGTest() {
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
     * Test of convert method, of class ChainGFFConverter1.
     */
//    @Test
    public void testConvert() throws Exception {
        System.out.println("convert");

        File chainFile = new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain");
        File gffFileBgz = new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff.bgz");
//        File gffFile = new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff");

        ChainGFFConverter1 instance = new ChainGFFConverter1(new GFFChainContextCodec());
//        instance.convert(chainFile, new FileOutputStream(gffFile));
        BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(gffFileBgz);
        instance.convert(chainFile, bcos);
//        bcos.close();

    }

//    @Test (dependsOnMethods = {"testConvert"})
//    @Test
    public void doIdx() throws IOException {
        System.out.println("indexing file");
//        File gffFile = new File("hg38.mm10.all.chain.gff.bgz");
//        File tbiFile = new File("hg38.mm10.all.chain.gff.bgz.tbi");
        File gffFile = new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff.bgz");
        File tbiFile = new File("/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff.bgz.tbi");
        createTabixIndex(gffFile, tbiFile);
    }

//    @Test (dependsOnMethods = {"doIdx"})
    @Test
    public void queryGFF() throws IOException {
        System.out.println("querying");
        String file = "/mnt/LaiLab/jaaved/play/hg38.mm10.all.chain.gff.bgz";
        
//        TabixFeatureReader<AlignmentContext, LineIterator> tifr = new TabixFeatureReader(file, new GFFChainContextCodec());
        AbstractFeatureReader<ChainContext, LineIterator> tifr = AbstractFeatureReader.getFeatureReader(file, "temp.gz.tbi", new GFFChainContextCodec(), true);
        CloseableTribbleIterator<ChainContext> itt = tifr.query("chrX", 145663601, 148117912);

        while (itt.hasNext()) {
            ChainContext next = itt.next();
            StringWriter sw = new StringWriter();
//            GFFAlignmentContextEncoder gace = new GFFAlignmentContextEncoder(sw);
            sw.write(new GFFChainContextCodec().encodeToString(next));
            System.out.println("successfully parsed from GFF file" + sw.toString());
        }
    }

    private void createTabixIndex(File chain_gff_bgz, File chain_gff_bgz_tbi) throws IOException {
        GFFChainContextCodec codec = new GFFChainContextCodec();
        TabixIndexCreator indexCreator = new TabixIndexCreator(TabixFormat.GFF);
        BlockCompressedInputStream inputStream = new BlockCompressedInputStream(chain_gff_bgz);

        long p = 0;
        String line = inputStream.readLine();

        while (line != null) {
            //add the feature to the index
            ChainContext decode = codec.decode(line);
            indexCreator.addFeature(decode, p);
            // read the next line if available
            p = inputStream.getFilePointer();
            line = inputStream.readLine();
        }
        // write the index to a file
        Index index = indexCreator.finalizeIndex(p);
        // VERY important! either use write based on input file or pass the little endian a BGZF stream
        index.write(new LittleEndianOutputStream(new BlockCompressedOutputStream(chain_gff_bgz_tbi)));
    }

}
