package com.hgdata.davidj.utils;

import com.google.common.base.Function;
import com.hgdata.url.appender.model.CleanedURL;
import com.hgdata.url.appender.utils.URLAppenderFunctions;
import com.hgdata.url.utils.URLUtils;

import java.net.MalformedURLException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by david on 1/3/18.
 */
public class Utils {

    public static String checkNull(String text) {
        if (text == null) return text;
        if (text.length() == 0 || "null".equalsIgnoreCase(text) || "\\N".equalsIgnoreCase(text))
            return null;
        return text.trim();
    }

    /**
     * This holder class uses lazy initialization to create a URL cleaning function.
     */
    private static class CleanedURLsFunctionHolder {
        private static final Function<String, List<CleanedURL>> function =
                URLAppenderFunctions.rawURLToCleanedURLs();

        /**
         * Prevents instantiation.
         */
        private CleanedURLsFunctionHolder() {

        }
    }


    public static CleanedURL getCleanedURL(final String url) throws MalformedURLException {
        checkNotNull(url);
        final String trimmedUrl = url.trim();
        final List<CleanedURL> cleanedURLs = CleanedURLsFunctionHolder.function.apply(trimmedUrl);
        if (cleanedURLs.isEmpty()) {
            throw new MalformedURLException(String.format("Can't clean URL: %s", trimmedUrl));
        }
        return cleanedURLs.get(cleanedURLs.size() - 1);
    }

}
