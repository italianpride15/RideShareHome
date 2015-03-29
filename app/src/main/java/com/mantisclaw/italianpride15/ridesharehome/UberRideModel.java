package com.mantisclaw.italianpride15.ridesharehome;

import java.net.URLEncoder;

/**
 * Created by italianpride15 on 2/22/15.
 */

public class UberRideModel extends BaseRideModel {

    private static final String uber_client_id = "XikKX8YmAoiTaE4PxmvKK2xImKtMFKIG";
    private static final String uber_server_token = "p4xGiso2Lt5slHGTHSg9fHRgJMT0OUmsn5ksrJw3";
    private static final String uber_base_url = "https://api.uber.com/v1/estimates/price?";
    private static final String deepLink = "com.ubercab";
    private static final String query = "uber://?client_id=" + uber_client_id + "&action=setPickup&pickup=my_location&dropoff[formatted_address]=";

    UberRideModel(UserModel user) {
        client_id = uber_client_id;
        deepLinkAppName = deepLink;

        StringBuilder requestString = new StringBuilder();
        requestString.append(uber_base_url);
        requestString.append("start_latitude=");
        requestString.append(user.currentLatitude);
        requestString.append("&start_longitude=");
        requestString.append(user.currentLongitude);
        requestString.append("&end_latitude=");
        requestString.append(user.homeLatitude);
        requestString.append("&end_longitude=");
        requestString.append(user.homeLongitude);
        requestURL = requestString.toString();

        StringBuilder queryString = new StringBuilder();
        queryString.append(query);
        try {
            queryString.append(URLEncoder.encode(user.homeAddress, "UTF-8").toString().replace("+", "%20"));
        } catch (Exception e) {
            //if failed, use lat and long
            queryString.append("&dropoff[latitude]=" + user.homeLatitude);
            queryString.append("&dropoff[longitude]=" + user.homeLongitude);
        }
        deepLinkQuery = queryString.toString() ;
        drawableImageResource = "uber";
        serviceName = "Uber";
    }

    public static String getUber_server_token() {
        return uber_server_token;
    }
}
