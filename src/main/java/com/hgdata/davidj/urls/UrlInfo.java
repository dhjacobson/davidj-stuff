package com.hgdata.davidj.urls;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import java.io.IOException;
import java.net.SocketException;
import org.apache.commons.net.whois.WhoisClient;

import org.json.JSONObject;
import org.json.XML;

/**
 * Makes a request to the Alexa Web Information Service UrlInfo action.
 */
public class UrlInfo {

    private static final String ACTION_NAME = "UrlInfo";
    private static final String RESPONSE_GROUP_NAME = "Rank,LinksInCount,SiteData,OwnedDomains,Language,Speed,AdultContent,UsageStats,RankByCountry,Categories";
    private static final String SERVICE_HOST = "awis.amazonaws.com";
    protected static final String SERVICE_ENDPOINT = "awis.us-west-1.amazonaws.com";
    private static final String SERVICE_URI = "/api";
    private static final String SERVICE_REGION = "us-west-1";
    private static final String SERVICE_NAME = "awis";
    private static final String AWS_BASE_URL = "https://" + SERVICE_HOST + SERVICE_URI;
    private static final String HASH_ALGORITHM = "HmacSHA256";
    private static final String DATEFORMAT_AWS = "yyyyMMdd'T'HHmmss'Z'";
    private static final String DATEFORMAT_CREDENTIAL = "yyyyMMdd";
    private static final Pattern WHOIS_UPDATED_AT_PATTERN = Pattern.compile("Updated Date: (\\d{4}-\\d{2}-\\d{2})T");
    private static final Pattern WHOIS_SERVER_PATTERN = Pattern.compile("Registrar WHOIS Server: (\\S+)");
    private static final Pattern RANK_FROM_XML_PATTERN = Pattern.compile("<aws:Rank>(\\d+)</aws:Rank>");
    private static final Pattern LINKS_IN_COUNT_FROM_XML_PATTERN = Pattern.compile("<aws:LinksInCount>(\\d+)</aws:LinksInCount>");

    private String accessKeyId;
    private String secretAccessKey;
    public String amzDate;
    public String dateStamp;

    private String site;

    private String xmlResponse;
    private JSONObject jsonResponse;

    private Integer rank;
    private Integer linksInCount;
    private Boolean adultContent;
    private Integer speed;
    private String locale;

    private String whoIsResult;
    private Integer daysSinceDomainUpdate;
    private String whoIsServer;



    public UrlInfo(String site) {
        AWSCredentials credentials = new ProfileCredentialsProvider("dev").getCredentials();
        this.accessKeyId = credentials.getAWSAccessKeyId();
        this.secretAccessKey = credentials.getAWSSecretKey();
        this.site = site;

        this.assignDates();
    }

    public UrlInfo(String accessKeyId, String secretAccessKey, String site) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.site = site;

