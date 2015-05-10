package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class TaxiRideModel extends BaseRideModel {

    private static final String taxiFareFinder_client_id = "mu3eprAPr2pe";
    private static final String taxiFareFinder_base_url = "https://api.taxifarefinder.com/fare?";
    private static final String deepLink = "";

    public TaxiRideModel(UserModel user) {

        //build url string
        StringBuilder urlString = new StringBuilder();
        urlString.append(taxiFareFinder_base_url);
        urlString.append("key=");
        urlString.append(taxiFareFinder_client_id);
//        urlString.append("&entity_handle=");
//        urlString.append(user.currentCity);
        urlString.append("&origin=");
        urlString.append(user.currentLatitude + "," + user.currentLongitude);
        urlString.append("&destination=");
        urlString.append(user.homeLatitude + "," + user.homeLongitude);

        requestURL = urlString.toString();
        drawableImageResource = "taxi";
        serviceName = "Taxi";
        deepLinkAppName = deepLink;
    }
}
