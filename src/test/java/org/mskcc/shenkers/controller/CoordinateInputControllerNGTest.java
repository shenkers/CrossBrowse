/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Function;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.util.Callback;
import org.fxmisc.easybind.EasyBind;
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
public class CoordinateInputControllerNGTest {

    public CoordinateInputControllerNGTest() {
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

    @Test
    public void testInitialize() {
        ObservableMap<String, StringProperty> m = FXCollections.observableHashMap();
        m.put("a", new SimpleStringProperty());
        m.addListener(new MapChangeListener<String, StringProperty>() {

            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends StringProperty> change) {
                System.out.println("change detected " + change);
            }
        });

        m.put("b", new SimpleStringProperty());

//        class cd extends ListBinding<StringProperty> {
//
//            public cd() {
//                bind(m);
//            }
//            
//            @Override
//            protected ObservableList<StringProperty> computeValue() {
//                Collection<StringProperty> values = m.values();
//                ObservableList<StringProperty> observableArrayList = FXCollections.observableArrayList(values);
//                return observableArrayList;
//            }
//
//        }
//        
//        cd ef = new cd();
        
        ObjectBinding<List<StringProperty>> createObjectBinding = Bindings.createObjectBinding(() -> new ArrayList<>(m.values()), m);
        List<StringProperty> get = createObjectBinding.get();
        createObjectBinding.addListener(new ChangeListener<List<StringProperty>>() {
            public void changed(ObservableValue<? extends List<StringProperty>> observable, List<StringProperty> oldValue, List<StringProperty> newValue) {
                System.out.println("list of values has changed " + newValue);
            }
        });
        ObservableList<StringProperty> observableList = FXCollections.observableList(get, new Callback<StringProperty, Observable[]>() {
            public Observable[] call(StringProperty param) {
                return new Observable[]{param};
            }
        });
        observableList.addListener(new ListChangeListener<StringProperty>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends StringProperty> c) {
                System.out.println("list change detected " + c);
            }
        });
        m.get("a").setValue("c");

        m.put("d", new SimpleStringProperty());

    }

}
