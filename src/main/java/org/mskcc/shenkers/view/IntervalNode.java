/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.DomainFlippable;

/**
 *
 * @author sol
 */

public class IntervalNode<T extends Pane & DomainFlippable> {

    private static final Logger logger = LogManager.getLogger();

    Pair<Integer, Integer> interval;
    T content;

    public IntervalNode(Pair<Integer, Integer> interval, T content) {
        this.interval = interval;
        this.content = content;
    }
}
