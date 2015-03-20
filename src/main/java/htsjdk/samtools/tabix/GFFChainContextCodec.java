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
import java.util.ArrayList;
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
public class GFFChainContextCodec extends AsciiFeatureCodec<ChainContext> implements SortingCollection.Codec<ChainContext> {

    Logger logger = LogManager.getLogger();
    
    LineReader fromBufferedStream;
    Writer w;
    
    public GFFChainContextCodec() {
        super(ChainContext.class);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    @Override
    public ChainContext decode(String s) {
        try{
        String[] item = s.split("\t");
        String chr = item[0];
        int start = Integer.parseInt(item[3]);
        int end = Integer.parseInt(item[4]);
        ChainContext context = new ChainContext(chr, start, end);
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
                } else if ("queryGaps".equals(key)) {
                    context.setQueryGaps(value.length()==0?new ArrayList<>():Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                } else if ("targetGaps".equals(key)) {
                    context.setTargetGaps(value.length()==0?new ArrayList<>():Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                } else if ("blockLengths".equals(key)) {
                    context.setBlockLengths(Stream.of(value.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                }
            }
        }
        return context;
        }catch(Exception e){
            logger.info("error decoding line '{}'",s);
            logger.info("error",e);
            try {
                logger.info("nextline {}",fromBufferedStream.readLine());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(GFFChainContextCodec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        try {
            if(w!=null)
            w.flush();
        } catch (IOException ex) {
            logger.error(ex);
        }
        w = new OutputStreamWriter(os);
    }

    @Override
    public void setInputStream(InputStream is) {
        fromBufferedStream = LineReaderUtil.fromBufferedStream(new BufferedInputStream(is));
    }
    
    
    public String encodeToString(ChainContext context) {
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
                            + "queryGaps \"%s\"; "
                            + "targetGaps \"%s\"; "
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
                    StringUtils.join(context.getQueryGaps(),','),
                    StringUtils.join(context.getTargetGaps(),','),
                    StringUtils.join(context.getBlockLengths(),',')
            );
    }

    @Override
    public void encode(ChainContext context) {
        try {
            w.write(encodeToString(context));
            w.write("\n");
        } catch (IOException ex) {
            logger.error(ex);
        }
        
    }

    @Override
    public ChainContext decode() {
        String line=null;
        try {
            line = fromBufferedStream.readLine();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GFFChainContextCodec.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(line==null)
            return null;
        return decode(line);
    }

    @Override
    public SortingCollection.Codec<ChainContext> clone() {
        return new GFFChainContextCodec();
    }
};
