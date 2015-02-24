/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.tabix;

import java.io.IOException;

/**
 *
 * @author Soma
 */
public interface AlignmentContextEncoder {
    public void encode(AlignmentContext context) throws IOException;

    public void close() throws IOException;
}
