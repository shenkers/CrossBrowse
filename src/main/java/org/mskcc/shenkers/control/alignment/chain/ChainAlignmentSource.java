/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment.chain;

import htsjdk.samtools.liftover.LiftOver;
import java.io.File;

/**
 *
 * @author Soma
 */
public class ChainAlignmentSource {
    
    public void test(){
        File chainFile=null;
        LiftOver lo = new LiftOver(chainFile);
//        lo.diagnosticLiftover(null)
    }
}
