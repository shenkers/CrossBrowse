/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import javafx.scene.layout.Pane;

/**
 *
 * @author sol
 */
public interface View<T> {
    
    public Pane getContent(T context);
    
}
