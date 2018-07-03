package com.hgdata.davidj.models;

import com.hgdata.commons.models.Firmographics;

/**
 * Created by david on 1/3/18.
 */
public class HgUrl {

    private String url;
    private Firmographics firmographics;
    private int productCount;
    private int coreProductCount;
    private int digsigProductCount;
    private boolean lowValue;
    private String finalUrl;
    private Boolean isValid;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Firmographics getFirmographics() {
        return firmographics;
    }

    public void setFirmographics(Firmographics firmographics) {
        this.firmographics = firmographics;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getCoreProductCount() {
        return coreProductCount;
    }

    public void setCoreProductCount(int coreProductCount) {
        this.coreProductCount = coreProductCount;
    }

    public int getDigsigProductCount() {
        return digsigProductCount;
    }

    public void setDigsigProductCount(int digsigProductCount) {
        this.digsigProductCount = digsigProductCount;
    }

    public boolean isLowValue() {
        return lowValue;
    }

    public void setLowValue(boolean lowValue) {
        this.lowValue = lowValue;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    public Boolean isValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }
}
