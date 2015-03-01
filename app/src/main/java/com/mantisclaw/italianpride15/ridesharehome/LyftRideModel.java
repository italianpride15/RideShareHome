package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class LyftRideModel extends BaseRideModel {

    private static final String deepLink = "me.lyft.android";

    public LyftRideModel(UserModel user) {
        drawableImageResource = "lyft";
        deepLinkAppName = deepLink;
        //fill in when API becomes available
    }
}
