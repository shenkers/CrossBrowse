/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.shenkers.model.datatypes.Genome;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ModelSingleton {

    private static ModelSingleton instance = null;

    private ModelSingleton() {
        genomes = new ArrayList<>();
        tracks = new HashMap<>();
        nTracks=0;
    }

    public static synchronized ModelSingleton getInstance() {
        if (instance == null) {
            instance = new ModelSingleton();
        }

        return instance;
    }

    GenomeSpan span;
    List<Genome> genomes;
    Map<Genome,List<Node>> tracks;
    
    private int nTracks;

    public GenomeSpan getSpan() {
        synchronized (span) {
            return span;
        }
    }

    public void setSpan(GenomeSpan span) {
        synchronized (span) {
            this.span = span;
        }
    }
    
    public void addTrack(Genome g, Node track){
        tracks.get(g).add(track);
        setnTracks(getnTracks() + 1);
    }
    
    public void removeTrack(Genome g, Node track){
        tracks.get(g).remove(track);
        setnTracks(getnTracks() - 1);
    }
    
    public void addGenome(Genome g){
        tracks.put(g, new ArrayList<Node>());
    }
    
    public void removeTrack(Genome g){
        tracks.remove(g);
    }

    /**
     * @return the nTracks
     */
    public int getnTracks() {
        return nTracks;
    }

    /**
     * @param nTracks the nTracks to set
     */
    public void setnTracks(int nTracks) {
        this.nTracks = nTracks;
    }

}
