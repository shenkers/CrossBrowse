/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.data_impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.mskcc.shenkers.control.track.bam.BamView1;
import org.mskcc.shenkers.control.track.bam.BamView2;
import org.mskcc.shenkers.control.track.TrackFactory;
import org.mskcc.shenkers.control.track.TrackBuilder;
import org.mskcc.shenkers.control.track.TrackBuilderImpl;
import org.mskcc.shenkers.control.track.TrackFactoryImpl;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.control.track.config.TrackConfiguration;
import org.mskcc.shenkers.data.DataSource;
import org.mskcc.shenkers.data.IntervalDataSource;
import org.mskcc.shenkers.data.RealValueDataSource;
import org.mskcc.shenkers.view.DataView;
import org.mskcc.shenkers.view.HistogramView;
import org.mskcc.shenkers.view.IntervalView;
import org.mskcc.shenkers.view.LineView;

/**
 *
 * @author sol
 */
public class SamDataSourceTest {

    public SamDataSourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @org.junit.Test
    public void testOptionalProperty() {
        
        DoubleProperty dp = new SimpleDoubleProperty(1);
        
        System.out.println(dp);
        System.out.println(dp.multiply(2.).get());
//        System.exit(0);
        
        Property<Optional<String>> pos = new SimpleObjectProperty<>(Optional.empty());
        Map<String, Property<Optional<String>>> m = new HashMap<>();
        m.put("key", pos);
        
        System.out.println(m.get("key"));

        Optional<String> reversed = pos.getValue().flatMap((String s) -> Optional.of(new StringBuilder(s).reverse().toString()));

        reversed.ifPresent((s) -> System.out.println("present: " + s));
        System.out.println("get orElse: " + reversed.map(Function.identity()).orElse("not present"));

//        pos.setValue(Optional.of("HelloWorld"));
        
        m.get("key").setValue(Optional.of("HelloWorld"));
        System.out.println(m.get("key"));
        
        reversed = pos.getValue().flatMap((String s) -> Optional.of(new StringBuilder(s).reverse().toString()));
        reversed.ifPresent((s) -> System.out.println("present: " + s));
        System.out.println("get orElse: " + reversed.map(Function.identity()).orElse("not present"));

    }

    @org.junit.Test
    public void testSomeMethod() {

        Injector inj;
        inj = Guice.createInjector(new TrackConfiguration(), new AbstractModule() {

//            class ABC implements Serializable{
//                
//            }
            @Override
            protected void configure() {

                {
//                    bind(TrackBuilder.class).to(TrackBuilderImpl.class);
//                    bind(BamView.class).to(BamViewImpl.class);
                }
//                TypeLiteral<DataView<IntervalDataSource>> tl = new TypeLiteral<>
                {
                    Multibinder<DataView<IntervalDataSource>> mb = Multibinder.newSetBinder(binder(), new TypeLiteral<DataView<IntervalDataSource>>() {
                    });
                    mb.addBinding().to(IntervalView.class);
                }
                {
                    Multibinder<DataView<RealValueDataSource>> mb = Multibinder.newSetBinder(binder(), new TypeLiteral<DataView<RealValueDataSource>>() {
                    });
                    mb.addBinding().to(HistogramView.class);
                    mb.addBinding().to(LineView.class);
                }
//                {
//                    MapBinder<String,DataView> mb = MapBinder.newMapBinder(binder(), String.class, DataView.class);
//                    mb.permitDuplicates();
//                    mb.addBinding(null)
//                }
                {

//                FactoryModuleBuilder fmb = new FactoryModuleBuilder().implement(BAMTrack.class, BAMTrackImpl.class);
//                    install(fmb.build(TrackFactory.class));
                }
            }
        });

        {
            TrackFactory<BamContext> instance = inj.getInstance(Key.get(new TypeLiteral<TrackFactory<BamContext>>() {
            }));
            System.out.println(instance);
        }

        {
//            Key<Set<DataView>> get = Key.get(new TypeLiteral<Set<DataView>>() {
//            });
//            Set<DataView> instance = inj.getInstance(get);
//            System.err.println(instance);
        }

//        inj.getInstance(TrackBuilder.class).load(FileType.BAM, new File("/dev/stdin"));
        Key<?> key = Key.get(Types.newParameterizedType(DataView.class, RealValueDataSource.class));
        TypeLiteral<Set<DataView<RealValueDataSource>>> tlit = new TypeLiteral<Set<DataView<RealValueDataSource>>>() {
        };
        Set<DataView<RealValueDataSource>> instance = inj.getInstance(key.get(tlit));
        for (DataView<RealValueDataSource> dv : instance) {
            System.out.println(dv.getClass().getCanonicalName());
        }

        // TODO review the generated test code and remove the default call to fail.
        SamDataSource sds = new SamDataSource();

        for (Class at : sds.getClass().getInterfaces()) {
            boolean interfaceIsDataSource = DataSource.class.isAssignableFrom(at);
            if (interfaceIsDataSource) {
                System.out.println(at.getCanonicalName());
                ParameterizedType pt = Types.newParameterizedType(DataView.class, at);
                Key<?> dataViewType = Key.get(pt);

                ParameterizedType setOfDataViewTypes = Types.newParameterizedType(Set.class, pt);
                Set instance1 = (Set) inj.getInstance(Key.get(setOfDataViewTypes));
                for (Object o : instance1) {
                    System.out.println("provided by: " + o.getClass().getCanonicalName());
                }
            } else {
                System.out.println("is not data source: " + at.getCanonicalName());
            }
        }
        if (false) {
            fail("The test case is a prototype.");
        }

    }

}
