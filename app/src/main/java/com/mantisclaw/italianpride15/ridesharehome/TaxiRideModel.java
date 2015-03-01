package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class TaxiRideModel extends BaseRideModel {

    private static final String taxime_client_id = "";
    private static final String taxime_base_url = "http://www.taxime.com/api/1/estimate.json?";
    private static final String deepLink = "com.taxime.client";
    public String query;

    public TaxiRideModel(UserModel user) {

        //build url string
        StringBuilder urlString = new StringBuilder();
        urlString.append(taxime_base_url);
        urlString.append("from=");
        urlString.append(user.startAddress);
        urlString.append("&to=");
        urlString.append(user.homeAddress);
        urlString.append("&key=");
        urlString.append(taxime_client_id);
        urlString.append("&lat_lng=");
        urlString.append(user.currentLatitude);
        urlString.append(",");
        urlString.append(user.currentLongitude);

        query = urlString.toString();
        drawableImageResource = "taxi";
        deepLinkAppName = deepLink;
    }
}
