/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model;

import com.sun.javafx.geom.Edge;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.imodel.AlignmentSource;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ViewModel {
    
    Map<Genome,List<Track>> loadedTracks;
    Map<Genome,GenomeSpan> genomeSpans;
    Map<Set<Genome>,AlignmentSource> genomeAlignments;
    
}
