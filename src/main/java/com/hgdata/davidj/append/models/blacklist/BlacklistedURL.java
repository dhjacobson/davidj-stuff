/*
 * Author: Alex Flury
 * Date: 5/22/2015
 * Copyright (c) 2015 HG Data, Inc.
 * www.hgdata.com
 */

package com.hgdata.davidj.append.models.blacklist;

import com.hgdata.davidj.append.Candidate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates model data for a blacklisted URL object.
 */
public class BlacklistedURL {

    private String url;
    private String matchString;
    private BlacklistType type;

    /**
     * @param url         the URL that is blacklisted
     * @param matchString the blacklist is bypassed when a {@code Candidate} matches this string
     * @param type        the type of matching algorithm to use when matching a {@code Candidate} using the match string
     */
    private BlacklistedURL(String url, String matchString, BlacklistType type) {
        this.url = url;
        this.matchString = matchString;
        this.type = type;
    }

    /**
     * @param url            the URL that is blacklisted
     * @param matchString    the blacklist is bypassed when a {@code Candidate} matches this string
     * @param matchType      a string identifying the type of matching algorithm to use when matching a {@code
     *                       Candidate} using the match string
     * @param blacklistTypes a list of possible matching algorithm types
     */
    private BlacklistedURL(String url, String matchString, String matchType, List<BlacklistType> blacklistTypes) {
        this.url = url;
        this.matchString = matchString;

        for (BlacklistType typeOption : blacklistTypes) {
            if (typeOption.getIdentifier().equals(matchType)) {
                this.type = typeOption;
            }
        }
    }

    /**
     * Deserializes a {@code BlacklistedURL} object from a CSV entry.
     *
     * @param values         an array of string values parsed from a CSV file
     * @param blacklistTypes a list of {@code BlacklistType} objects
     * @return a {@code BlacklistedURL} object
     */
    public static BlacklistedURL deserialize(String[] values, List<BlacklistType> blacklistTypes) {
        return new BlacklistedURL(values[0], values[1], values[2], blacklistTypes);
    }

    /**
     * Deserializes a {@code BlacklistedURL} object from MySQL result set.
     *
     * @param rs a {@code ResultSet} object
     * @return a {@code BlacklistedURL} object
     * @throws SQLException if a database access error occurs
     */
    public static List<BlacklistedURL> deserialize(ResultSet rs) throws SQLException {
        List<BlacklistedURL> blacklist = new ArrayList<BlacklistedURL>();
        while (rs.next()) {
            BlacklistType type = new BlacklistType(rs.getInt("type_id"), rs.getString("type_identifier"), rs
                    .getString("type_label"));
            blacklist.add(new BlacklistedURL(rs.getString("url"), rs.getString("match_string"), type));
        }
        return blacklist;
    }

    /**
     * Serializes a {@code BlacklistedURL} object to an array of strings to import to MySQL.
     *
     * @return an array of strings
     */
    public String[] serialize() {
        return new String[]{url, matchString, Integer.toString(type.getId())};
    }

    /**
     * Checks if a candidate is blacklisted by this blacklist entry.
     *
     * @param candidate a {@code ScoredCandidate} object.
     * @return {@code true} if the {@code ScoredCandidate} is blacklisted by this {@code BlacklistedURL}; {@code false}
     * otherwise
     */
    public boolean matches(Candidate candidate) {

        boolean isMatch = false;

//        if (url.equals(candidate.getTopPrivateDomain())) {
//            switch (type.getIdentifier()) {
//                case "dynamic":
//                    isMatch = !bypassDynamic(candidate);
//                    break;
//                case "contains":
//                    isMatch = !bypassContains(candidate);
//                    break;
//                case "regex":
//                    isMatch = !bypassRegex(candidate);
//                    break;
//                case "exact":
//                    isMatch = !bypassExact(candidate);
//                    break;
//                case "noexception":
//                    isMatch = true;
//                    break;
//                default:
//                    isMatch = false;
//            }
//        }

        return isMatch;

    }

//    /**
//     * Determines whether a candidate can bypass this blacklisted URL base on the exact match algorithm.
//     *
//     * @param candidate a {@code ScoredCandidate} object
//     * @return {@code true} if the candidate can bypass the blacklist entry; {@code false} otherwise
//     */
//    private boolean bypassExact(Candidate candidate) {
//        return matchString.equals(candidate.getSearchText());
//    }
//
//    /**
//     * Determines whether a candidate can bypass this blacklisted URL base on the regex match algorithm.
//     *
//     * @param candidate a {@code ScoredCandidate} object
//     * @return {@code true} if the candidate can bypass the blacklist entry; {@code false} otherwise
//     */
//    private boolean bypassRegex(Candidate candidate) {
//        Pattern pattern = Pattern.compile(matchString);
//        Matcher matches = pattern.matcher(candidate.getSearchText());
//        return matches.find();
//    }
//
//    /**
//     * Determines whether a candidate can bypass this blacklisted URL base on the contains match algorithm.
//     *
//     * @param candidate a {@code ScoredCandidate} object
//     * @return {@code true} if the candidate can bypass the blacklist entry; {@code false} otherwise
//     */
//    private boolean bypassContains(Candidate candidate) {
//        return candidate.getSearchText().contains(matchString);
//    }
//
//    /**
//     * Determines whether a candidate can bypass this blacklisted URL base on the dynamic match algorithm.
//     *
//     * @param candidate a {@code ScoredCandidate} object
//     * @return {@code true} if the candidate can bypass the blacklist entry; {@code false} otherwise
//     */
//    private boolean bypassDynamic(Candidate candidate) {
//
//        boolean isAllowed = true;
//        final int unmatchedCharacterCount = candidate.getCharacterCount() - candidate.getCharacterMatchCount();
//
//        if (candidate.getCharacterCount() > 9 && unmatchedCharacterCount > 3) {
//            isAllowed = false;
//        } else if (candidate.getCharacterCount() <= 9 && unmatchedCharacterCount > 2) {
//            isAllowed = false;
//        } else if (candidate.getCharacterCount() <= 6 && unmatchedCharacterCount > 1) {
//            isAllowed = false;
//        }
//
//        return isAllowed;
//
//    }

}
