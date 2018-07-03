package com.hgdata.davidj.append.models;

import com.hgdata.davidj.append.Candidate;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class SearchResultSet {

    private String searchText;
    private String market;
    private Date searchedAt;
    private List<SearchResult> searchResults;
    private Set<Candidate> candidates;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Date getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(Date searchedAt) {
        this.searchedAt = searchedAt;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public Set<Candidate> buildCandidates() {
        Collection<Optional<Candidate>> optionalCandidatesCollection = this.getSearchResults()
                .stream()
                .map(Candidate::fromSearchResult)
                .collect(Collectors.groupingBy(Candidate::getUrl, Collectors.reducing(Candidate::mergeWith)))
                .values();

        return new ArrayList<>(optionalCandidatesCollection)
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public int matchScore(String requestMarket) {
        return this.getMarket().equals(requestMarket) ? 1 : 0;
    }

    public static SearchResultSet fromResultSet(ResultSet rs) throws SQLException {
        SearchResultSet srs = new SearchResultSet();
        srs.setSearchText(rs.getString("search_text"));
        srs.setMarket(rs.getString("market"));

        String searchResultsString = rs.getString("search_results");
        List<SearchResult> searchResults = Arrays.stream(searchResultsString.split("\\|\\|\\|")).
                map(SearchResult::fromString)
                .filter(searchResult -> searchResult.getRank() <= 10)
                .collect(Collectors.toList());
        srs.setSearchResults(searchResults);

        return srs;
    }
}
