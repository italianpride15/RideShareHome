package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class SidecarRideModel extends BaseRideModel {

    private static final String deepLink = "com.sidecarPassenger";

    public SidecarRideModel(UserModel user) {
        drawableImageResource = "sidecar";
        deepLinkAppName = deepLink;
        //fill in when API becomes available
    }
}
