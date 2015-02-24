/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.tabix;

import htsjdk.samtools.util.SortingCollection;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineReader;
import htsjdk.tribble.readers.LineReaderUtil;
import htsjdk.tribble.readers.PositionalBufferedStream;
import htsjdk.tribble.util.ParsingUtils;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;
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
public class GFFAlignmentContextCodec extends AsciiFeatureCodec<AlignmentContext> implements SortingCollection.Codec<AlignmentContext> {

    Logger logger = LogManager.getLogger();
    
    LineReader fromBufferedStream;
    Writer w;
    
    public GFFAlignmentContextCodec() {
        super(AlignmentContext.class);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    @Override
    public AlignmentContext decode(String s) {
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
        return context;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        w = new BufferedWriter(new OutputStreamWriter(os));
    }

    @Override
    public void setInputStream(InputStream is) {
        fromBufferedStream = LineReaderUtil.fromBufferedStream(new BufferedInputStream(is));
    }
    
    
    public String encodeToString(AlignmentContext context) {
        return String.format(
                    "%s\t"
                            + "CGV\t"
                            + "chain\t"
                            + "%d\t"
                            + "%d\t"
                            + "%f\t"
                            + "+\t"
                            + ".\t"
                            + "id \"%s\"; "
                            + "targetChr \"%s\"; "
                            + "targetStart \"%d\"; "
                            + "targetEnd \"%d\"; "
                            + "toNegativeStrand \"%b\"; "
                            + "queryStarts \"%s\"; "
                            + "targetStarts \"%s\"; "
                            + "blockLengths \"%s\";",
                    context.getChr(),
                    context.getStart(),
                    context.getEnd(),
                    context.getScore(),
                    context.getId(),
                    context.getTargetChr(),
                    context.getTargetStart(),
                    context.getTargetEnd(),
                    context.getToNegativeStrand(),
                    StringUtils.join(context.getQueryStarts(),','),
                    StringUtils.join(context.getTargetStarts(),','),
                    StringUtils.join(context.getBlockLengths(),',')
            );
    }

    @Override
    public void encode(AlignmentContext context) {
        BufferedWriter bw = null;
        try {
            w.write(encodeToString(context));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public AlignmentContext decode() {
        String line=null;
        try {
            line = fromBufferedStream.readLine();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GFFAlignmentContextCodec.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(line==null)
            return null;
        return decode(line);
    }

    @Override
    public SortingCollection.Codec<AlignmentContext> clone() {
        return new GFFAlignmentContextCodec();
    }
};
