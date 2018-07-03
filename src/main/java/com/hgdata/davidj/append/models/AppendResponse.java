package com.hgdata.davidj.append.models;

import com.amazonaws.Response;
import com.hgdata.davidj.append.Candidate;

public class AppendResponse {

    private AppendRequest request;
    private Candidate winningCandidate;
    private double winningScore;
    private String url;
    private ResponseStatus status;

    public AppendRequest getRequest() {
        return request;
    }

    public void setRequest(AppendRequest request) {
        this.request = request;
    }

    public Candidate getWinningCandidate() {
        return winningCandidate;
    }

    public void setWinningCandidate(Candidate winningCandidate) {
        this.winningCandidate = winningCandidate;
    }

    public double getWinningScore() {
        return winningScore;
    }

    public void setWinningScore(double winningScore) {
        this.winningScore = winningScore;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        switch (status) {
            case SUCCESS:
                return String.format("%s gets the following URL: `%s`, with score %s.", request.toString(), getUrl(), Double.toString(winningScore));
            case FAILURE:
                return String.format("Could not append a URL for %s.", request.toString());
            default:
                return "Unknown status.";
        }
    }
}
