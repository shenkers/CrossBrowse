/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.util.concurrent.FutureTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;

/**
 *
 * @author sol
 */
public interface View<T> {
    
    public Task<Pane> getContent(T context);
    
}
