/*
 * Author: Alex Flury
 * Date: 5/22/2015
 * Copyright (c) 2015 HG Data, Inc.
 * www.hgdata.com
 */

package com.hgdata.davidj.append.models.blacklist;

/**
 * Encapsulates model data for a blacklist match type.
 */
public class BlacklistType {

    private int id;
    private String identifier;
    private String label;

    /**
     * @param id         an integer used to identify this type of blacklist matching
     * @param identifier a string used to identify this type of blacklist matching
     * @param label      a human readable label for this type of blacklist matching
     */
    public BlacklistType(int id, String identifier, String label) {
        this.id = id;
        this.identifier = identifier;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
