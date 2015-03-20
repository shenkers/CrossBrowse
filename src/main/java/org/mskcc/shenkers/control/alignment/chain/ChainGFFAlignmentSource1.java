/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.liftover.ChainGFFConverter1;
import htsjdk.samtools.liftover.ChainParser;
import htsjdk.samtools.tabix.AlignmentContext;
import htsjdk.samtools.tabix.ChainContext;
import htsjdk.samtools.tabix.GFFAlignmentContextCodec;
import htsjdk.samtools.tabix.GFFAlignmentContextEncoder;
import htsjdk.samtools.tabix.GFFChainContextCodec;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.Interval;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.alignment.LocalAlignment;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ChainGFFAlignmentSource1 implements AlignmentSource {

    final static GFFChainContextCodec codec = new GFFChainContextCodec();

    private static final Logger logger = LogManager.getLogger();

    TabixFeatureReader<ChainContext, LineIterator> alignments;

    public ChainGFFAlignmentSource1(String uri, boolean force_convert) throws IOException {

        File chain_gff_bgz = new File(uri.concat(".gff.bgz"));
        File chain_gff_bgz_tbi = new File(uri.concat(".gff.bgz.tbi"));

        logger.info("converting {} to {}", uri, chain_gff_bgz.getAbsoluteFile());
        logger.info("{} already exists? {}", chain_gff_bgz.getAbsoluteFile(), chain_gff_bgz.exists());
        if (force_convert || !chain_gff_bgz.exists()) {
            logger.info("converting chain to gff");
            convertChainToGffBgz(new File(uri), chain_gff_bgz);
            logger.info("creating tabix index");
            createTabixIndex(chain_gff_bgz);
        }
        logger.info("{} already exists? {}", chain_gff_bgz_tbi.getAbsoluteFile(), chain_gff_bgz_tbi.exists());
        if (!chain_gff_bgz_tbi.exists()) {
            logger.info("creating tabix index");
            createTabixIndex(chain_gff_bgz);
        }
        alignments = new TabixFeatureReader(chain_gff_bgz.getAbsolutePath(), codec);
    }

    private void convertChainToGffBgz(File f, File chain_gff_bgz) throws IOException {
        OutputStream gffOutputStream = new BlockCompressedOutputStream(chain_gff_bgz);
        ChainGFFConverter1 converter = new ChainGFFConverter1(codec);
        converter.convert(f, gffOutputStream);
    }

    private void createTabixIndex(File chain_gff_bgz) throws IOException {
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
        Index index = indexCreator.finalizeIndex(inputStream.getFilePointer());
        index.writeBasedOnFeatureFile(chain_gff_bgz);
    }

    @Override
    public List<LocalAlignment> getAlignment(GenomeSpan span1, GenomeSpan span2) {
        try {

            logger.info("querying alignment file");
            CloseableTribbleIterator<ChainContext> itt = alignments.query(span1.getChr(), span1.getStart(), span1.getEnd());

            List<LocalAlignment> chainIntersections = new ArrayList<>();

            // find all overlapping chains
            logger.info("iterating alignments");
            while (itt.hasNext()) {
                ChainContext next = itt.next();
                logger.info("alignment from {}:{}-{} to {}:{}-{}", next.getChr(), next.getStart(), next.getEnd(), next.getTargetChr(), next.getTargetStart(), next.getTargetEnd());
                // check for intersections with the displayed interval
                // retaining only those blocks that intersect
                Optional<LocalAlignment> alignedBlocks = getAlignedBlocks(next, span1);
                logger.info("aligned blocks? {}", alignedBlocks.isPresent());
                alignedBlocks.ifPresent(l -> chainIntersections.add(l));
            }

            return chainIntersections;
        } catch (IOException ex) {
            logger.error("error parsing alignment file", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @return a local alignment restricted to the given query interval
     */
    public static Optional<LocalAlignment> getAlignedBlocks(final ChainContext chain, GenomeSpan interval) {

        List<Integer> queryGaps = chain.getQueryGaps();
        List<Integer> targetGaps = chain.getTargetGaps();
        List<Integer> blockLengths = chain.getBlockLengths();

        List<Pair<Integer, Integer>> fromBlocks = new ArrayList<>();
        List<Pair<Integer, Integer>> toBlocks = new ArrayList<>();

        boolean hasIntersection = false;

        int qPos = chain.getStart();
        int tPos = chain.getToNegativeStrand() ? chain.getTargetEnd() : chain.getTargetStart();

        int start = interval.getStart();
        int end = interval.getEnd();

        int nBlocks = blockLengths.size();
        for (int i = 0; i < nBlocks; ++i) {

            int length = blockLengths.get(i);

            // get the boundaries of the current block in the query
            int qStart = qPos;
            int qEnd = qStart + length - 1;

            // get the boundaries of the current block in the target
            int tStart = chain.getToNegativeStrand() ? tPos - length + 1 : tPos;
            int tEnd = chain.getToNegativeStrand() ? tPos : tPos + length - 1;
            
            if (qStart > end) {
                logger.info("breaking");
                break;
            }
            if (qEnd >= start) {
                logger.info("overlaps");
                hasIntersection = true;

                // determine if the query interval is offset from the block
                int fromStart = start > qStart ? start : qStart;
                int fromEnd = qEnd > end ? end : qEnd;

                int startOffset = (start > qStart)
                        ? start - qStart
                        : 0;

                int offsetFromEnd = (qEnd > end)
                        ? qEnd - end
                        : 0;

                // apply the offset to the interval in the target genome
                int toStart = tStart
                        + (chain.getToNegativeStrand()
                                ? offsetFromEnd
                                : startOffset);
                int toEnd = tEnd
                        - (chain.getToNegativeStrand()
                                ? startOffset
                                : offsetFromEnd);

                fromBlocks.add(new Pair<>(fromStart, fromEnd));
                toBlocks.add(new Pair<>(toStart, toEnd));
            }

            if (i < nBlocks - 1) {
                int qGap = queryGaps.get(i);
                int tGap = targetGaps.get(i);

                qPos += length + qGap;
                tPos += chain.getToNegativeStrand() ? -(length + tGap) : length + tGap;
            }

        }

        Optional<LocalAlignment> alignment
                = hasIntersection
                        ? Optional.of(new LocalAlignment(chain.getChr(), chain.getTargetChr(), chain.getToNegativeStrand(), fromBlocks, toBlocks))
                        : Optional.empty();

        return alignment;
    }

}
