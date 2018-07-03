package com.hgdata.davidj.append;

import com.hgdata.davidj.append.models.AppendRequest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarketMap {

    private Map<String, String> stateAndCountryMap = new HashMap<>();
    private Map<String, String> countryMap = new HashMap<>();;

    public static MarketMap build() throws IOException {
        MarketMap marketMap =  new MarketMap();

        BufferedReader br = new BufferedReader(new FileReader("/Users/david/IdeaProjects/davidj-stuff/src/main/resources/url-appender/markets.tsv"));

        String line;
        while((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            String country = fields[0];
            String state = fields[1];
            String market = fields[2];
            if (!market.equals("NULL")) {
                if (state.isEmpty()) {
                    marketMap.countryMap.put(country, market);
                } else {
                    marketMap.stateAndCountryMap.put(combineStateAndCountry(state, country), market);
                }
            }
        }
        return marketMap;
    }

    public String findMarket(AppendRequest request) {
        String state = request.getState();
        String country = request.getCountry();

        if (country == null) {
            return null;
        }

        if (state != null) {
            String stateAndCountry = combineStateAndCountry(state, country);
            if (stateAndCountryMap.containsKey(stateAndCountry)) {
                return stateAndCountryMap.get(stateAndCountry);
            }
        }

        return countryMap.getOrDefault(country, null);
    }

    private static String combineStateAndCountry(String state, String country) {
        return state + ", " + country;
    }

}
