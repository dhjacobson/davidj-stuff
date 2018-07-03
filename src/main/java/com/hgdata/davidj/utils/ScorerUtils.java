/*
 * Author: Alex Flury
 * Date: 09/18/2015
 * Copyright HG Data 2015
 * www.hgdata.com
 */

package com.hgdata.davidj.utils;

import java.util.*;

/**
 * Static utility functions for the scorer step.
 */
public class ScorerUtils {

    private static final String[] COMMON_WORDS = {"the", "and", "for", "not", "you", "inc"};
    private static Set<String> commonWords = new HashSet<>(Arrays.asList(COMMON_WORDS));
    private static final String[] URL_CONTAINS_BLACKLIST = {"sucks"};

    /**
     * Defining a private constructor hides the implicit public one.
     */
    private ScorerUtils() {

    }

    /**
     * Finds the number of times the candidate appears in the top n search results.
     *
     * @param n a positive integer
     * @param ranks a list of all the ranks where the candidate appears in the search results
     * @return the number of times the candidate appears in results with rank less than or equal to n
     */
    public static int getFrequencyInTopN(int n, int[] ranks) {
        int frequency = 0;
        for (int rank : ranks) {
            if (rank <= n) {
                frequency++;
            } else {
                break;
            }
        }
        return frequency;
    }

    /**
     * Gets the set of words in a company search text, including acronyms.
     *
     * @param searchText a string
     * @return the set of words and acronyms in the given search text
     */
    public static Set<String> getWordSet(String searchText) {

        // Remove special punctuation, and split the company name at
        // non-alphanumeric characters.

        String[] wordArray = searchText.split("[^a-zA-Z0-9]");
        Set<String> words = new HashSet<>(Arrays.asList(wordArray));

        // Remove common words.

        if (words.size() > 3) {
            for (String word : new HashSet<>(words)) {
                if (word.length() < 3 || commonWords.contains(word)) {
                    words.remove(word);
                }
            }
        }

        words.addAll(getAcronymSet(searchText));

        return words;

    }

    /**
     * Gets the set of acronyms for the words in the company search text.
     *
     * @param searchText a string
     * @return a set of possible acronyms for the words in the given search text
     */
    public static Set<String> getAcronymSet(String searchText) {

        // Remove special punctuation, and split the company name at
        // non-alphanumeric characters.

        String[] wordArray = searchText.split("[^a-zA-Z0-9]");
        List<String> words = Arrays.asList(wordArray);
        Set<String> acronyms = new HashSet<>();

        String longAcronym = "";
        String shortAcronym = "";

        for (String word : words) {
            if (word.length() > 0) {

                // The long acronym contains the first letter of every word.

                longAcronym += word.charAt(0);

                // The short acronym contains the first letter of every word
                // that is longer than 3 characters.

                if (word.length() > 3) {
                    shortAcronym += word.charAt(0);
                }
            }
        }

        // Add the acronyms to the set of words if they are longer than 2
        // characters.

        if (longAcronym.length() >= 2) {
            acronyms.add(longAcronym);
        }

        if (shortAcronym.length() >= 2) {
            acronyms.add(shortAcronym);
        }

        return acronyms;

    }

    /**
     * Counts the number of words in the word set that are found in the URL.
     *
     * @param url   a string
     * @param words a set of strings
     * @return the number of words in the given set that are contained in the given URL
     */
    public static int getWordMatchCount(String url, Set<String> words) {

        // Split the URL at the dots.

        String[] urlParts = url.split("\\.");

        // Loop through the parts of the URL, excluding the last part.

        int matchesCount = 0;

        for (String word : words) {

            for (int p = 0; p < urlParts.length - 1; p++) {

                // Count how many words from the company name match this part of
                // the URL.

                if (urlParts[p].contains(word)) {
                    matchesCount++;
                    break;
                }

            }

        }

        return matchesCount;
    }

    /**
     * Counts the number of characters in a URL excluding the public domain.
     *
     * @param url          a string
     * @param publicDomain a string
     * @return the number of characters in the part of the URL that is outside of the public domain
     */
    public static int getCharacterCount(String url, String publicDomain) {
        return url.replace(".", "").length() - publicDomain.replace(".", "").length();
    }

    /**
     * Counts the number of characters in a URL that are part of a specified set of words.
     *
     * @param url          a string
     * @param publicDomain the public domain of the URL
     * @param words        a set of strings
     * @return the number of characters in the URL that are part of one of the words in the set
     */
    public static int getCharacterMatchCount(String url, String publicDomain, Set<String> words) {

        int characterMatchCount = 0;
        String[] urlParts = url.split("\\.");
        String[] publicDomainParts = publicDomain.split("\\.");

        for (int p = 0; p < urlParts.length - publicDomainParts.length; p++) {

            // Loop through the characters in the top private part of the URL.

            int s = 0;
            while (s < urlParts[p].length()) {

                // Find the longest word from the company name that starts at
                // this character.

                int longestMatchStartingHere = findLongestWordStartingAt(words, urlParts[p], s);

                // Add the length of the matched word to the number of matched
                // characters.

                characterMatchCount += longestMatchStartingHere;

                // Skip over the characters in the matched word.

                if (longestMatchStartingHere >= 1) {
                    s += longestMatchStartingHere;
                } else {
                    s++;
                }

            }

        }

        return characterMatchCount;
    }

