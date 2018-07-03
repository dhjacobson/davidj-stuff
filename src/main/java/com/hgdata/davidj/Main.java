package com.hgdata.davidj;

import com.google.common.base.Joiner;
import com.hgdata.commons.models.Firmographics;
import com.hgdata.davidj.db.AthenaDb;
import com.hgdata.davidj.models.HgUrl;
import com.hgdata.davidj.urls.UrlClassifier;
import com.hgdata.davidj.urls.UrlInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by david on 12/29/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {

    }



    public static void writeUrlInfo() throws Exception {
        String inputDelimiter = "\\|";
        UrlClassifier classifier = new UrlClassifier();

        String inputFileName = classifier.getClass().getClassLoader().getResource("training-sets/url_dispositions.txt").getFile();

        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output/url_classifier_output.txt"));
        Joiner joiner = Joiner.on("\t");

        writer.write(joiner.join(Arrays.asList("url", "final_url", "is_valid", "disposition", "rank", "links_in_count", "speed", "locale", "adult_content", "whois_server", "days_since_domain_update","product_count", "core_product_count", "c", "country", "revenue_range_id", "employees_range_id", "industry_id")) + "\n");

        List<String> urls = Files.lines(Paths.get(inputFileName)).map(s -> s.split(inputDelimiter)[0]).distinct().collect(Collectors.toList());

        AthenaDb athena = new AthenaDb();
        Map<String, HgUrl> hgUrlMap = athena.fetchHgUrls(urls);

        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(inputDelimiter, -1);
            String url = fields[0];
            String disposition = fields[1];

            HgUrl hgUrl = hgUrlMap.get(url);

            if (hgUrl == null) {
                hgUrl = new HgUrl();
                hgUrl.setUrl(url);
                hgUrl.setFinalUrl(url);
                hgUrl.setFirmographics(new Firmographics());
            }

            Firmographics firmo = hgUrl.getFirmographics();

            System.out.println("Fetching Alexa data for " + hgUrl.getFinalUrl() + "...");
            UrlInfo urlInfo = new UrlInfo(hgUrl.getFinalUrl());
            urlInfo.makeRequest();

            String finalUrl = Objects.toString(hgUrl.getFinalUrl());
            String isValid = Objects.toString(hgUrl.isValid());

            String rank = Objects.toString(urlInfo.getRank(), "");
            String linksInCount = Objects.toString(urlInfo.getLinksInCount(), "");
            String speed = Objects.toString(urlInfo.getSpeed(), "");
            String locale = Objects.toString(urlInfo.getLocale(), "");
            String adultContent = Objects.toString(urlInfo.getAdultContent(), "");

            System.out.println("Fetching WhoIs data for " + hgUrl.getFinalUrl() + "...");
            urlInfo.makeWhoIsRequest();

            String whoIsServer = Objects.toString(urlInfo.getWhoIsServer());
            String daysSinceDomainUpdate = Objects.toString(urlInfo.getDaysSinceDomainUpdate());

            String productCount = Objects.toString(hgUrl.getProductCount(), "0");
            String coreProductCount = Objects.toString(hgUrl.getCoreProductCount(), "0");
            String digsigProductCount = Objects.toString(hgUrl.getDigsigProductCount(), "0");
            String companyName = Objects.toString(firmo.getCompanyName(), "");
            String country = Objects.toString(firmo.getCountry(), "");
            String revenueRangeId = Objects.toString(firmo.getRevenueRangeID(), "");
            String employeeRangeId = Objects.toString(firmo.getEmployeeRangeID(), "");
            String industryIds = Objects.toString(firmo.getIndustryIDs() != null ? firmo.getIndustryIDs().get(0) : null, "");


            String toWrite = joiner.join(Arrays.asList(
                    url, finalUrl, isValid, disposition,
                    rank, linksInCount, speed, locale, adultContent,
                    whoIsServer, daysSinceDomainUpdate,
                    productCount, coreProductCount,
                    companyName, country,
                    revenueRangeId, employeeRangeId,
                    industryIds
            ));
            writer.write(toWrite + "\n");
        }
        writer.close();
    }
}