        this.assignDates();
    }

    private void assignDates() {
        Date now = new Date();
        SimpleDateFormat formatAWS = new SimpleDateFormat(DATEFORMAT_AWS);
        formatAWS.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.amzDate = formatAWS.format(now);

        SimpleDateFormat formatCredential = new SimpleDateFormat(DATEFORMAT_CREDENTIAL);
        formatCredential.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.dateStamp = formatCredential.format(now);
    }

    String sha256(String textToHash) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] byteOfTextToHash=textToHash.getBytes("UTF-8");
        byte[] hashedByteArray = digest.digest(byteOfTextToHash);
        return bytesToHex(hashedByteArray);
    }

    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(HASH_ALGORITHM);
        mac.init(new SecretKeySpec(key, HASH_ALGORITHM));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * Generates a V4 Signature key for the service/region
     *
     * @param key         Initial secret key
     * @param dateStamp   Date in YYYYMMDD format
     * @param regionName  AWS region for the signature
     * @param serviceName AWS service name
     * @return byte[] signature
     * @throws Exception
     */
    static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    private String getAuthorization() throws Exception {
        String canonicalQuery = "Action=" + "urlInfo" + "&ResponseGroup=" + URLEncoder.encode(RESPONSE_GROUP_NAME, "UTF-8") + "&Url=" + URLEncoder.encode(site, "UTF-8");
        String canonicalHeaders = "host:" + SERVICE_ENDPOINT + "\n" + "x-amz-date:" + amzDate + "\n";
        String signedHeaders = "host;x-amz-date";

        String payloadHash = this.sha256("");
        String canonicalRequest = "GET" + "\n" + SERVICE_URI + "\n" + canonicalQuery + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;

        // Match the algorithm to the hashing algorithm you use, either SHA-1 or
        // SHA-256 (recommended)
        String algorithm = "AWS4-HMAC-SHA256";
        String credentialScope = this.dateStamp + "/" + SERVICE_REGION + "/" + SERVICE_NAME + "/" + "aws4_request";
        String stringToSign = algorithm + '\n' +  this.amzDate + '\n' +  credentialScope + '\n' +  this.sha256(canonicalRequest);

        // Create the signing key
        byte[] signingKey = UrlInfo.getSignatureKey(secretAccessKey, dateStamp, SERVICE_REGION, SERVICE_NAME);

        // Sign the string_to_sign using the signing_key
        String signature = bytesToHex(HmacSHA256(stringToSign, signingKey));

        // Make the Request

        return algorithm + " " + "Credential=" + accessKeyId + "/" + credentialScope + ", " +  "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
    }

    /**
     * Makes a request to the specified Url and return the results as a String
     *
     * @param requestUrl url to make request to
     * @return the XML document as a String
     * @throws IOException
     */
    public static String makeRequest(String requestUrl, String authorization, String amzDate) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/xml");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Amz-Date", amzDate);
        conn.setRequestProperty("Authorization", authorization);

        InputStream in = (conn.getResponseCode() / 100 == 2 ? conn.getInputStream() : conn.getErrorStream());

        // Read the response
        StringBuffer sb = new StringBuffer();
        int c;
        int lastChar = 0;
        while ((c = in.read()) != -1) {
            if (c == '<' && (lastChar == '>'))
                sb.append('\n');
            sb.append((char) c);
            lastChar = c;
        }
        in.close();

        return sb.toString();
    }

    public String getRequestUrl() throws Exception {
        String canonicalQuery = "Action=" + "urlInfo" + "&ResponseGroup=" + URLEncoder.encode(RESPONSE_GROUP_NAME, "UTF-8") + "&Url=" + URLEncoder.encode(this.site, "UTF-8");
        return AWS_BASE_URL + "?" + canonicalQuery;
    }

    public void makeRequest() throws Exception {
        this.xmlResponse = UrlInfo.makeRequest(this.getRequestUrl(), this.getAuthorization(), amzDate);
        this.jsonResponse = XML.toJSONObject(this.getXmlResponse());

//        System.out.println(this.jsonResponse);

        JSONObject urlInfoResult = XML.toJSONObject(this.getXmlResponse())
                .getJSONObject("aws:UrlInfoResponse")
                .getJSONObject("aws:Response")
                .getJSONObject("aws:UrlInfoResult")
                .getJSONObject("aws:Alexa");
        try {this.adultContent = urlInfoResult.getJSONObject("aws:ContentData").getString("aws:AdultContent").equalsIgnoreCase("yes");} catch (JSONException e) {}
        try {this.linksInCount = urlInfoResult.getJSONObject("aws:ContentData").getInt("aws:LinksInCount");} catch (JSONException e) {}
        try {this.speed = urlInfoResult.getJSONObject("aws:ContentData").getJSONObject("aws:Speed").getInt("aws:MedianLoadTime");} catch (JSONException e) {}
        try {this.rank = urlInfoResult.getJSONObject("aws:TrafficData").getInt("aws:Rank");} catch (JSONException e) {}
        try {this.locale = urlInfoResult.getJSONObject("aws:ContentData").getJSONObject("aws:Language").getString("aws:Locale");} catch (JSONException e) {}

//        JSONArray countryTraffic = urlInfoResult.getJSONObject("aws:TrafficData").getJSONObject("aws:RankByCountry").getJSONArray("aws:Country");
//        Comparator<JSONObject> comp = (p1, p2) -> Float.compare(
//                Float.parseFloat(p1.getJSONObject("aws:Contribution").getString("aws:PageViews").replace("%", "")),
//                Float.parseFloat(p2.getJSONObject("aws:Contribution").getString("aws:PageViews").replace("%", ""))
//        )
//        this.primaryContributingCountry = Collections.max(countryTraffic, comp);
    }

    public void makeWhoIsRequest() {
        WhoisClient whois = new WhoisClient();
        try {
            whois.connect(WhoisClient.DEFAULT_HOST);
            this.whoIsResult = whois.query("=" + site);
            whois.disconnect();

            Matcher updatedAtMatcher = WHOIS_UPDATED_AT_PATTERN.matcher(whoIsResult);
            if (updatedAtMatcher.find()) {
                Date updatedAt = new SimpleDateFormat("yyyy-MM-dd").parse(updatedAtMatcher.group(1));
                this.daysSinceDomainUpdate = ((Long)TimeUnit.DAYS.convert(new Date().getTime() - updatedAt.getTime(), TimeUnit.MILLISECONDS)).intValue();
            }

            Matcher serverMatcher = WHOIS_SERVER_PATTERN.matcher(whoIsResult);
            if (serverMatcher.find()) {
                this.whoIsServer = serverMatcher.group(1);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSite() {
        return site;
    }

    public String getXmlResponse() {
        return xmlResponse;
    }

    public JSONObject getJsonResponse() {
        return jsonResponse;
    }

    public Boolean getAdultContent() {
        return adultContent;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getLinksInCount() {
        return linksInCount;
    }

    public String getLocale() {
        return locale;
    }

    public String getWhoIsResult() {
        return whoIsResult;
    }

    public Integer getDaysSinceDomainUpdate() {
        return daysSinceDomainUpdate;
    }

    public String getWhoIsServer() {
        return whoIsServer;
    }

    /**
     * Makes a request to the Alexa Web Information Service UrlInfo action
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: UrlInfo ACCESS_KEY_ID " +
                               "SECRET_ACCESS_KEY site");
            System.exit(-1);
        }

        String site = args[0];
        UrlInfo urlInfo = new UrlInfo(site);

        urlInfo.makeRequest();

        System.out.println(urlInfo.getAdultContent());
        System.out.println(urlInfo.getSpeed());
        System.out.println(urlInfo.getRank());
        System.out.println(urlInfo.getLinksInCount());
        System.out.println(urlInfo.getLocale());

        urlInfo.makeWhoIsRequest();
        System.out.println(urlInfo.getWhoIsResult());
    }
}
