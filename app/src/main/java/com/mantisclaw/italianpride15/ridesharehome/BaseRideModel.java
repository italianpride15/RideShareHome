package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class BaseRideModel implements Comparable<BaseRideModel> {

    public String serviceName;

    //API variables
    public String client_id;
    public String requestURL;
    public String deepLinkAppName;
    public String deepLinkQuery;

    //Resource ids
    public String drawableImageResource;

    //Cost variables
    public String estimatedCost;
    public String surgeRate;

    public boolean isInstalled = false;

    public int compareTo(BaseRideModel rideModel) {
        try { //TODO: remove me
            if (this.estimatedCost == null && rideModel.estimatedCost == null) {
                return 0;
            } else if (rideModel.estimatedCost == null && this.estimatedCost != null) {
                return 0 - Integer.getInteger(this.estimatedCost);
            } else if (this.estimatedCost == null && rideModel.estimatedCost != null) {
                return 0 - Integer.getInteger(rideModel.estimatedCost);
            } else {
                Double compareEstimate = Double.parseDouble(rideModel.estimatedCost);
                Double estimate = Double.parseDouble(this.estimatedCost);
                return estimate.intValue() - compareEstimate.intValue();
            }
        } catch (Exception e) {
            if (this.estimatedCost == null) {
                this.estimatedCost = "n/a";
            }
            if (rideModel.estimatedCost == null) {
                rideModel.estimatedCost = "n/a";
            }
            return 0;
        }

    }
}
