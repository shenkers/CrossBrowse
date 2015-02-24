/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdk.samtools.tabix;

import htsjdk.samtools.util.LocationAware;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps output stream in a manner which keeps track of the position within the file and allowing writes
 * at arbitrary points
 */
public class PositionalOutputStream  extends OutputStream implements LocationAware
{
    private final OutputStream out;
    private long position = 0;

    public PositionalOutputStream(final OutputStream out) {
        this.out = out;
    }

    public final void write(final byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    public final void write(final byte[] bytes, final int startIndex, final int numBytes) throws IOException {
        position += numBytes;
        out.write(bytes, startIndex, numBytes);
    }

    public final void write(final int c)  throws IOException {
        position++;
        out.write(c);
    }

    public final long getPosition() { 
        return position; 
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }
}
