/*
 * Author: Alex Flury
 * Date: 5/22/2015
 * Copyright (c) 2015 HG Data, Inc.
 * www.hgdata.com
 */

package com.hgdata.davidj.utils;

import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.hgdata.aws.s3.downloader.Downloader;
import com.hgdata.aws.s3.uploader.Uploader;
import com.hgdata.compression.CompressionUtils;
import com.hgdata.davidj.models.FatalException;
import com.hgdata.urldownloader.BasicURLDownloader;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility functions for the URL Appender.
 */
public class URLAppenderUtils {

    private static final DateFormat SQL_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd");

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private static final Joiner FILE_PATH_JOINER = Joiner.on('/');

    /**
     * Defining a private constructor hides the implicit public one.
     */
    private URLAppenderUtils() {

    }

    /**
     * Creates and configures a URL downloader object.
     *
     * @param urlConnectTimeout the amount of time in milliseconds to wait for a connection
     * @param urlReadTimeout    the amount of time in milliseconds to wait to read a response from a URL
     * @return a {@code BasicURLDownloader} object
     */
    public static BasicURLDownloader createURLDownloader(int urlConnectTimeout, int urlReadTimeout) {

        BasicURLDownloader urlDownloader = new BasicURLDownloader();

        // Configure the URLDownloader object.
        urlDownloader.setConnectTimeout(urlConnectTimeout);
        urlDownloader.setReadTimeout(urlReadTimeout);

        return urlDownloader;

    }

    /**
     * Converts a string to valid UTF-8 by removing invalid byte sequences.
     *
     * @param str a string
     * @return a valid UTF-8 string
     */
    public static String validUtf8(String str) {
        return str.replaceAll("[^\\u0000-\\uFFFF]", "?");
    }

    /**
     * Formats a date in the form that MySQL will recognize.
     *
     * @param date a {@code Date} object
     * @return a string
     */
    public static String formatSqlDate(Date date) {
        return SQL_DATETIME_FORMAT.format(date);
    }

    /**
     * Formats a date in the form that MySQL will recognize.
     *
     * @param date        a {@code Date} object
     * @param includeTime whether or not to include the time part of the date
     * @return a string
     */
    public static String formatSqlDate(Date date, boolean includeTime) {
        if (includeTime)
            return formatSqlDate(date);
        else
            return SQL_DATE_FORMAT.format(date);
    }

    /**
     * Truncates a string to a given maximum length.
     *
     * @param string    a string
     * @param maxLength an integer
     * @return the first {@code maxLength} characters of {@code string} if {@code string} is longer than {@code
     * maxLength}; otherwise, {@code string}
     */
    public static String truncateString(String string, int maxLength) {
        return string != null && string.length() > maxLength ? string.substring(0, maxLength) : string;
    }

    /**
     * Converts a comma separated list of integers to an {@code int} array.
     *
     * @param intListString a comma separated list of integers
     * @return an {@code int} array
     */
    public static synchronized int[] parseIntList(String intListString) {

        int[] ints;

        if (intListString == null) {
            ints = new int[0];
        } else {
            String[] intStrings = StringUtils.splitPreserveAllTokens(intListString, ',');
            ints = new int[intStrings.length];
            for (int r = 0; r < intStrings.length; r++) {
                ints[r] = Integer.parseInt(intStrings[r]);
            }
        }

        return ints;

    }

    /**
     * Converts an {@code int} array to a comma separated list.
     *
     * @param ints an {@code int} array
     * @return a comma separated list of integers
     */
    public static String printIntList(int[] ints) {

        String listString = "";

        if (ints.length > 0) {
            List<Integer> list = Ints.asList(ints);
            listString = COMMA_JOINER.join(list);
        }

        return listString;

    }

    /**
     * Converts a comma separated list of dates to an array of {@code Date} objects.
     *
     * @param dateListString a comma separated list of dates
     * @param includeTime    whether or not the time is included in the dates
     * @return an array of {@code Date} objects
     * @throws ParseException if a parsing exception occurs
     */
    public static synchronized Date[] parseDateList(String dateListString, boolean includeTime) throws ParseException {

        Date[] dates;

        if (includeTime) {
            dates = parseDateList(dateListString, SQL_DATETIME_FORMAT);
        } else {
            dates = parseDateList(dateListString, SQL_DATE_FORMAT);
        }

        return dates;

    }

    /**
     * Converts a comma separated list of dates to an array of {@code Date} objects.
     *
     * @param dateListString a comma separated list of dates
     * @param dateFormatter  a {@code DateFormat} object
     * @return an array of {@code Date} objects
     * @throws ParseException if a parsing exception occurs
     */
    public static Date[] parseDateList(String dateListString, DateFormat dateFormatter) throws ParseException {

        Date[] dates;

        if (dateListString == null) {
            dates = new Date[0];
        } else {
            String[] dateStrings = StringUtils.splitPreserveAllTokens(dateListString, ',');
            dates = new Date[dateStrings.length];
            for (int d = 0; d < dateStrings.length; d++) {
                dates[d] = dateFormatter.parse(dateStrings[d]);
            }
        }

        return dates;
    }

