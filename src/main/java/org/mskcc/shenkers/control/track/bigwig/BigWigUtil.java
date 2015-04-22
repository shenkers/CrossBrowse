/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bigwig;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.broad.igv.bbfile.ZoomDataRecord;
import org.broad.igv.bbfile.ZoomLevelIterator;

/**
 *
 * @author sol
 */
public class BigWigUtil {

    private static final Logger logger = LogManager.getLogger();
    private static final double DEFAULT_MIN_PIXELS_PER_FEATURE = 0.25;

    private static class BBItem {

        private int start;
        private int end;
        private double value;

        public BBItem(WigItem wi) {
            start = wi.getStartBase();
            end = wi.getEndBase();
            value = wi.getWigValue();
        }

        public BBItem(ZoomDataRecord wi) {
            start = wi.getChromStart();
            end = wi.getChromEnd();
            value = wi.getSumData();
        }

        /**
         * @return the start
         */
        public int getStart() {
            return start;
        }

        /**
         * @return the end
         */
        public int getEnd() {
            return end;
        }

        /**
         * @return the value
         */
        public double getValue() {
            return value;
        }

    }

    public static RangeMap<Integer, Double> values(BBFileReader bbfr, String chr, int start, int end, int zoom) {

        boolean contained = false;
        Iterator<BBItem> iterator = null;
        if (zoom == 0) {
            BigWigIterator bwi = bbfr.getBigWigIterator(chr, start - 1, chr, end, false);
            iterator = StreamSupport.stream(Spliterators.spliteratorUnknownSize(bwi, 0), false).map(w -> new BBItem(w)).iterator();
        } else {
            ZoomLevelIterator zli = bbfr.getZoomLevelIterator(zoom, chr, start - 1, chr, end, contained);
            iterator = StreamSupport.stream(Spliterators.spliteratorUnknownSize(zli, 0), false).map(w -> new BBItem(w)).iterator();
        }

        RangeMap<Integer, Double> cov = TreeRangeMap.create();
        while (iterator.hasNext()) {
            BBItem wi = iterator.next();
            cov.put(Range.closed(wi.getStart() + 1, wi.getEnd()), wi.getValue());
        }

        return cov;
    }

    public static int selectZoomLevel(int nPixels, int nNucleotides, List<Double> featuresPerNucleotide) {
        return selectZoomLevel(nPixels, nNucleotides, DEFAULT_MIN_PIXELS_PER_FEATURE, featuresPerNucleotide);
    }

    public static int selectZoomLevel(int nPixels, int nNucleotides, double minPixelsPerFeature, List<Double> featuresPerNucleotide) {

        double ntPerPixel = nNucleotides * 1. / nPixels;
        List<Double> featuresPerPixel = featuresPerNucleotide.stream().map(d -> d * ntPerPixel).collect(Collectors.toList());

        int indexNotLessThanMin = 0;

        for (int i = 0; i < featuresPerPixel.size(); i++) {
            if (featuresPerPixel.get(i) >= minPixelsPerFeature) {
                indexNotLessThanMin = i;
            }
        }

        return indexNotLessThanMin;
    }

    public static List<Double> zoomFeatureDensities(BBFileReader bbfr) {
        Long totalNt = Stream.iterate(0, i -> i + 1)
                .limit(bbfr.getChromosomeNameCount())
                .map(i -> bbfr.getChromosomeBounds(i, i))
                .map(t -> (long) t.getEndBase())
                .reduce(0l, (a, b) -> a + b);

        List<Double> featuresPerNucleotide = new ArrayList<>();
        
        featuresPerNucleotide.add(1.);
        for (int i = 1; i <= bbfr.getZoomLevelCount(); i++) {
            featuresPerNucleotide.add(bbfr.getZoomLevelRecordCount(i) * 1. / totalNt);

        }
        
        return featuresPerNucleotide;
    }

}
