/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.fasta;

import com.google.common.io.Files;
import htsjdk.tribble.readers.PositionalBufferedStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sol
 */
public class FastaTools {
    
    enum State {
        init, head, seqLineInit, seqLine
    }

    private static final Logger logger = LogManager.getLogger();
    
    final static String format = "%s\t%d\t%d\t%d\t%d\n";
    
    public static void createIndex(File fasta) throws FileNotFoundException, IOException {
        PrintStream out = new PrintStream(new FileOutputStream(new File(fasta.getAbsolutePath().concat(".fai"))));
        PositionalBufferedStream s = new PositionalBufferedStream(new FileInputStream(fasta));

        State state = State.init;
        StringBuilder header = new StringBuilder();

        String name = null;
        Long seqStart = null;
        Long lineStart = null;
        Long chrLen = null;

        TreeSet<Integer> bytesPerLine = new TreeSet<>();

        while (!s.isDone()) {
            long curPos = s.getPosition();
            char read = (char) s.read();
            if (state == State.init && read == '>') {
                state = State.head;
                header.setLength(0);
                chrLen = 0l;
            } else if (state == State.head) {
                if (read != '\n') {
                    header.append(read);
                } else {
                    name = header.toString().split("\\W")[0];
                    state = State.seqLineInit;
                    seqStart = curPos + 1;
                }
            } else if (state == State.seqLineInit) {
                lineStart = curPos;
                // check if we have seen two new lines, otherwise
                // keep reading
                if (!(read == '>' || read == '\n')) {
                    state = State.seqLine;
                } else {
                    int maxBytes = bytesPerLine.last();
                    out.printf(format, name, chrLen, seqStart, maxBytes - 1, maxBytes);
                    if (read == '>') {
                        chrLen = 0l;
                        header.setLength(0);
                        bytesPerLine.clear();
                        state = State.head;
                    } else {
                        state = State.init;
                    }
                }
            } else if (state == State.seqLine) {
                if (read != '\n') {
                    // keep reading                    
                } else {
                    chrLen += curPos - lineStart;
                    bytesPerLine.add((int) (curPos - lineStart + 1));
                    state = State.seqLineInit;
                }
            }
        }

        if (name != null) {
            int maxBytes = bytesPerLine.last();
            out.printf(format, name, chrLen, seqStart, maxBytes - 1, maxBytes);
        }
        
        out.close();
    }
}
