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
import com.google.inject.internal.MoreTypes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
    public void testSomeMethod() {

        Injector inj = Guice.createInjector(new AbstractModule() {

//            class ABC implements Serializable{
//                
//            }
            @Override
            protected void configure() {
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
            }
        });
                
       

        Key<?> key = Key.get(Types.newParameterizedType(DataView.class, RealValueDataSource.class));
        TypeLiteral<Set<DataView<RealValueDataSource>>> tlit = new TypeLiteral<Set<DataView<RealValueDataSource>>>(){};
        Set<DataView<RealValueDataSource>> instance = inj.getInstance(key.get(tlit));
        for(DataView<RealValueDataSource> dv : instance){
            System.out.println(dv.getClass().getCanonicalName());
        }
        

        // TODO review the generated test code and remove the default call to fail.
        SamDataSource sds = new SamDataSource();

        for (Class at : sds.getClass().getInterfaces()) {
            boolean interfaceIsDataSource = DataSource.class.isAssignableFrom(at);
            if(interfaceIsDataSource){
            System.out.println(at.getCanonicalName());
            ParameterizedType pt = Types.newParameterizedType(DataView.class, at);
            Key<?> dataViewType = Key.get(pt);
            
            ParameterizedType setOfDataViewTypes = Types.newParameterizedType(Set.class, pt);
            Set instance1 = (Set) inj.getInstance(Key.get(setOfDataViewTypes));
            for(Object o : instance1){
                System.out.println("provided by: "+o.getClass().getCanonicalName());
            }
            }
            else{
                System.out.println("is not data source: "+at.getCanonicalName());
            }
        }
        fail("The test case is a prototype.");
        
    }

}
