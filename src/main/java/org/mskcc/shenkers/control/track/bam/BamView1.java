/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bam;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.mskcc.shenkers.control.track.View;

/**
 *
 * @author sol
 */
public class BamView1 implements View<BamContext>{

    @Override
    public Node getContent(BamContext context) {
        return new Label("bamview1");
    }
    
}
