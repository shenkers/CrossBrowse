/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.view;

import javafx.scene.Node;
import org.mskcc.shenkers.data.DataSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public interface DataView<T extends DataSource> {    
    public Node getGraphics(GenomeSpan span, T dataSource);
}
