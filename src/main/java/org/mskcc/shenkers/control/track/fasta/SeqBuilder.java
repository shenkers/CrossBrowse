/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class SeqBuilder {

    private static final Logger logger = LogManager.getLogger();
    String seq;

    public SeqBuilder(String seq) {
        this.seq = seq;
    }

    public Seq build() {

        Seq t = new Seq();
        t.setNumElements(seq.length());
        List<SeqUnit> nts = new ArrayList<>(seq.length());
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            SeqUnit nt = new SeqUnit();
//                                nt.getChildren().setAll(new Text("" + (char) i));
            nt.setText("" + c);
            
            double offset = .05;
            if(c=='T')
                nt.setColor(Color.hsb(((0.00+offset)*360)%360, 1, 1));
            else if(c=='A'){
                nt.setColor(Color.hsb(((0.25+offset)*360)%360, 1, 1));
            }
            else if(c=='C'){
                nt.setColor(Color.hsb(((0.50+offset)*360)%360, 1, 1));
            }
            else if(c=='G'){
                nt.setColor(Color.hsb(((0.75+offset)*360)%360, 1, 1));
            }
            else{
                nt.setColor(Color.GRAY);
            }
//            nt.setColor(new Color(Math.random(), Math.random(), Math.random(), 1));
            nt.prefWidthProperty().bind(t.elemWidth);
            nts.add(nt);
        }

        t.getChildren().setAll(nts);
        return t;
    }
}
