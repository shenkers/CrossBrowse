/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;

/**
 *
 * @author sol
 */
public class RenderStrategy<T extends AbstractContext> extends Service<Pane> {

    View<T> view;
    T context;

    public void setView(View<T> view) {
        this.view = view;
    }
    
    public void setContext(T context) {
        this.context = context;
    }

    protected Task<Pane> createTask() {
        return view.getContent(context);
    }

}
