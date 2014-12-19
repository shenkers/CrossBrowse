/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model.datatypes;

/**
 *
 * @author sol
 */
public class Genome {

    private String id;
    private String description;

    public Genome(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public boolean equals(Genome obj) {
        if (obj == null) {
            return false;
        }
        return getId().equals(obj.getId());
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
