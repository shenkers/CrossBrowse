/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

/**
 *
 * @author sol
 */
public interface AlignmentLoader {
    public AlignmentSource load(AlignmentType type, String uri);
}
