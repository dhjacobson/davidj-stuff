package com.hgdata.davidj.append.models;

import java.sql.ResultSet;

/**
 * Created by david on 2/1/18.
 */
public class SearchResult {

    String rawUrl;
    Integer rank;

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public static SearchResult fromString(String s) {
        SearchResult sr = new SearchResult();
        String[] fields = s.split("\\|");
        sr.setRank(Integer.parseInt(fields[0]));
        sr.setRawUrl(fields[1]);

        return sr;
    }

}
