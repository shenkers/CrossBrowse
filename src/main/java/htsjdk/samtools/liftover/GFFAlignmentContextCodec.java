/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Soma
 */
public class GFFAlignmentContextCodec extends AsciiFeatureCodec<AlignmentContext> {

    Logger logger = LogManager.getLogger();
    
    public GFFAlignmentContextCodec() {
        super(AlignmentContext.class);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    @Override
    public AlignmentContext decode(String s) {
        logger.info("decoding -> " + s);
        String[] item = s.split("\t");
        String chr = item[0];
        int start = Integer.parseInt(item[3]);
        int end = Integer.parseInt(item[4]);
        AlignmentContext context = new AlignmentContext(chr, start, end);
        double score = Double.parseDouble(item[5]);
        context.setScore(score);

        String attributes = item[8];
        Pattern attributePattern = Pattern.compile("([^ ]+) \"([^\"]*)\"");
        for (String attribute : attributes.split("; *")) {
            Matcher m = attributePattern.matcher(attribute);
            if (m.find()) {
                String key = m.group(1);
                String value = m.group(2);
                if ("id".equals(key)) {
                    context.setId(value);
                } else if ("targetChr".equals(key)) {
                    context.setTargetChr(value);
                } else if ("targetStart".equals(key)) {
                    context.setTargetStart(Integer.parseInt(value));
                } else if ("targetEnd".equals(key)) {
                    context.setTargetEnd(Integer.parseInt(value));
                } else if ("toNegativeStrand".equals(key)) {
                    context.setToNegativeStrand(Boolean.parseBoolean(value));
                } else if ("queryStarts".equals(key)) {
                    context.setQueryStarts(Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                } else if ("targetStarts".equals(key)) {
                    context.setTargetStarts(Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                } else if ("blockLengths".equals(key)) {
                    context.setBlockLengths(Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                }
            }
        }
//        String.format(
//                0   "%s\t"
//             1           + "CGV\t"
//             2           + "chain\t"
//             3           + "%d\t"
//             4           + "%d\t"
//             5           + "%f\t"
//             6           + "+\t"
//             7           + ".\t"
//             8 0          + "id \"%s\"; "
//               1         + "targetChr \"%s\"; "
//               2         + "targetStart \"%d\"; "
//               3         + "targetEnd \"%d\"; "
//               4         + "toNegativeStrand \"%b\"; "
//               5         + "queryStarts \"%s\"; "
//               6         + "targetStarts \"%s\"; "
//               7         + "blockLengths \"%s\";"
//                        + "\n", 
//                context.getChr()
//        ,
//                context.getStart()
//        ,
//                context.getEnd()
//        ,
//                context.getScore()
//        ,
//                context.getId()
//        ,
//                context.getTargetChr()
//        ,
//                context.getTargetStart()
//        ,
//                context.getTargetEnd()
//        ,
//                context.getToNegativeStrand()
//        ,
//                StringUtils.join(context.getQueryStarts(), ',')
//        ,
//                StringUtils.join(context.getTargetStarts(), ',')
//        ,
//                StringUtils.join(context.getBlockLengths(), ',')
//                
//        AlignmentContext context = new AlignmentContext(chain.fromSequenceName, chain.fromChainStart, chain.fromChainEnd);
//        context.setTargetChr(chain.toSequenceName);
//        context.setTargetStart(chain.toChainStart);
//        context.setTargetEnd(chain.toChainEnd);
//        context.setScore(chain.score);
//        context.setId(chain.id);
//        context.setToNegativeStrand(chain.toNegativeStrand);
//        context.setBlockLengths(chain.getBlocks().stream().map(b -> b.blockLength).collect(Collectors.toList()));
//        context.setQueryStarts(chain.getBlocks().stream().map(b -> b.fromStart).collect(Collectors.toList()));
//        context.setTargetStarts(chain.getBlocks().stream().map(b -> b.toStart).collect(Collectors.toList()));

        return context;
    }
};
