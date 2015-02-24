package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/22/15.
 */

public class RideModel {

    public enum RideShareType {
        UBER, LYFT, SIDECARE, TAXI
    }

    public static final String uber_client_id = "XikKX8YmAoiTaE4PxmvKK2xImKtMFKIG";
    public static final String uber_server_token = "p4xGiso2Lt5slHGTHSg9fHRgJMT0OUmsn5ksrJw3";

    public String deepLinkAppName;
    public String deepLinkQuery;

    public String estimatedCost;
    public String surgeRate;

    RideModel(RideShareType type, GoogleDirectionsAPI info) {
        switch (type) {
            case UBER:
                deepLinkAppName = "com.ubercab";
                deepLinkQuery = "uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=";
                break;
            case LYFT:
//                NSURL *lyftURL =[NSURL URLWithString:@"fb275560259205767://"];
//                if ([[UIApplication sharedApplication] canOpenURL:lyftURL]) {
//                [[UIApplication sharedApplication] openURL:lyftURL];
                deepLinkAppName = "com.lyft";
                deepLinkQuery = "";
                break;
            case SIDECARE:
                deepLinkAppName = "com.sidecar";
                deepLinkQuery = "";
                break;
            case TAXI:
                //nothing to handle
                break;
        }
    }
}
