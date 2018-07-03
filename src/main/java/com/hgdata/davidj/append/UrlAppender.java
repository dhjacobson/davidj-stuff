package com.hgdata.davidj.append;

import com.hgdata.davidj.append.models.AppendRequest;
import com.hgdata.davidj.append.models.AppendResponse;
import com.hgdata.davidj.append.models.ResponseStatus;
import com.hgdata.davidj.append.models.SearchResultSet;
import com.hgdata.davidj.db.AthenaDb;
import com.hgdata.filters.character.CharacterFilter;
import com.hgdata.normalization.entities.company.CompanyRootCleaner;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by david on 2/1/18.
 */
public class UrlAppender {

    private AthenaDb athena = new AthenaDb();
    private final CharacterFilter characterFilter = new CharacterFilter();
    private final CompanyRootCleaner rootCleaner = new CompanyRootCleaner();
    private final MarketMap marketMap = MarketMap.build();
    private final CandidateScorer candidateScorer = new CandidateScorer();

    public UrlAppender() throws IOException {
    }

    public List<AppendResponse> doAppend(List<AppendRequest> requests) throws SQLException {
        Map<AppendRequest, Set<Candidate>> candidates = fetchCandidatesFromSearchResults(requests);
        List<AppendResponse> responses = new ArrayList<>();
        for (AppendRequest request : requests) {
            AppendResponse response = new AppendResponse();
            response.setRequest(request);
            if (candidates.containsKey(request)) {
                Candidate bestCandidate = chooseBestCandidate(candidates.get(request));
                if (bestCandidate != null) {
                    response.setStatus(ResponseStatus.SUCCESS);
                    response.setWinningCandidate(bestCandidate);
                    response.setWinningScore(candidateScorer.candidateScore(response.getWinningCandidate()));
                    response.setUrl(response.getWinningCandidate().getUrl());
                }
            }
            if (response.getUrl() == null) {
                response.setStatus(ResponseStatus.FAILURE);
            }
            responses.add(response);
        }

        return responses;
    }

    private Candidate chooseBestCandidate(Set<Candidate> candidates) {
        return candidates.stream().max(Comparator.comparing(candidateScorer::candidateScore)).orElse(null);
    }

    private Map<AppendRequest, Set<Candidate>> fetchCandidatesFromFirmographics(List<AppendRequest> requests) {
        String sql = "";
        return null;
    }

    private Map<AppendRequest, Set<Candidate>> fetchCandidatesFromSearchResults(List<AppendRequest> requests) throws SQLException {
        String sql = String.format("select * from (\n" +
                        "SELECT \n" +
                        "  regexp_extract(d.results[1].__metadata.uri, 'Web\\?Query=''(.*?)''&(?:Market|\\$skip)=', 1) as search_text,\n" +
                        "  regexp_extract(d.results[1].__metadata.uri, '&Market=''(.*?)''&\\$skip=', 1) as market,\n" +
                        "  array_join(transform(d.results, x -> concat(cast(cast(regexp_extract(x.__metadata.uri, '&\\$skip=(\\d+)&\\$top=1', 1) as integer) + 1 as varchar), '|', x.Url)), '|||') as search_results\n" +
                        "FROM \"url_appender\".\"20171205_03f3942c9af739eed2df768d1eb04865\"\n" +
                        "where \n" +
                        "  cardinality(d.results) > 0\n" +
                        ") where search_text in ('%s')"
                , requests
                        .stream()
                        .map(AppendRequest::getSearchText)
                        .collect(Collectors.joining( "', '" ))
                );

        ResultSet rs = athena.fetch(sql);

        List<SearchResultSet> searchResultSets = new ArrayList<>();
        while (rs.next()) {
            searchResultSets.add(SearchResultSet.fromResultSet(rs));
        }

        Map<AppendRequest, Set<Candidate>> toReturn = new HashMap<>();
        for (AppendRequest request : requests) {
            Function<SearchResultSet, Integer> requestToSearchResultSetMatchScore = srs -> {
                int score = 0;
                String resultMarket = srs.getMarket();
                if (resultMarket != null) {
                    if (resultMarket.equals(marketMap.findMarket(request))) {
                        score++;
                    }
                }
                return score;
            };

            Optional<SearchResultSet> bestSearchResultSet = searchResultSets.stream()
                    .filter(x -> x.getSearchText().equals(request.getSearchText()))
                    .max(Comparator.comparing(requestToSearchResultSetMatchScore));

            bestSearchResultSet.ifPresent(x -> toReturn.put(request, x.buildCandidates()));
        }

        return toReturn;

    }

    public static void main(String[] args) throws IOException, SQLException {
        UrlAppender urlAppender = new UrlAppender();
        List<AppendRequest> requests = new ArrayList<>();
        requests.add(new AppendRequest("IBM", "US"));
        requests.add(new AppendRequest("HG Data", "US"));
        requests.add(new AppendRequest("Microsoft", "FR"));

        List<AppendResponse> responses = urlAppender.doAppend(requests);

        responses.forEach(System.out::println);

    }
}
