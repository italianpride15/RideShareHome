package com.mantisclaw.italianpride15.ridesharehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private RideModel[] rideModels;
    Context context;
    GoogleApiClient mGoogleApiClient;
    //endregion

    //region Lazy Loaded Variables
    public Context getContext() {
        if (context == null) {
            context = getApplicationContext();
        }
        return context;
    }
    //endregion

    //region Life Cycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeGoogleMapsRequest();
        retrieveHomeAddress();
    }
    //endregion

    //region Location Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        //get last location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            currentLatitude = String.valueOf(mLastLocation.getLatitude());
            currentLongitude = String.valueOf(mLastLocation.getLongitude());
        }

        //get current city from location and check if service is available in user's area
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            if (addresses.size() > 0) {
                String currentCity = addresses.get(0).getLocality();
                proceedIfServiceIsAvailable(currentCity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConnectionSuspended(int i) {
        return;
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        showAlertDialog("Location Error!", "Could not retrieve location. Please check connection and relaunch app");
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
                showAlertDialog("App Not Installed", "Please install the app.");
            }
        }
    }
    //endregion

    //region Helper Methods
    private void retrieveHomeAddress() {

        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        homeAddress = preferences.getString("homeAddress", null);

        if (homeAddress == null) {
            //get user's address
        }
    }

    private void storeHomeAddress() {
        if (homeAddress != null) {
            SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("homeAddress", homeAddress);
            editor.commit();
        }
    }

    private void makeGoogleMapsRequest() {
        buildGoogleApiClient();
    }

    private void proceedIfServiceIsAvailable(String currentCity) {
        ServiceAvailability services = new ServiceAvailability(); //replace with parse call

        if (services.appServiceAvailable == true) {
            getPricesFromAvailableServices(services);
        } else {
            showAlertDialog("Service Not Available", "Sorry, this app is not available in your city yet");
        }
    }

    private void getPricesFromAvailableServices(ServiceAvailability services) {

        rideModels = new RideModel[services.numberOfAvailableServices];
        GoogleDirectionsAPI info = new GoogleDirectionsAPI(currentLatitude, currentLongitude, homeAddress);

        int index = 0;
        if (services.uberAvailable == true) {
            RideModel uber = new RideModel(RideModel.RideShareType.UBER, info);
            rideModels[index] = uber;
            index++;
        }
        if (services.lyftAvailable == true) {
            RideModel lyft = new RideModel(RideModel.RideShareType.LYFT, info);
            rideModels[index] = lyft;
            index++;
        }
        if (services.sidecarAvailable == true) {
            RideModel sidecar = new RideModel(RideModel.RideShareType.SIDECARE, info);
            rideModels[index] = sidecar;
            index++;
        }
        if (services.taxiAvailable == true) {
            RideModel taxi = new RideModel(RideModel.RideShareType.TAXI, info);
            rideModels[index] = taxi;
            index++;
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //endregion


}
