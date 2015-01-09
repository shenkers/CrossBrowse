/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bam;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.view.LineHistogramView;

/**
 *
 * @author sol
 */
public class BamView1 implements View<BamContext> {

    private final Logger logger = LogManager.getLogger();
    
    @Override
    public Pane getContent(BamContext context) {

        return context.spanProperty().getValue().map(i -> {
            
            SamReader reader = context.readerProperty().getValue();
            int[] coverage = coverage(reader, i.getChr(), i.getStart(), i.getEnd());
            LineHistogramView lhv = new LineHistogramView();
            lhv.setMin(0);
            
            OptionalInt max = IntStream.of(coverage).max();
            max.ifPresent(m->lhv.setMax(m));
            
            double[] data = ArrayUtils.toPrimitive(IntStream.of(coverage).mapToDouble(j->j+0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
            lhv.setData(data);
            
            logger.info("coverage {}", IntStream.of(coverage).mapToDouble(j->j+0.).boxed().collect(Collectors.toList()).toString());
            
            Pane p = lhv.getGraphic();
            
            return p;
        }
        ).orElse(new Pane(new Label("bamview1: span not set")));

    }

    /**
     *
     *
     * @param SAMReader
     * @param chromosome
     * @param start
     * @param end
     * @return
     */
    public static int[] coverage(SamReader SAMReader, String chromosome, int start, int end) {
        SAMRecordIterator sri = SAMReader.query(chromosome, start, end, false);

        int length = 1 + end - start;
        int[] cov = new int[length];

        while (sri.hasNext()) {
            SAMRecord sr = sri.next();

            int alignmentPosition = sr.getAlignmentStart();
            Cigar cigar = sr.getCigar();

            for (int i = 0; i < cigar.numCigarElements(); i++) {
                CigarElement cigarElement = cigar.getCigarElement(i);
                if (cigarElement.getOperator().consumesReferenceBases()) {
                    boolean consumesReadBases = cigarElement.getOperator().consumesReadBases();
                    for (int j = 0; j < cigarElement.getLength(); j++) {
                        if (consumesReadBases && alignmentPosition >= start && alignmentPosition <= end) {
                            cov[alignmentPosition - start]++;
                        }
                        alignmentPosition++;
                    }
                }
            }
        }

        sri.close();

        return cov;
    }

}
