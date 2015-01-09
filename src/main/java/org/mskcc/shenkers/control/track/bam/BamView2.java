/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bam;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.mskcc.shenkers.control.track.View;

/**
 *
 * @author sol
 */
public class BamView2 implements View<BamContext>{

    @Override
    public Pane getContent(BamContext context) {
        return new BorderPane(new Label("bamview2"));
    }
    
}
