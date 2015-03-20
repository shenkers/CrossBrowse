/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import htsjdk.samtools.tabix.*;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.gene.GTFContext.Feature;

/**
 *
 * @author sol
 */
public class GTFCodec extends AsciiFeatureCodec<GTFContext> {

    Logger logger = LogManager.getLogger();

    LineReader fromBufferedStream;
    final private static Pattern p = Pattern.compile("([^ ]+) \"([^\"]*)\"");

    public GTFCodec() {
        super(GTFContext.class);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    @Override
    public GTFContext decode(String s) {
        try {
            String[] item = s.split("\t");
            String chr = item[0];
            String source = item[1];
            String feature = item[2];
            int start = Integer.parseInt(item[3]);
            int end = Integer.parseInt(item[4]);
            String score = item[5];
            char strand = item[6].charAt(0);
            String frame = item[7];
            GTFContext context = new GTFContext(chr, start, end);
            String attributes = item[8];
            context.setSource(source);
            context.setFeature(feature);
            context.setStrand(strand);
            context.setAttributes(attributes);
            context.setScore(score);
            context.setFrame(frame);
            
            Stream.of(attributes.split(";")).map(str -> {
                        Matcher matcher = p.matcher(str);
                        matcher.find();
                        return new String[]{matcher.group(1), matcher.group(2)};
                    }
                    ).forEach(keyValue -> {
                        if(keyValue[0].equals("transcript_id")){
                            context.setTranscriptId(keyValue[1]);
                        }
                        if(keyValue[0].equals("gene_id")){
                            context.setGeneId(keyValue[1]);
                        }
                        if(keyValue[0].equals("gene_name")){
                            context.setName(keyValue[1]);
                        }
                    });

            return context;
        } catch (Exception e) {
            logger.info("error decoding line '{}'", s);
            logger.info("error", e);
            try {
                logger.info("nextline {}", fromBufferedStream.readLine());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(GTFCodec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public String encodeToString(GTFContext context) {

        return String.format(
                "%s\t" // chr
                + "%s\t" // source
                + "%s\t" // feature
                + "%d\t" // start
                + "%d\t" // end
                + "%s\t" // score
                + "%c\t" // strand
                + "%s\t" // frame
                + "%s" // attributes
                + "\n",
                context.getChr(),
                context.getSource(),
                context.getFeature(),
                context.getStart(),
                context.getEnd(),
                context.getScore(),
                context.getStrand(),
                context.getFrame(),
                context.getAttributes()
        );
    }

};
