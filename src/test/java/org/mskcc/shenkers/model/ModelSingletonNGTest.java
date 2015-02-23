/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
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
public class ModelSingletonNGTest {

    public ModelSingletonNGTest() {
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

    Logger logger = LogManager.getLogger();

    /**
     * Test of getInstance method, of class ModelSingleton.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");

        ModelSingleton result = ModelSingleton.getInstance();
        Genome g1 = new Genome("g1", "1");
        Genome g2 = new Genome("g2", "2");
        result.addGenome(g1);
        result.addGenome(g2);
        result.setSpan(g1, Optional.of(new GenomeSpan("x", 1, 2, false)));
        result.setSpan(g2, Optional.of(new GenomeSpan("y", 3, 4, false)));

        ObservableValue[] toArray = result.genomes.stream().map(g -> result.getSpan(g)).collect(Collectors.toList()).toArray(new ObservableValue[0]);

        class SpanBinding extends ObjectBinding {

            Observable[] dependencies;

            public SpanBinding(Observable[] dependencies) {
                this.dependencies = dependencies;
                super.bind(this.dependencies);
            }

            @Override
            protected Object computeValue() {
                logger.info("recomputing span binding");
                return null;
            }

        }
        SpanBinding spanBinding = new SpanBinding(toArray);
        spanBinding.addListener(new InvalidationListener() {

//            @Override
//            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                logger.info("span change detected, updating overlay");
//
//            }

            @Override
            public void invalidated(Observable observable) {
                logger.info("span change detected, updating overlay");
            }
        });
        
        toArray[1].addListener(new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                logger.info("property change detected, updating overlay");

            }
        });
        
        logger.info("{}", Arrays.asList(toArray));
        result.setSpan(g2, Optional.of(new GenomeSpan("y", 3, 5, false)));

    }

}
