/*
 * The MIT License
 *
 * Copyright (c) 2009 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package htsjdk.samtools.liftover;

import htsjdk.samtools.tabix.AlignmentContext;
import htsjdk.samtools.tabix.AlignmentContextEncoder;
import htsjdk.samtools.tabix.GFFAlignmentContextCodec;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.liftover.Chain.ContinuousBlock;
import htsjdk.samtools.tabix.ChainContext;
import htsjdk.samtools.tabix.GFFChainContextCodec;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import htsjdk.samtools.util.SortingCollection;
import htsjdk.tribble.Feature;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.data.interval.CoordinateOrderComparator;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 * Java port of UCSC liftOver. Only the most basic liftOver functionality is
 * implemented. Internally coordinates are 0-based, half-open. The API is
 * standard Picard 1-based, inclusive.
 *
 * @author alecw@broadinstitute.org
 */
public class ChainGFFConverter1 {

    private static Logger logger = LogManager.getLogger();

    private ChainValidationStringency stringency = ChainValidationStringency.silent;
    GFFChainContextCodec codec;

    /**
     * Load UCSC chain file in order to lift over Intervals.
     */
    public ChainGFFConverter1(GFFChainContextCodec codec) {
        this.codec = codec;
    }

    public void convert(final File chainFile, OutputStream gffOutputStream) throws IOException {

        int DEAFULT_MAX_RECORDS_IN_RAM = 500000;
//        int DEAFULT_MAX_RECORDS_IN_RAM = 1000;
        int maxRecordsInRam = DEAFULT_MAX_RECORDS_IN_RAM;

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        Comparator<Feature> comparator = new CoordinateOrderComparator();

        logger.info("scanning chain file");
        int nChains = 0;
        {
            BufferedReader br = new BufferedReader(new FileReader(chainFile));
            String line = br.readLine();

            while (line != null) {
                if (line.length() > 4 && line.substring(0, 5).equals("chain")) {
                    nChains++;
                }
                line = br.readLine();
            }
            logger.info("{} chains counted", nChains);
        }

//        SortingCollection<ChainContext> sortingCollection = SortingCollection.newInstance(ChainContext.class, codec, comparator, maxRecordsInRam, tmpDir);

        BufferedReader br = new BufferedReader(new FileReader(chainFile));
        String line = br.readLine();

        logger.info("reporting progress after every {} chains", maxRecordsInRam);
        int i = 0;
        List<ChainContext> chains = new ArrayList<>();
        while (line != null) {
            if (line.length() > 4 && line.substring(0, 5).equals("chain")) {
//            System.out.println(line);
//                logger.info(line);
                ChainContext loadChain = loadChain(line);
                i++;
                if (i % maxRecordsInRam == 0) {
                    logger.info("chain id={} ({}/{})", loadChain.getId(), i, nChains);
                }
//chain 14981 chrY 57227415 + 57015736 57016297 chr1 195471971 - 122704743 122705649 1070674
                if (true) {
//                    logger.info(line);

                    line = br.readLine();

                    List<Integer> blocks = new ArrayList<>();
                    List<Integer> gaps1 = new ArrayList<>();
                    List<Integer> gaps2 = new ArrayList<>();
                    while (line != null && !line.equals("")) {
                        String[] blockFields = SPLITTER.split(line);

                        boolean sawLastLine = blockFields.length == 1;

                        int size = Integer.parseInt(blockFields[0]);
//                        logger.info("block size {}", size);
                        blocks.add(size);
                        if (!sawLastLine) {
                            gaps1.add(Integer.parseInt(blockFields[1]));
                            gaps2.add(Integer.parseInt(blockFields[2]));
//                            logger.info("gap sizes {} {}", blockFields[1], blockFields[2]);
                        }
                        line = br.readLine();
                    }
                    loadChain.setBlockLengths(blocks);
                    loadChain.setQueryGaps(gaps1);
                    loadChain.setTargetGaps(gaps2);

//                    sortingCollection.add(loadChain);
                    chains.add(loadChain);
                }
            }
            line = br.readLine();
        }

        logger.info("sorting chain file");
        Collections.sort(chains, comparator);

        i = 0;

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gffOutputStream));

        logger.info("writing to GFF");
        for (final ChainContext context : chains) {
//        for (final ChainContext context : sortingCollection) {
//             logger.info("processing chain id={} ({}/{})", context.getId(), i, nChains);
            bw.write(codec.encodeToString(context));
            bw.write("\n");
            i++;

            if (i % maxRecordsInRam == 0) {
                logger.info("processed chain id={} ({}/{})", context.getId(), i, nChains);
            }
        }
//        sortingCollection.cleanup();

        bw.flush();
        bw.close();
        br.close();
        logger.info("finished conversion to GFF");
    }

    Pattern SPLITTER = Pattern.compile("\\s");

    public ChainContext loadChain(String line) {

        String[] chainFields = SPLITTER.split(line);

        double score = 0;
        String fromSequenceName = null;
        int fromSequenceSize = 0;
        int fromChainStart = 0;
        int fromChainEnd = 0;
        String toSequenceName = null;
        int toSequenceSize = 0;
        boolean toNegativeStrand = false;
        int toChainStart = 0;
        int toChainEnd = 0;
        int id = 0;

        score = Double.parseDouble(chainFields[1]);
        fromSequenceName = chainFields[2];
        fromSequenceSize = Integer.parseInt(chainFields[3]);
        // Strand ignored because it is always +
        fromChainStart = Integer.parseInt(chainFields[5]);
        fromChainEnd = Integer.parseInt(chainFields[6]);
        toSequenceName = chainFields[7];
        toSequenceSize = Integer.parseInt(chainFields[8]);
        toNegativeStrand = chainFields[9].equals("-");
        toChainStart = Integer.parseInt(chainFields[10]);
        toChainEnd = Integer.parseInt(chainFields[11]);
        id = Integer.parseInt(chainFields[12]);

        ChainContext context = new ChainContext(fromSequenceName, fromChainStart + 1, fromChainEnd);
        context.setTargetChr(toSequenceName);
        context.setTargetStart((toNegativeStrand ? toSequenceSize - toChainEnd : toChainStart) + 1);
        context.setTargetEnd(toNegativeStrand ? toSequenceSize - toChainStart : toChainEnd);
        context.setScore(score);
        context.setId(String.format("%d", id));
        context.setToNegativeStrand(toNegativeStrand);

        return context;
    }

    /**
     * @return the stringency
     */
    public ChainValidationStringency getStringency() {
        return stringency;
    }

    /**
     * @param stringency the stringency to set
     */
    public void setStringency(ChainValidationStringency stringency) {
        this.stringency = stringency;
    }
}
