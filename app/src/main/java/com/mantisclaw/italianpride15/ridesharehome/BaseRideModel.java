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
            //Prioritize app installed over cost, exclude taxi. Taxi has neutral priority and is based only by cost
            if (this.isInstalled && !rideModel.isInstalled && !(this.getClass() == TaxiRideModel.class || rideModel.getClass() == TaxiRideModel.class)) {
                //both models not taxi and this is installed but rideModel isn't, return this
                return 0 - Integer.getInteger(this.estimatedCost);

            } else if (rideModel.isInstalled && !this.isInstalled && !(this.getClass() == TaxiRideModel.class || rideModel.getClass() == TaxiRideModel.class)) {
                //both models not taxi and rideModel is installed but this isn't, return rideModel
                return 0 - Integer.getInteger(rideModel.estimatedCost);

            } else if (this.estimatedCost == null && rideModel.estimatedCost == null) {
                //cannot do accurate comparison
                return 0;

            } else if (rideModel.estimatedCost == null && this.estimatedCost != null) {
                //only this is valid
                return 0 - Integer.getInteger(this.estimatedCost);

            } else if (this.estimatedCost == null && rideModel.estimatedCost != null) {
                //only rideModel is valid
                return 0 - Integer.getInteger(rideModel.estimatedCost);

            } else {
                //both are valid, do cost comparison
                Double compareEstimate = Double.parseDouble(rideModel.estimatedCost);
                Double estimate = Double.parseDouble(this.estimatedCost);
                return Double.compare(estimate, compareEstimate);

                //Note: Corner case appears if Taxi cost is greater than app that's not installed
                //but still less than other apps that are installed.
            }
        } catch (Exception e) {
            //reset estimatedCost to n/a
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