    /**
     * Parses an integer that's formatted as a human readable string.
     *
     * @param text the string containing the integer
     * @return an integer
     */
    public static synchronized int parseFormattedInt(String text) {

        int num = 0;

        String cleanText = text.replace(",", "");

        if (!cleanText.isEmpty()) {
            num = Integer.parseInt(cleanText);
        }

        return num;

    }

    /**
     * Parses a floating point that's formatted as a human readable string.
     *
     * @param text the string containing the floating point
     * @return a double
     */
    public static synchronized double parseFormattedDouble(String text) {

        double num = 0.0;

        String cleanText = text.replace(",", "").replace("%", "");

        if (!cleanText.isEmpty()) {
            num = Double.parseDouble(cleanText);
        }

        return num;

    }

    /**
     * Applies the bzip2 compression algorithm to a byte array.
     *
     * @param uncompressed the uncompressed bytes
     * @return the compressed bytes
     * @throws IOException if a compression error occurs
     */
    public static byte[] bzip2Compress(byte[] uncompressed) throws IOException {

        ByteArrayInputStream in = new ByteArrayInputStream(uncompressed);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream bzOut = new BZip2CompressorOutputStream(out);

        IOUtils.copy(in, bzOut);
        bzOut.flush();

        in.close();
        out.close();
        bzOut.close();

        return out.toByteArray();

    }

    /**
     * Gets the host name of the server where the JAR is currently running.
     *
     * @return a string
     * @throws IOException if an I/O error occurs
     */
    public static String getHostName() throws IOException {
        Process p = Runtime.getRuntime().exec("hostname");
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return reader.readLine();
    }

    /**
     * Compresses a list of files and uploads them to S3.
     *
     * @param files     the source files
     * @param uploader  the S3 uploader
     * @param bucket    the destination S3 bucket
     * @param baseKey   the destination S3 base key
     * @param filenames the destination file names
     * @throws FatalException if a fatal error occurs
     */
    public static void compressAndUpload(
            File[] files,
            Uploader uploader,
            String bucket,
            String baseKey,
            String[] filenames)
            throws FatalException {

        File[] compressedFiles = new File[files.length];
        CompressorThread[] compressors = new CompressorThread[files.length];
        Upload[] uploads = new Upload[files.length];

        // Start the compressor threads.

        for (int f = 0; f < files.length; f++) {
            compressedFiles[f] = getCompressedFile(files[f]);
            compressors[f] = new CompressorThread(files[f], compressedFiles[f]);
            compressors[f].start();
        }

        // Wait for the compressor threads to finish.

        waitForCompressorThreads(compressors);

        // Start the uploads.

        for (int f = 0; f < files.length; f++) {
            uploads[f] = uploader.uploadFileAsync(
                    bucket, String.format("%s/%s", baseKey, filenames[f]), compressedFiles[f]);
        }

        // Wait for the uploads to finish.

        waitForUploads(uploads);

    }

    /**
     * Waits for a list of compressor threads to complete.
     *
     * @param threads an array of {@code CompressorThread} objects
     * @throws FatalException if a fatal error occurs
     */
    public static void waitForCompressorThreads(CompressorThread[] threads) throws FatalException {

        for (int t = 0; t < threads.length; t++) {
            try {

                threads[t].join();

                if (threads[t].getException() != null) {
                    throw threads[t].getException();
                }

            } catch (InterruptedException | IOException e) {
                throw new FatalException("An error occurred while compressing a file.", e);
            }
        }

    }

    /**
     * Waits for a list of S3 uploads to finish.
     *
     * @param uploads an array of {@code Upload} objects
     * @throws FatalException if a fatal error occurs
     */
    public static void waitForUploads(Upload[] uploads) throws FatalException {

        for (int u = 0; u < uploads.length; u++) {

            try {
                uploads[u].waitForCompletion();
            } catch (InterruptedException e) {
                throw new FatalException("An error occurred while uploading a file to S3.", e);
            }

        }

    }


    /**
     * Compresses a list of files and uploads them to S3.
     *
     * @param files    the source files
     * @param uploader the S3 uploader
     * @param bucket   the destination S3 bucket
     * @param baseKey  the destination S3 base key
     * @throws FatalException if a fatal error occurs
     */
    public static void compressAndUpload(
            File[] files,
            Uploader uploader,
            String bucket,
            String baseKey)
            throws FatalException {

        String[] filenames = new String[files.length];

        for (int f = 0; f < filenames.length; f++) {
            filenames[f] = getCompressedFile(files[f]).getName();
        }

        compressAndUpload(files, uploader, bucket, baseKey, filenames);

    }

