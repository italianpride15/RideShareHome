package com.mantisclaw.italianpride15.ridesharehome;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by italianpride15 on 4/5/15.
 */
public class UtilityMethods {

    public static Boolean retrieveDestinationAddress(Context context, UserModel user) {

        SharedPreferences preferences = context.getSharedPreferences("RideShareHomePreferences", context.MODE_PRIVATE);
        user.homeAddress = preferences.getString("homeAddress", null);

        if (user.homeAddress == null) {

            //track analytics
            Map<String, String> dictionary = new HashMap<>();
            dictionary.put("NoAddress", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);
            return false;

        } else {
            //track analytics
            Map<String, String> dictionary = new HashMap<>();
            dictionary.put("AddressRetrieved", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);
            return true;
        }
    }

    public static void storeDestinationAddress(Context context, UserModel user) {
        if (user.homeAddress != null) {
            SharedPreferences preferences = context.getSharedPreferences("RideShareHomePreferences", context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("homeAddress", user.homeAddress);
            editor.putString("homeLatitude", user.homeLatitude);
            editor.putString("homeLongitude", user.homeLongitude);
            editor.commit();

            //track analytics
            Map<String, String> dictionary = new HashMap<>();
            dictionary.put("AddressStored", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);
        }
    }

    public static void toastMessage(String title, String message, Activity activity) {

        //track analytics
        Map<String, String> dictionary = new HashMap<>();
        dictionary.put(title, message);
        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.ALERTS, dictionary);

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    /*
        Get hashed version of deviceUUID so we have unique reference for user without having
        their phone's private information
    */
    public static String getDeviceTokenHashed(Context baseContext, ContentResolver contentResolver) {
        final TelephonyManager telephonyManager = (TelephonyManager) baseContext.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + telephonyManager.getDeviceId();
        tmSerial = "" + telephonyManager.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString().substring(deviceUuid.toString().length()-12, deviceUuid.toString().length());
    }
}
