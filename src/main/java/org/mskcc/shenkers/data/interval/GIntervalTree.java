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
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jersey.repackaged.com.google.common.collect.Iterables;
import jersey.repackaged.com.google.common.collect.Iterators;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author sol
 */
public class GIntervalTree {

    Map<ChrStrand, IntervalTree> intervals = new HashMap<>();

    final IntervalTree empty = new IntervalTree();

    public GIntervalTree() {
    }

    public void add(String chr, int start, int end) {
        add(chr, start, end, Strand.NONE);
    }

    public void add(String chr, int start, int end, Strand strand) {
        intervals
                .computeIfAbsent(new ChrStrand(chr, strand), k -> new IntervalTree())
                .put(start, end, null);
    }

    public void remove(String chr, int start, int end, Strand strand) {
        Iterator<Node> queryNodes = queryNodes(chr, start, end, strand);
        queryNodes
                .forEachRemaining(n -> {
                    if (start == n.getStart() && end == n.getEnd()) {
                        queryNodes.remove();
                    }
                });
    }

    public Iterator<Node> queryNodes(String chr, int start, int end) {
        return Iterators.concat(
                queryNodes(chr, start, end, Strand.POSITIVE),
                queryNodes(chr, start, end, Strand.NEGATIVE),
                queryNodes(chr, start, end, Strand.NONE)
        );
    }

    public Iterator<Node> queryNodes(String chr, int start, int end, Strand strand) {
        System.out.println(intervals.getOrDefault(new ChrStrand(chr, strand), empty));
        return intervals
                .getOrDefault(new ChrStrand(chr, strand), empty)
                .overlappers(start, end);
    }

    public Iterator<IntervalFeature> query(String chr, int start, int end, Strand strand) {
        return Iterators.transform(
                queryNodes(chr, start, end, strand),
                n -> new IntervalFeatureImpl(chr, n.getStart(), n.getEnd(), strand, null));
    }

    public Iterator<IntervalFeature> query(String chr, int start, int end) {
        return Iterators.concat(
                query(chr, start, end, Strand.POSITIVE),
                query(chr, start, end, Strand.NEGATIVE),
                query(chr, start, end, Strand.NONE)
        );
    }

    public Stream<Node> streamOverlapNodes(String chr, int start, int end) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                queryNodes(chr, start, end), characteristics),
                        parallel);
    }

    public Stream<Node> streamOverlapNodes(String chr, int start, int end, Strand strand) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                queryNodes(chr, start, end, strand), characteristics),
                        parallel);
    }

    public Stream<IntervalFeature> streamOverlaps(String chr, int start, int end) {
        boolean parallel = false;
        int characteristics = 0;
        return StreamSupport
                .stream(
                        Spliterators
                        .spliteratorUnknownSize(
                                query(chr, start, end), characteristics),
                        parallel);
    }

    public Stream<IntervalFeature> streamOverlaps(String chr, int start, int end, Strand strand) {
        return streamOverlapNodes(chr, start, end, strand)
                .map(n -> new IntervalFeatureImpl(chr, n.getStart(), n.getEnd(), strand, null));
    }

    public Stream<IntervalFeature> stream() {
        boolean parallel = false;
        int characteristics = 0;
        return intervals.entrySet().stream().flatMap(
                e -> {
                    String chr = e.getKey().chr;
                    Strand strand = e.getKey().strand;
                    return StreamSupport
                    .stream(
                            Spliterators
                            .spliteratorUnknownSize(
                                    (Iterator<Node>) e.getValue().iterator(), characteristics),
                            parallel)
                    .map(n -> new IntervalFeatureImpl(chr, n.getStart(), n.getEnd(), strand, null));
                });
    }

    public Iterator<IntervalFeature> iterator() {
        return stream().iterator();
    }

}
