package com.hgdata.davidj.append;

import com.hgdata.davidj.append.models.SearchResult;
import com.hgdata.davidj.append.models.blacklist.BlacklistedURL;

import java.util.List;
import java.util.Set;

public class CandidateScorer {

    public double candidateScore(Candidate candidate) {
        return highestRankScore(candidate) + additionalRanksScore(candidate);
    }

    public double rankScore(Candidate candidate) {
        return highestRankScore(candidate) + additionalRanksScore(candidate);
    }

    public double highestRankScore(Candidate candidate) {
        switch (candidate.getHighestRank()) {
            case 1: return 4.54;
            case 2: return 2.66;
            case 3: return 1.86;
            case 4: return 1.35;
            case 5: return 0.85;
            case 6: return 0.37;
            default: return 0;
        }
    }

    public double additionalRanksScore(Candidate candidate) {
        return candidate.getAdditionalRanks().stream().mapToDouble(rank -> {
            if (rank <= 5) {return 0.514;}
            if (rank <= 10) {return 0.175;}
            return 0;
        }).sum();
    }

    public double publicDomainScore(Candidate candidate) {
        return "com".equals(candidate.getPublicDomain()) ? .81 : 0;
    }

    private Set<SearchResult> bestSearchResults(Candidate candidate) {
        return null;
    }

    /**
     * Checks whether the candidate is blacklisted.
     *
     * @param blacklist a list of {@code BlacklistedURL} objects
     * @return {@code true} is the candidate is blacklisted; {@code false} otherwise
     */
    private boolean checkIsBlacklisted(
            Candidate candidate,
            List<BlacklistedURL> blacklist) {
        for (BlacklistedURL blacklistedUrl : blacklist) {
            if (blacklistedUrl.matches(candidate)) {
                return true;
            }
        }
        return false;
    }

}
