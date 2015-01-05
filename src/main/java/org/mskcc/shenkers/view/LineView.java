/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import javafx.scene.Node;
import org.mskcc.shenkers.data.IntervalDataSource;
import org.mskcc.shenkers.data.RealValueDataSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class LineView implements DataView<RealValueDataSource>{

    @Override
    public Node getGraphics(GenomeSpan span, RealValueDataSource dataSource) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
