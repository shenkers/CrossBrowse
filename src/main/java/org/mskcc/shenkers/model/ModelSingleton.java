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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.mskcc.shenkers.imodel.Track;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ModelSingleton {

    private static ModelSingleton instance = null;

    private ModelSingleton() {
        genomes = FXCollections.observableArrayList();
        tracks = FXCollections.observableHashMap();
        nTracks=0;
        span = new SimpleObjectProperty<>(new GenomeSpan("", 0, 0, false));
    }

    public static synchronized ModelSingleton getInstance() {
        if (instance == null) {
            instance = new ModelSingleton();
        }

        return instance;
    }

    Property<GenomeSpan> span;
    ObservableList<Genome> genomes;
    Map<Genome,ObservableList<Track>> tracks;
    
    private int nTracks;

    public Property<GenomeSpan> genomeSpanProperty(){
        return span;
    }
    
    public GenomeSpan getSpan() {
            return span.getValue();
    }

    public void setSpan(GenomeSpan span) {
            this.span.setValue(span);
    }
    
    public void addTrack(Genome g, Track track){
        tracks.get(g).add(track);
        setnTracks(getnTracks() + 1);
    }
    
    public void removeTrack(Genome g, Node track){
        tracks.get(g).remove(track);
        setnTracks(getnTracks() - 1);
    }
    
    public ObservableList<Genome> getGenomes(){
        return genomes;
    }
    
    public void addGenome(Genome g){
        tracks.put(g, FXCollections.observableArrayList());
        genomes.add(g);
    }
    
    public void removeTrack(Genome g){
        tracks.remove(g);
        genomes.remove(g);
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

    public ObservableList<Track> getTracks(Genome g) {
        return tracks.get(g);
    }

}
