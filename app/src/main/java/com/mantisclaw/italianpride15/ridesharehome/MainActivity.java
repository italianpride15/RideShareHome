package com.mantisclaw.italianpride15.ridesharehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends ActionBarActivity {

    private static String kUberAppName = "com.ubercab";
    private static String kLyftAppName;
    private static String kSideCarAppName;
    private static String kTaxiAppName;

    private static String kUberDeepLinkQuery = "uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=";
    private static String kLyftDeepLinkQuery;
    private static String kSideCarDeepLinkQuery;
    private static String kTaxiDeepLinkQuery;

    private String[] appName;
    private String[] appQueryLink;

    private String homeAddress;
    private String currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeGoogleMapsRequest();
        if (appIsAvailableInMyLocation(currentLocation)) {
            getPricesFromAvailableServices();
        }
    }

    /* Image Button onClick call to deeplink to specific apps */
    public void deepLinkToApp1(View view) {
        String appExists;
        Uri appQuery;
        Integer index = Integer.parseInt(view.getTag().toString());

        if (index >= 0 && index < appQueryLink.length -1) {
            appExists = appName[index];
            appQuery = Uri.parse(appQueryLink[index]);

            Context context = getApplicationContext();
            PackageManager pm = context.getPackageManager();
            try
            {

                pm.getPackageInfo(appExists, PackageManager.GET_ACTIVITIES);
                // The app is installed! Launch App.
                Intent intent = new Intent(Intent.ACTION_VIEW, appQuery);
                context.startActivity(intent);

            }
            catch (PackageManager.NameNotFoundException e)
            {
                // App not installed! Launch popup.
                new AlertDialog.Builder(this)
                        .setTitle("App Not Installed")
                        .setMessage("Please install the app.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    private void makeGoogleMapsRequest() {
        //get home address

        //get current location
    }

    private Boolean appIsAvailableInMyLocation(String location) {
        Boolean isAvailable = true;
        //make parse call with location

        return isAvailable;
    }

    private void getPricesFromAvailableServices() {

        appName = new String[4];
        appQueryLink = new String[4];

        //make api calls

        //add update arrays by price
    }
}
