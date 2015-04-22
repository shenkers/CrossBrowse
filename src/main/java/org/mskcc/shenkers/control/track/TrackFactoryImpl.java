/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;


public class TrackFactoryImpl<T extends AbstractContext> implements TrackFactory<T> {

    List<View<T>> views;
    
    Executor executor;

    @Inject
    public TrackFactoryImpl(Set<View<T>> views, Executor executor) {
        this.views = new ArrayList<>(views);
        this.executor = executor;
        
        views.stream().forEach((View<T> v) ->{ System.out.println(v);});
    }
    
    @Override
    public Track<T> create(T context) {
        return new Track(context, views, executor);
    }
    
}
