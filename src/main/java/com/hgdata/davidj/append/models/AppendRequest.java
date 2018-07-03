package com.hgdata.davidj.append.models;

import com.hgdata.davidj.utils.URLAppenderUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by david on 2/1/18.
 */
public class AppendRequest {

    private String company;
    private String city;
    private String state;
    private String country;

    public AppendRequest(String company, String country) {
        this.company = company;
        this.country = country;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSearchText() {
        return URLAppenderUtils.computeCompanySearchText(this.getCompany());
    }

    public String getLocation() {
        return Stream.of(city, state, country).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    public String toString() {
        return country == null ? String.format("`%s`", company) : String.format("`%s` in `%s`", company, getLocation());
    }

}
