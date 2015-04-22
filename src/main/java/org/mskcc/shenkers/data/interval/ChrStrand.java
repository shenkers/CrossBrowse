/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.data.interval;

import htsjdk.tribble.annotation.Strand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author sol
 */
class ChrStrand {

    String chr;
    Strand strand;

    public ChrStrand(String chr, Strand strand) {
        this.chr = chr;
        this.strand = strand;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(chr).append(strand).build();
    }
}