    /**
     * Checks whether the given text contains any blacklisted words.
     *
     * @param text a string
     * @return {@code true} if the URL contains any blacklist words; {@code false} otherwise.
     */
    public static boolean containsBlacklistWords(String text) {
        for (String word : URL_CONTAINS_BLACKLIST) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the length of the longest word starting at a certain character in a string.
     *
     * @param words  a set of all possible words to look for
     * @param string look for the word inside of this string
     * @param index  look for the word starting at this character
     * @return the length of the longest word in the given set that starts at the given index in the given string
     */
    public static int findLongestWordStartingAt(Set<String> words, String string, int index) {

        int longest = 0;

        for (String word : words) {

            if (
                    word.length() > longest &&
                            index + word.length() <= string.length() &&
                            string.substring(index, index + word.length()).equals(word)
                    ) {

                longest = word.length();

            }

        }

        return longest;

    }

//    /**
//     * Filters a list of {@code Candidate.ExternalURL} objects to those that match a given string at the state level.
//     *
//     * @param externalUrls the list of {@code Candidate.ExternalURL} objects to filter
//     * @param state        the desired state
//     * @return the list of {@code Candidate.ExternalURL} objects in the given list whose state matches the given state
//     */
//    public static List<Candidate.ExternalURL> filterStateLevelExternalUrls(
//            List<Candidate.ExternalURL> externalUrls, String state) {
//
//        List<Candidate.ExternalURL> stateLevelExternalUrls = new ArrayList();
//
//        for (Candidate.ExternalURL externalUrl : externalUrls) {
//            if (state.equals(externalUrl.getState())) {
//                stateLevelExternalUrls.add(externalUrl);
//            }
//        }
//
//        return stateLevelExternalUrls;
//
//    }
//
//    /**
//     * Filters a list of {@code Candidate.ExternalURL} objects to those that match a given source ID.
//     *
//     * @param externalUrls the list of {@code Candidate.ExternalURL} objects to filter
//     * @param source       the desired source ID
//     * @return the list of {@code Candidate.ExternalURL} objects in the given list whose source ID matches the given
//     * source ID
//     */
//    public static List<Candidate.ExternalURL> filterExternalUrlsBySource(
//            List<Candidate.ExternalURL> externalUrls, int source) {
//
//        List<Candidate.ExternalURL> filteredExternalUrls = new ArrayList();
//
//        for (Candidate.ExternalURL externalUrl : externalUrls) {
//            if (source == externalUrl.getSource()) {
//                filteredExternalUrls.add(externalUrl);
//            }
//        }
//
//        return filteredExternalUrls;
//
//    }
//
//    /**
//     * Determines whether a given list of {@code Candidate.ExternalURL} objects contains external URL's from mulitple
//     * sources.
//     *
//     * @param externalUrls a list of {@code Candidate.ExternalURL} objects
//     * @return true if there exist two {@code Candidate.ExternalURL} objects in the specified list with differing
//     * source ID's
//     */
//    public static boolean isMultipleSources(List<Candidate.ExternalURL> externalUrls) {
//
//        boolean isMultipleSources = false;
//
//        if (!externalUrls.isEmpty()) {
//
//            int firstSource = externalUrls.get(0).getSource();
//
//            for (Candidate.ExternalURL externalUrl : externalUrls) {
//                if (externalUrl.getSource() != firstSource) {
//                    isMultipleSources = true;
//                    break;
//                }
//            }
//
//        }
//
//        return isMultipleSources;
//
//    }
//
//    /**
//     * Determines the most recent verified date for a given list of {@code Candidate.ExternalURL} objects.
//     *
//     * @param externalUrls a list of {@code Candidate.ExternalURL} objects
//     * @return the most recent verified date from the specified list of {@code Candidate.ExternalURL} objects
//     */
//    public static Date getMaxVerifiedDate(List<Candidate.ExternalURL> externalUrls) {
//
//        Date maxVerifiedDate = null;
//
//        if (!externalUrls.isEmpty()) {
//
//            maxVerifiedDate = externalUrls.get(0).getVerifiedDate();
//
//            for (Candidate.ExternalURL externalUrl : externalUrls) {
//                if (externalUrl.getVerifiedDate().getTime() > maxVerifiedDate.getTime()) {
//                    maxVerifiedDate = externalUrl.getVerifiedDate();
//                }
//            }
//
//        }
//
//        return maxVerifiedDate;
//
//    }

}
