/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.gene;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 *
 * @author sol
 */
public class RangeSetCollector implements Collector<Range<Integer>, RangeSet<Integer>, RangeSet<Integer>> {

    @Override
    public Supplier<RangeSet<Integer>> supplier() {
        return () -> TreeRangeSet.create();
    }

    @Override
    public BiConsumer<RangeSet<Integer>, Range<Integer>> accumulator() {
        return (set, range) -> set.add(range);
    }

    @Override
    public BinaryOperator<RangeSet<Integer>> combiner() {
        return (set1, set2) -> {
            set1.addAll(set2);
            return set1;
        };
    }

    @Override
    public Function<RangeSet<Integer>, RangeSet<Integer>> finisher() {
        return set -> set;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED);
    }

}
