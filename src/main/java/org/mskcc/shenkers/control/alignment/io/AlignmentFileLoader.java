/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.alignment.io.AlignmentLoader;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.control.alignment.AlignmentType;
import org.mskcc.shenkers.control.alignment.chain.ChainAlignmentSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;


public class AlignmentFileLoader implements AlignmentLoader {
    Logger logger = LogManager.getLogger();
    @Override
    public AlignmentSource load(String uri) {
        logger.info("returning dummy alignment source");
        return new ChainAlignmentSource(uri);
    }
    
}
