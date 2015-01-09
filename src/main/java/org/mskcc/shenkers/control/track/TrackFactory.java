/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import java.io.File;

/**
 *
 * @author sol
 * @param <T>
 */
public interface TrackFactory<T extends AbstractContext> {
    public Track<T> create(T context);
}