    /**
     * Downloads and uncompresses a list of files from S3.
     *
     * @param files      the destination files
     * @param downloader the S3 downloader
     * @param bucket     the S3 bucket of the source files
     * @param baseKey    the S3 base key of the source files
     * @throws FatalException if a fatal error occurs
     */
    public static void downloadAndUncompress(File[] files, Downloader downloader, String bucket, String baseKey)
            throws FatalException {

        File[] compressedFiles = new File[files.length];
        Download[] downloads = new Download[files.length];
        UncompressorThread[] uncompressors = new UncompressorThread[files.length];

        // Start the download threads.

        for (int f = 0; f < files.length; f++) {
            compressedFiles[f] = getCompressedFile(files[f]);
            downloads[f] = downloader.downloadFileAsync(
                    bucket, String.format("%s/%s", baseKey, compressedFiles[f].getName()), compressedFiles[f]
            );
        }

        waitForDownloads(downloads);

        // Start the compressor threads.

        for (int f = 0; f < files.length; f++) {
            uncompressors[f] = new UncompressorThread(compressedFiles[f], files[f]);
            uncompressors[f].start();
        }

        waitForUncompressorThreads(uncompressors);

    }

    /**
     * Waits for a list of S3 downloads to complete.
     *
     * @param downloads an array of {@code Download} objects
     * @throws FatalException if a fatal error occurs
     */
    public static void waitForDownloads(Download[] downloads) throws FatalException {

        for (int d = 0; d < downloads.length; d++) {
            try {
                downloads[d].waitForCompletion();
            } catch (InterruptedException e) {
                throw new FatalException("An error occurred while downloading a file from S3.", e);
            }
        }

    }

    /**
     * Watis for a list of uncompressor threads to finish
     *
     * @param threads an array of {@code CompressorThread} objects
     * @throws FatalException if a fatal error occurs
     */
    public static void waitForUncompressorThreads(UncompressorThread[] threads) throws FatalException {

        for (int t = 0; t < threads.length; t++) {

            try {

                threads[t].join();

                if (threads[t].getException() != null) {
                    throw threads[t].getException();
                }

            } catch (InterruptedException | IOException e) {
                throw new FatalException("An error occurred while uncompressing a file.", e);
            }

        }

    }

    /**
     * Gets a reference to the compressed version of a given file.
     *
     * @param file the uncompressed file
     * @return the compressed file
     */
    public static File getCompressedFile(File file) {
        return new File(String.format("%s.bz2", file.getAbsolutePath()));
    }

    /**
     * Closes a {@code Uploader} object.
     *
     * @param uploader the {@code Uploader} object to close.
     * @throws FatalException if a fatal error occurs
     */
    public static void closeUploader(Uploader uploader) throws FatalException {
        try {
            uploader.close();
        } catch (Exception e) {
            throw new FatalException("An error occurred while closing the S3 uploader.", e);
        }
    }

    /**
     * Waits a fixed configurable amount of time for the {@code BulkExporter} to complete its execution
     *
     * @param waitTime the amount of time to wait
     * @throws FatalException if a fatal error occurs
     */
    public static void waitForBulkImporterExecute(long waitTime) throws FatalException {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new FatalException("An error occurred while waiting for the database import to complete.", e);
        }
    }

    /**
     * Joins parts of a file path using the file path separator character.
     *
     * @param parts the parts of the file path to join
     * @return the joined file path
     */
    public static String joinFilePath(String[] parts) {
        return FILE_PATH_JOINER.join(parts);
    }

    /**
     * A thread for asynchronously compressing a file.
     */
    static class CompressorThread extends Thread {

        private File source;
        private File dest;
        private IOException exception;

        public CompressorThread(File source, File dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public void run() {
            try {
                CompressionUtils.compress(source, dest);
            } catch (IOException e) {
                this.exception = e;
            }
        }

        public IOException getException() {
            return exception;
        }

    }

    /**
     * A thread for asynchronously uncompressing a file.
     */
    static class UncompressorThread extends Thread {

        private File source;
        private File dest;
        private IOException exception;

        public UncompressorThread(File source, File dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public void run() {
            try {
                CompressionUtils.uncompress(source, dest);
            } catch (IOException e) {
                this.exception = e;
            }
        }

        public IOException getException() {
            return exception;
        }

    }

    /**
     * Converts a company name to a search text parameter.
     *
     * @param companyName the company name
     * @return the search text parameter
     */
    public static String computeCompanySearchText(String companyName) {

        // Remove text inside parentheses or after a hyphen character.

        String searchText = companyName.replaceAll("\\(.*\\)", "").replaceAll(" -+ .*", "");

        // Insert spaces between camelcase words, and remove all punctuation except punctuation marks that separate
        // words.

        searchText = searchText.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("([A-Z])([A-Z][a-z])", "$1 $2")
                .replaceAll("[^a-zA-Z0-9 \\-/.]", "").toLowerCase().trim();

        // Remove spaces between single letters.

        String[] words = searchText.split("[^a-zA-Z0-9]+");
        List<String> newWords = new ArrayList<String>();

        for (int w = 0; w < words.length; w++) {
            if (w > 0 && words[w - 1].length() == 1 && words[w].length() == 1) {
                newWords.set(newWords.size() - 1, newWords.get(newWords.size() - 1) + words[w]);
            } else {
                newWords.add(words[w]);
            }
        }

        searchText = StringUtils.join(newWords, " ");

        // Return the search text.

        return searchText;

    }

    /**
     * Static utility functions for the scorer step.
     */
    public static class ScorerUtils {

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
}
