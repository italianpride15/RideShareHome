package com.mantisclaw.italianpride15.ridesharehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;

import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener{

    //region VARIABLES
    private String homeAddress;

    private String currentLatitude;
    private String currentLongitude;
    private String currentCity;

    private RideModel[] rideModels;
    GoogleApiClient mGoogleApiClient;
    //endregion

    //region Life Cycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getHomeAddress();
        makeGoogleMapsRequest();
        if (appIsAvailableInMyLocation(currentCity)) {
            getPricesFromAvailableServices();
        }
    }
    //endregion

    //region Location Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            currentLatitude = String.valueOf(mLastLocation.getLatitude());
            currentLongitude = String.valueOf(mLastLocation.getLongitude());
        }
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            if (addresses.size() > 0)
                currentCity = addresses.get(0).getLocality();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConnectionSuspended(int i) {
        return;
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        new AlertDialog.Builder(this)
                .setTitle("Location Error!")
                .setMessage("Could not retrieve location. Please check connection and relaunch app")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    //endregion

    //region Touch Events
    /* Image Button onClick call to deeplink to specific apps */
    public void deepLinkToApp(View view) {
        Integer index = Integer.parseInt(view.getTag().toString());

        if (index >= 0 && index < rideModels.length -1) {

            Context context = getApplicationContext();
            PackageManager pm = context.getPackageManager();
            try
            {
                pm.getPackageInfo(rideModels[index].deepLinkAppName, PackageManager.GET_ACTIVITIES);
                // The app is installed! Launch App.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rideModels[index].deepLinkQuery));
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
    //endregion

    //region Helper Methods
    private void getHomeAddress() {

    }

    private void makeGoogleMapsRequest() {
        buildGoogleApiClient();
    }

    private Boolean appIsAvailableInMyLocation(String location) {
        Boolean isAvailable = true;
        //make parse call with location

        return isAvailable;
    }

    private void getPricesFromAvailableServices() {

        rideModels = new RideModel[4];

        //make api calls

        //add update arrays by price
    }
    //endregion
}
