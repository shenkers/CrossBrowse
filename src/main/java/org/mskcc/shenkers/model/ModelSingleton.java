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
import java.util.Observable;
import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.AbstractContext;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
public class ModelSingleton {

    private static final Logger logger = LogManager.getLogger();

    private static ModelSingleton instance = null;

    private ModelSingleton() {
        genomes = FXCollections.observableArrayList();
        tracks = FXCollections.observableHashMap();
        nTracks = 0;
        spans = FXCollections.observableHashMap();
    }

    public static synchronized ModelSingleton getInstance() {
        if (instance == null) {
            instance = new ModelSingleton();
        }

        return instance;
    }

    ObservableList<Genome> genomes;
    Map<Genome, ObservableList<Track<AbstractContext>>> tracks;
    Map<Genome, Property<Optional<GenomeSpan>>> spans;

    private int nTracks;

    public ObservableValue<Optional<GenomeSpan>> getSpan(Genome g) {
        return spans.get(g);
    }

    public void setSpan(Genome g, Optional<GenomeSpan> span) {
        logger.info("genome {}",g);
        logger.info("spans.get(g) {}",spans.get(g));
        logger.info("span {}",span);
        this.spans.get(g).setValue(span);
    }

    public void addTrack(Genome g, Track track) {
        tracks.get(g).add(track);
        track.getSpan().bind(getSpan(g));
        setnTracks(getnTracks() + 1);
    }

    public void removeTrack(Genome g, Track track) {
        tracks.get(g).remove(track);
        setnTracks(getnTracks() - 1);
    }

    public ObservableList<Genome> getGenomes() {
        return genomes;
    }

    public void addGenome(Genome g) {
        tracks.put(g, FXCollections.observableArrayList());
        spans.put(g, new SimpleObjectProperty<>(Optional.empty()));
        genomes.add(g);
    }

    public void removeTrack(Genome g) {
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

    public ObservableList<Track<AbstractContext>> getTracks(Genome g) {
        return tracks.get(g);
    }

}
