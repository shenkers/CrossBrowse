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
public class TSeqBuilder {

    private static final Logger logger = LogManager.getLogger();

    int seqLen;
    List<String> txts;
    Frame f;

    public TSeqBuilder(int seqLen, List<String> labels, Frame f) {
        this.seqLen = seqLen;
        this.txts = labels;
        this.f = f;
    }    

    public Seq build() {

        Seq t = new Seq();
        t.setNumElements(seqLen);

        int skip = 0;

        if (f == Frame.p1) {
            skip = 1;
        } else if (f == Frame.p2) {
            skip = 2;
        }

        int rem = (seqLen - skip) % 3;

        List<SeqUnit> l = new ArrayList<>();
        if (skip > 0) {
            SeqUnit nt = new SeqUnit();
//                nt.getChildren().setAll(new Text());
//            nt.setText(seq.substring(0, skip));
            nt.prefWidthProperty().bind(t.elemWidth.multiply(skip));
            l.add(nt);
        }

        for (int i = 0; i < txts.size(); i ++) {
            SeqUnit nt = new SeqUnit();
            nt.setText(txts.get(i));
            nt.prefWidthProperty().bind(t.elemWidth.multiply(3));
            if(txts.get(i).equals("M"))
                nt.setColor(Color.rgb(0, 255, 0));
            if(txts.get(i).equals("*"))
                nt.setColor(Color.RED);
            l.add(nt);
        }

        if (rem > 0) {
            SeqUnit nt = new SeqUnit();
//            nt.setText(seq.substring(seq.length() - rem));
            nt.prefWidthProperty().bind(t.elemWidth.multiply(rem));
            l.add(nt);
        }

        t.getChildren().setAll(l);
        return t;
    }
}
