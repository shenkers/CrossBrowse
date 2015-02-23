/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.liftover;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Soma
 */
public class GFFAlignmentContextEncoder implements AlignmentContextEncoder {

    Writer w;
    public GFFAlignmentContextEncoder(Writer out) {
        w = out;
    }

    
    @Override
    public void encode(AlignmentContext context) throws IOException{
        w.write(String.format(
                "%s\t"
                        + "CGV\t"
                        + "chain\t"
                        + "%d\t"
                        + "%d\t"
                        + "%f\t"
                        + "+\t"
                        + ".\t"
                        + "id \"%s\"; "
                        + "targetChr \"%s\"; "
                        + "targetStart \"%d\"; "
                        + "targetEnd \"%d\"; "
                        + "toNegativeStrand \"%b\"; "
                        + "queryStarts \"%s\"; "
                        + "targetStarts \"%s\"; "
                        + "blockLengths \"%s\";"
                        + "\n", 
                context.getChr(),
                context.getStart(),
                context.getEnd(),
                context.getScore(),
                context.getId(),
                context.getTargetChr(),
                context.getTargetStart(),
                context.getTargetEnd(),
                context.getToNegativeStrand(),
                StringUtils.join(context.getQueryStarts(),','),
                StringUtils.join(context.getTargetStarts(),','),
                StringUtils.join(context.getBlockLengths(),',')
                ));
    }

    @Override
    public void close() throws IOException {
        w.close();
    }
    
}
