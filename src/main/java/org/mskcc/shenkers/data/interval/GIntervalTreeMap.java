/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.data.interval;

import com.google.common.collect.Maps;
import htsjdk.samtools.util.IntervalTree;
import htsjdk.samtools.util.IntervalTree.Node;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.bed.SimpleBEDFeature;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jersey.repackaged.com.google.common.collect.Iterators;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author sol
 */
public class GIntervalTreeMap<T> {

    Map<ChrStrand, IntervalTree<T>> intervals = new HashMap<ChrStrand, IntervalTree<T>>();

    final IntervalTree<T> empty = new IntervalTree<T>();

    public GIntervalTreeMap() {
    }

    public void add(String chr, int start, int end) {
        add(chr, start, end, Strand.NONE, null);
    }

    public void add(String chr, int start, int end, Strand strand) {
        add(chr, start, end, strand, null);
    }

    public void add(String chr, int start, int end, Strand strand, T value) {
        intervals
                .computeIfAbsent(new ChrStrand(chr, strand), k -> new IntervalTree<T>())
                .put(start, end, value);
    }

    public Iterator<Node<T>> queryNodes(String chr, int start, int end) {
        return Iterators.concat(
                queryNodes(chr, start, end, Strand.POSITIVE),
                queryNodes(chr, start, end, Strand.NEGATIVE),
                queryNodes(chr, start, end, Strand.NONE)
        );
    }

    public Iterator<Node<T>> queryNodes(String chr, int start, int end, Strand strand) {
        return intervals
                .getOrDefault(new ChrStrand(chr, strand), empty)
                .overlappers(start, end);
    }

    public Iterator<IntervalFeature<T>> query(String chr, int start, int end, Strand strand) {
        return Iterators.transform(
                queryNodes(chr, start, end, strand),
                n -> new IntervalFeatureImpl<T>(chr, n.getStart(), n.getEnd(), strand, n.getValue()));
    }

    public Iterator<IntervalFeature<T>> query(String chr, int start, int end) {
        return Iterators.concat(
                query(chr, start, end, Strand.POSITIVE),
                query(chr, start, end, Strand.NEGATIVE),
                query(chr, start, end, Strand.NONE)
        );
    }

    public Stream<Node<T>> streamOverlapNodes(String chr, int start, int end) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                queryNodes(chr, start, end), characteristics),
                        parallel);
    }

    public Stream<Node<T>> streamOverlapNodes(String chr, int start, int end, Strand strand) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                queryNodes(chr, start, end, strand), characteristics),
                        parallel);
    }

    public Stream<IntervalFeature<T>> streamOverlaps(String chr, int start, int end) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                query(chr, start, end), characteristics),
                        parallel);
    }

    public Stream<IntervalFeature<T>> streamOverlaps(String chr, int start, int end, Strand strand) {
        return streamOverlapNodes(chr, start, end, strand)
                .map(n -> new IntervalFeatureImpl<T>(chr, n.getStart(), n.getEnd(), strand, n.getValue()));
    }
}
