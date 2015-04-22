/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.FutureTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 *
 * @author sol
 */
public interface View<T> {
    
    public Task<Pane> getContent(T context);
    
    default public Collection<MenuItem> getMenuItems(T context){
        return new ArrayList<>();
    }
    
}
