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

import org.mskcc.shenkers.control.alignment.LocalAlignment;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 * Java port of UCSC liftOver. Only the most basic liftOver functionality is
 * implemented. Internally coordinates are 0-based, half-open. The API is
 * standard Picard 1-based, inclusive.
 *
 * @author alecw@broadinstitute.org
 */
public class ChainConverter {

    private ChainValidationStringency stringency = ChainValidationStringency.silent;
    AlignmentContextEncoder encoder;

    /**
     * Load UCSC chain file in order to lift over Intervals.
     */
    public ChainConverter(AlignmentContextEncoder ace) throws IOException {
        encoder = ace;
    }

    public void convert(final File chainFile) throws IOException {
        IOUtil.assertFileIsReadable(chainFile);
        final Set<Integer> ids = new HashSet<Integer>();
        BufferedLineReader reader = new BufferedLineReader(IOUtil.openFileForReading(chainFile));
        Chain chain;
        List<AlignmentContext> contexts = new ArrayList<>();
        while ((chain = Chain.loadChain(reader, chainFile.toString())) != null) {
            if (stringency == ChainValidationStringency.strict && ids.contains(chain.id)) {
                throw new SAMException("Chain id " + chain.id + " appears more than once in chain file.");
            }
            ids.add(chain.id);

            // create context and write to encoder
            AlignmentContext context = new AlignmentContext(chain.fromSequenceName, chain.fromChainStart, chain.fromChainEnd);
            context.setTargetChr(chain.toSequenceName);
            context.setTargetStart(chain.toChainStart);
            context.setTargetEnd(chain.toChainEnd);
            context.setScore(chain.score);
            context.setId(String.format("%d", chain.id));
            context.setToNegativeStrand(chain.toNegativeStrand);
            context.setBlockLengths(chain.getBlocks().stream().map(b -> b.blockLength).collect(Collectors.toList()));
            context.setQueryStarts(chain.getBlocks().stream().map(b -> b.fromStart).collect(Collectors.toList()));
            context.setTargetStarts(chain.getBlocks().stream().map(b -> b.toStart).collect(Collectors.toList()));

            contexts.add(context);
            
        }
        
        Collections.sort(contexts, new Comparator<AlignmentContext>() {

            @Override
            public int compare(AlignmentContext o1, AlignmentContext o2) {
                int compareChr = o1.getChr().compareTo(o2.getChr());
                if(compareChr==0){
                    return o1.getStart() - o2.getStart();
                }
                else{
                    return compareChr;
                }
            }
        });
        
        for(AlignmentContext context : contexts)
            encoder.encode(context);
        encoder.close();
        reader.close();
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
