package com.hgdata.davidj.append;

import com.hgdata.commons.models.Firmographics;
import com.hgdata.commons.models.Location;
import com.hgdata.davidj.append.models.SearchResult;
import com.hgdata.davidj.utils.Utils;
import com.hgdata.url.utils.URLUtils;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by david on 2/1/18.
 */
public class Candidate {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private String cleanCompanyName;
    private String url;
    private Set<SearchResult> searchResults;
    private Set<Integer> searchRanks;
    private Set<Firmographics> firmographics;

    public Candidate() {

    }

    public String getCleanCompanyName() {
        return cleanCompanyName;
    }

    public void setCleanCompanyName(String cleanCompanyName) {
        this.cleanCompanyName = cleanCompanyName;
    }

    public String getUrl() {
        return url;
    }

    public Set<Integer> getSearchRanks() {
        return searchRanks;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSearchRanks(Set<Integer> searchRanks) {
        this.searchRanks = searchRanks;
    }

    public int getHighestRank() {
        return Collections.min(searchRanks);
    }

    public Set<Integer> getAdditionalRanks() {
        return searchRanks.stream().filter(x -> !x.equals(getHighestRank())).collect(Collectors.toSet());
    }

    public String getTopPrivateDomain() {
        try {
            return Utils.getCleanedURL(this.url).getTopPrivateDomain();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public String getPublicDomain() {
        try {
            return Utils.getCleanedURL(this.url).getPublicDomain();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public Set<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(Set<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public Set<Firmographics> getFirmographics() {
        return firmographics;
    }

    public void setFirmographics(Set<Firmographics> firmographics) {
        this.firmographics = firmographics;
    }

    public Candidate mergeWith(Candidate other) {
        this.searchRanks.addAll(other.getSearchRanks());
        this.searchResults.addAll(other.getSearchResults());
        this.firmographics.addAll(other.getFirmographics());
        return this;
    }

    public static Candidate fromSearchResult(SearchResult searchResult) {
        Candidate candidate = new Candidate();

        try {
            candidate.setUrl(URLUtils.cleanURL(searchResult.getRawUrl()));
        } catch(MalformedURLException e) {
            return null;
        }

        candidate.setSearchRanks(Collections.singleton(searchResult.getRank()));

        return candidate;
    }




    /**
     * Encapsulate data for an external URL that matches the URL, company name, and country of the candidate, but may
     * or may not match the state.
     */
    public class ExternalURL {

        private int source;
        private Date verifiedDate;
        private String state;

        /**
         * @param source       the external source ID
         * @param verifiedDate the most recent verified date
         * @param state        the state
         */
        public ExternalURL(int source, Date verifiedDate, String state) {
            this.source = source;
            this.verifiedDate = verifiedDate;
            this.state = state;
        }

        public int getSource() {
            return source;
        }

        public Date getVerifiedDate() {
            return verifiedDate;
        }

        public String getState() {
            return state;
        }

    }


}
