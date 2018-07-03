package com.hgdata.davidj.db;

import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import com.google.common.base.Joiner;
import com.hgdata.commons.models.Firmographics;
import com.hgdata.davidj.append.models.AppendRequest;
import com.hgdata.davidj.append.models.SearchResult;
import com.hgdata.davidj.models.HgUrl;
import com.hgdata.davidj.settings.AppConfig;
import com.hgdata.davidj.utils.Utils;
import com.hgdata.mysql.connection.DbClient;
import com.hgdata.url.utils.URLUtils;
import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Created by david on 10/11/17.
 */

public class AthenaDb {
    private final static Logger LOGGER = LogManager.getLogger(AthenaDb.class);
    private AppConfig config = ConfigCache.getOrCreate(AppConfig.class);
    private Connection connection;
    private DbClient dbClient;
    public static final String DATABASE = "`integration`";

    private final static String POPULATE_IM_READY_CIDS = "populate_im_ready_cids";

    public AthenaDb(Connection connection) {
        this.connection = connection;
        this.dbClient = new DbClient(connection);
    }

    public AthenaDb(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public AthenaDb() {
        String schema = config.athenaMrdSchema();
        String region = config.athenaRegion();

        final Properties info = new Properties();
        info.put("s3_staging_dir", "s3://aws-athena-query-results-641530021294-us-west-2/davidj/");
        info.put("aws_credentials_provider_class", "com.amazonaws.auth.profile.ProfileCredentialsProvider");
//        info.put("aws_credentials_provider_arguments", awsCreds);

        try {
            LOGGER.trace("Connecting to Athena...");
            this.connection = DriverManager.getConnection(
                    String.format(
                            "jdbc:awsathena://athena.%s.amazonaws.com:443/",
                            region
                    ),
                    info
            );
            connection.setSchema(schema);
            this.dbClient = new DbClient(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet fetch(String sql) {
        ResultSet rs = null;

        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }


    public Statement getPreparedStatement(String sql) throws SQLException {
        return this.dbClient.getPreparedStatement(sql);
    }

    public HgUrl fetchHgUrl(String url) throws SQLException {
        ResultSet rs = this.fetch(String.format("SELECT * FROM %s.farnsworth_urls u LEFT JOIN %s.urls_classified uc ON u.url = uc.url WHERE u.url = '%s' LIMIT 1;",
                config.athenaMrdSchema(), config.athenaDataReleaseSchema(), url));
        rs.next();

        return null;//getHgUrlFromResultSet(rs);
    }

    public Map<String, HgUrl> fetchHgUrls(List<String> urls) throws SQLException {
        Joiner joiner = Joiner.on("', '");

        String sql = String.format("SELECT u.url, u.c, u.hq_address, u.hq_city, u.hq_state, u.hq_country, u.hq_zip, u.revenue_range_id, u.employees_range_id, u.industry_id, u.ticker, uc.end_url, uc.binary, u.product_hits, u.core_product_hits, u.digsig_product_hits FROM %s.farnsworth_urls u LEFT JOIN %s.urls_classified uc ON u.url = uc.url WHERE u.url IN ('%s');",
                config.athenaMrdSchema(), config.athenaDataReleaseSchema(), joiner.join(urls));
        ResultSet rs = this.fetch(sql);
        Map<String, HgUrl> toReturn = new HashMap();
        while (rs.next()) {
            Firmographics firmo = new Firmographics();
            firmo.setCompanyName(Utils.checkNull(rs.getString("c")));
            firmo.setAddressLines(Arrays.asList(Utils.checkNull(rs.getString("hq_address"))));
            firmo.setCity(Utils.checkNull(rs.getString("hq_city")));
            firmo.setState(Utils.checkNull(rs.getString("hq_state")));
            firmo.setCountry(Utils.checkNull(rs.getString("hq_country")));
            firmo.setZip(Utils.checkNull(rs.getString("hq_zip")));
            firmo.setRevenueRangeID(rs.getInt("revenue_range_id"));
            firmo.setEmployeeRangeID(rs.getInt("employees_range_id"));
            firmo.setIndustryIDs(Arrays.asList(rs.getInt("industry_id")));
            firmo.setTicker(Utils.checkNull(rs.getString("ticker")));

            HgUrl hgUrl = new HgUrl();
            hgUrl.setUrl(rs.getString("url"));
            String rawFinalUrl = Utils.checkNull(rs.getString("end_url"));
            try {
                hgUrl.setFinalUrl(URLUtils.cleanURL(rawFinalUrl));
            } catch (MalformedURLException e) {
                hgUrl.setFinalUrl("UNPARSABLE");
            }
            hgUrl.setValid(rs.getBoolean("binary"));
            hgUrl.setFirmographics(firmo);
            hgUrl.setProductCount(rs.getInt("product_hits"));
            hgUrl.setCoreProductCount(rs.getInt("core_product_hits"));
            hgUrl.setDigsigProductCount(rs.getInt("digsig_product_hits"));
            toReturn.put(hgUrl.getUrl(), hgUrl);
        }

        return toReturn;
    }



//    private HgUrl getHgUrlFromResultSet(ResultSet rs) throws SQLException {
//        Firmographics firmo = new Firmographics();
//        firmo.setCompanyName(Utils.checkNull(rs.getString("c")));
//        firmo.setAddressLines(Arrays.asList(Utils.checkNull(rs.getString("hq_address"))));
//        firmo.setCity(Utils.checkNull(rs.getString("hq_city")));
//        firmo.setState(Utils.checkNull(rs.getString("hq_state")));
//        firmo.setCountry(Utils.checkNull(rs.getString("hq_country")));
//        firmo.setZip(Utils.checkNull(rs.getString("hq_zip")));
//        firmo.setRevenueRangeID(rs.getInt("revenue_range_id"));
//        firmo.setEmployeeRangeID(rs.getInt("employees_range_id"));
//        firmo.setIndustryIDs(Arrays.asList(rs.getInt("industry_id")));
//        firmo.setTicker(Utils.checkNull(rs.getString("ticker")));
//
//        HgUrl hgUrl = new HgUrl();
//        hgUrl.setUrl(rs.getString("url"));
//        String rawFinalUrl = Utils.checkNull(rs.getString("end_url"));
//        try {
//            hgUrl.setFinalUrl(URLUtils.cleanURL(rawFinalUrl));
//        } catch (MalformedURLException e) {
//            hgUrl.setFinalUrl("UNPARSABLE");
//        }
//        hgUrl.setValid(rs.getBoolean("binary"));
//        hgUrl.setFirmographics(firmo);
//        hgUrl.setProductCount(rs.getInt("product_hits"));
//        hgUrl.setCoreProductCount(rs.getInt("core_product_hits"));
//        hgUrl.setDigsigProductCount(rs.getInt("digsig_product_hits"));
//
//        return hgUrl;
//    }

//    public  Map<AppendRequest, List<SearchResult>> fetchSearchResults(List<AppendRequest> requests) {
//        Joiner joiner = Joiner.on("', '");
//
//        String sql = String.format("SELECT u.url, u.c, u.hq_address, u.hq_city, u.hq_state, u.hq_country, u.hq_zip, u.revenue_range_id, u.employees_range_id, u.industry_id, u.ticker, uc.end_url, uc.binary, u.product_hits, u.core_product_hits, u.digsig_product_hits FROM %s.farnsworth_urls u LEFT JOIN %s.urls_classified uc ON u.url = uc.url WHERE u.url IN ('%s');",
//                config.athenaMrdSchema(), config.athenaDataReleaseSchema(), joiner.join(urls));
//        ResultSet rs = this.fetch(sql);
//        Map<String, HgUrl> toReturn = new HashMap();
//
//    }




}
