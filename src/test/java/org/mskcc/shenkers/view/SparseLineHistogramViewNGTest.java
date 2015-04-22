/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import java.util.Collections;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import org.mskcc.shenkers.data.interval.RangeTools;
import org.mskcc.shenkers.util.IntervalTools;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class SparseLineHistogramViewNGTest {

    public SparseLineHistogramViewNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    public RangeMap<Integer, Double> add(RangeMap<Integer, Double> rm, Range<Integer> r, Double d) {
        RangeMap<Integer, Double> ram = TreeRangeMap.create();
        ram.putAll(rm);
        ram.put(r, d);

        rm.subRangeMap(r).asMapOfRanges().forEach((range, val) -> ram.put(range, val + d));
        return ram;
    }

    public Range<Double> asReal(Range<Integer> r) {
        System.err.println("range "+r);
        Range<Integer> asClosed = RangeTools.asClosed(r);
        System.out.println("closed "+asClosed);
        return Range.closedOpen(asClosed.lowerEndpoint().doubleValue() - 1, asClosed.upperEndpoint().doubleValue());
    }

    public RangeMap<Double, Double> toReal(RangeMap<Integer, Double> rm) {
        RangeMap<Double, Double> ram = TreeRangeMap.create();

        rm.asMapOfRanges().forEach((range, val) -> ram.put(asReal(range), val));
        return ram;
    }
    
    public double length(Range<Double> r){
        return r.upperEndpoint()-r.lowerEndpoint();
    }

    /**
     * Test of getGraphic method, of class SparseLineHistogramView.
     */
    @Test
    public void testGetGraphic() {
        RangeMap<Integer, Double> ram = TreeRangeMap.create();
//        ram.put(Range.closed(0, 4), 0.);
        System.out.println("adding");
        ram = add(ram, Range.closed(1, 3), 1.);
        ram.asMapOfRanges().forEach((r, d) -> System.out.println(String.format("%s %f", r, d)));
        System.out.println("adding");
        ram = add(ram, Range.closed(-1, 4), 1.);
        ram.asMapOfRanges().forEach((r, d) -> System.out.println(String.format("%s %f", r, d)));
        System.out.println("adding");
        ram = add(ram, Range.closed(3, 5), 2.);
        ram.asMapOfRanges().forEach((r, d) -> System.out.println(String.format("%s %f", r, d)));
        System.out.println("asReal");
        ram.asMapOfRanges().forEach((r, d) -> System.out.println(String.format("%s %f", r, d)));
        toReal(ram).asMapOfRanges().forEach((r, d) -> System.out.println(String.format("%s %f", r, d)));
        
        RangeMap<Double, Double> toReal = toReal(ram);
        System.out.println("span "+toReal.span());
        double length = length(toReal.span());
        System.out.println("length "+length);
        int k = 7;
        double binSize = length/k;
        Double lb = toReal.span().lowerEndpoint();
        for(int i=0; i<k; i++){
            Range<Double> closedOpen = Range.closedOpen(lb+(i*1./k * length), lb+((i+1.)/k * length));
            System.out.println("bin "+i);
            System.out.println(closedOpen);
            Double max = Collections.max(toReal.subRangeMap(closedOpen).asMapOfRanges().values());
            Double mean = toReal.subRangeMap(closedOpen).asMapOfRanges().values().stream().mapToDouble(Double::doubleValue).average().orElse(0.);
            
//            StreamSupport.doubleStream(new Spliterator.OfDouble()
//                    false);
            System.out.println("max "+max);
            System.out.println("mean "+mean);
        }
        
    }

}
