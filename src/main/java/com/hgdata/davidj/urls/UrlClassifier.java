package com.hgdata.davidj.urls;

import com.hgdata.davidj.db.AthenaDb;
import com.hgdata.davidj.models.HgUrl;

/**
 * Created by david on 12/29/17.
 */
public class UrlClassifier {

    private AthenaDb athena = new AthenaDb();

    public UrlClassifier() {

    }

    public void classify(String url) throws Exception{
        UrlInfo urlInfo = new UrlInfo(url);
        urlInfo.makeRequest();

        HgUrl hgUrl = athena.fetchHgUrl(url);


    }



}
