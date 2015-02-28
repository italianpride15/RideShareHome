package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class BaseRideModel implements Comparable<BaseRideModel> {

    public String client_id;
    public String requestURL;
    public String deepLinkAppName;
    public String deepLinkQuery;

    public String estimatedCost;
    public String surgeRate;

    public int compareTo(BaseRideModel rideModel) {
        Double compareEstimate = Double.parseDouble(rideModel.estimatedCost);
        Double estimate = Double.parseDouble(this.estimatedCost);
        return estimate.intValue() - compareEstimate.intValue();
    }
}
