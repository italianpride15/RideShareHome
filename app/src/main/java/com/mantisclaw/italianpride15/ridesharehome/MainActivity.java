package com.mantisclaw.italianpride15.ridesharehome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;

import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, OnItemClickListener {

    //region VARIABLES
    //parse
    private static final String parse_application_id = "Z6WVHg75MPlkEmPYDB8Oa5CED9Wukm7UyGi1w79s";
    private static final String parse_client_id = "s5OF0qYXOgRelbps5ySIOBLeoG4GnrgL6BXprtQQ";
    public static String deviceToken;

    //resource
    private static final String imageButtonResource = "rideShareImageButton";
    private static final String textViewResource = "rideShareTextView";

    //application objects
    public static UserModel user;
    private BaseRideModel[] rideModels;

    //location
    public static Context context;
    GoogleApiClient mGoogleApiClient;
    //endregion

    //region Lazy Loaded Variables
    public Context getContext() {
        if (context == null) {
            context = getApplicationContext();
        }
        return context;
    }

    public UserModel getUser() {
        if (user == null) {
            user = new UserModel();
        }
        return user;
    }
    //endregion

    //region Life Cycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initParse();
        getDeviceTokenHashed();
        //onConnect proceeds with execution
    }

    protected void onResume() {
        makeGoogleMapsRequest();
        mGoogleApiClient.connect();
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
            getUser().currentLatitude = String.valueOf(mLastLocation.getLatitude());
            getUser().currentLongitude = String.valueOf(mLastLocation.getLongitude());
        }

        //get current city from location and check if service is available in user's area
        Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            if (addresses.size() > 0) {
                String currentCity = addresses.get(0).getLocality();

                //track analytics
                Map<String, String> dictionary = new HashMap<String, String>();
                dictionary.put("Locality", currentCity);
                PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.LOCATION, dictionary);

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
        showAlertDialog("Network Error", "Could not retrieve location. Please check connection and relaunch app.");
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
    public void deepLinkToApp(int index) {

        if (index >= 0 && index < rideModels.length) {

            PackageManager pm = getContext().getPackageManager();
            try
            {
                pm.getPackageInfo(rideModels[index].deepLinkAppName, PackageManager.GET_ACTIVITIES);
                // The app is installed! Launch App.

                //track analytics
                Map<String, String> dictionary = new HashMap<String, String>();
                dictionary.put("AppIsInstalled", rideModels[index].deepLinkAppName);
                PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.DEEPLINK, dictionary);

                dictionary.clear();
                dictionary.put("ServiceType", rideModels[index].deepLinkAppName);
                dictionary.put("ServiceRank", Integer.toString(index));
                if (index != 0) {
                    Integer tempIndex = index;
                    while (tempIndex > 0) {
                        tempIndex -= 1;
                        dictionary.put("ServiceAhead" + Integer.toString(tempIndex), rideModels[tempIndex].deepLinkAppName);
                        dictionary.put("ServiceAheadCost" + Integer.toString(tempIndex), rideModels[tempIndex].estimatedCost);
                        dictionary.put("ServiceAheadSurge" + Integer.toString(tempIndex), rideModels[tempIndex].surgeRate);
                    }
                }
                PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.DEEPLINK, dictionary);

                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(rideModels[index].deepLinkAppName);
                startActivity(LaunchIntent);
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rideModels[index].deepLinkQuery));
//                getContext().startActivity(intent);
            }
            catch (PackageManager.NameNotFoundException e)
            {
                //track analytics
                Map<String, String> dictionary = new HashMap<String, String>();
                dictionary.put("AppNotInstalled", rideModels[index].deepLinkAppName);
                PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.DEEPLINK, dictionary);

                // App not installed! Launch popup.
                showAlertDialog("App Not Installed", "Please install the app.");
            }
        }
    }
    //endregion

    //region Helper Methods
    private void makeGoogleMapsRequest() {
        buildGoogleApiClient();
    }

    private void retrieveHomeAddress() {

        SharedPreferences preferences = getContext().getSharedPreferences("RideShareHomePreferences", getContext().MODE_PRIVATE);
        getUser().homeAddress = preferences.getString("homeAddress", null);

        if (getUser().homeAddress == null) {

            //track analytics
            Map<String, String> dictionary = new HashMap<String, String>();
            dictionary.put("NoAddress", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);

            getUser().homeAddress = "626W.Patterson,Chicago";
            getUser().homeLatitude = "41.948756";
            getUser().homeLongitude = "-87.647285";
            //get user's address
            //use google completion
            storeHomeAddress();
        } else {
            //track analytics
            Map<String, String> dictionary = new HashMap<String, String>();
            dictionary.put("AddressRetrieved", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);
        }
    }

    private void storeHomeAddress() {
        if (getUser().homeAddress != null) {
            SharedPreferences preferences = getContext().getSharedPreferences("RideShareHomePreferences", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("homeAddress", getUser().homeAddress);
            editor.putString("homeLatitude", getUser().homeLatitude);
            editor.putString("homeLongitude", getUser().homeLongitude);
            editor.commit();

            //track analytics
            Map<String, String> dictionary = new HashMap<String, String>();
            dictionary.put("AddressStored", "true");
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.USER, dictionary);
        }
    }

    private void proceedIfServiceIsAvailable(final String currentCity) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CityRate");
        query.whereEqualTo("City", "Chicago");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> rideShareList, ParseException e) {
                if (e == null) {
                    if (rideShareList.size() > 0) {

                        //track analytics
                        Map<String, String> dictionary = new HashMap<String, String>();
                        dictionary.put("NumberOfServices", Integer.toString(rideShareList.size()));
                        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);

                        ServiceAvailability services = new ServiceAvailability(rideShareList);
                        getPricesFromAvailableServices(services);
                    } else {
                        showAlertDialog("Services Not Available", "Sorry, this app is not yet available in your city.");
                    }
                } else {
                    showAlertDialog("Network Error", "Could not retrieve available services.");
                }
            }
        });
    }

    private void getPricesFromAvailableServices(ServiceAvailability services) {

        rideModels = new BaseRideModel[services.numberOfAvailableServices-1];
        GoogleDirectionsAPI info = new GoogleDirectionsAPI(getUser());

        Integer index = 0;
        ParseObject object;

        Map<String, String> dictionary = new HashMap<String, String>();

        object = (ParseObject)services.rideShareDictionary.get("Uber");
        if (object != null) {
            UberRideModel uber = new UberRideModel(getUser());
            APIManager.makeAPICall(uber, object, info);
            rideModels[index] = uber;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Uber");
            dictionary.put("ServiceCost", uber.estimatedCost);
            dictionary.put("ServiceSurge", uber.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        object = (ParseObject)services.rideShareDictionary.get("Lyft");
        if (object != null) {
            LyftRideModel lyft = new LyftRideModel(getUser());
            APIManager.makeAPICall(lyft, object, info);
            rideModels[index] = lyft;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Lyft");
            dictionary.put("ServiceCost", lyft.estimatedCost);
            dictionary.put("ServiceSurge", lyft.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        object = (ParseObject)services.rideShareDictionary.get("Sidecar");
        if (object != null) {
            SidecarRideModel sidecar = new SidecarRideModel(getUser());
            APIManager.makeAPICall(sidecar, object, info);
            rideModels[index] = sidecar;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Sidecar");
            dictionary.put("ServiceCost", sidecar.estimatedCost);
            dictionary.put("ServiceSurge", sidecar.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

//        object = (ParseObject)services.rideShareDictionary.get("Taxi");
//        if (object != null) {
//            TaxiRideModel taxi = new TaxiRideModel(getUser());
//            APIManager.makeAPICall(taxi, object, info);
//            rideModels[index] = taxi;
//            index++;

        //track analytics
//        dictionary.clear();
//        dictionary.put("ServiceAvailable", "Taxi");
//        dictionary.put("ServiceCost", taxi.estimatedCost);
//        dictionary.put("ServiceSurge", taxi.surgeRate);
//        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
//        }
        Arrays.sort(rideModels);
        updateViewWithData();
    }

    public void updateViewWithData() {
        //setup autocomplete for home address
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.list_item, getUser()));
        autoCompView.setOnItemClickListener(this);

        //update home address
        autoCompView.setText(getUser().homeAddress);

        RideServicesAdapter adapter = new RideServicesAdapter(rideModels);
        ListView serviceList = (ListView) findViewById(R.id.rideShareListView);
        serviceList.setAdapter(adapter);
        serviceList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                    long id) {
                deepLinkToApp(position);
            }
        });

    }
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);
    }

    private void initParse() {
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, parse_application_id, parse_client_id);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    /*
        Get hashed version of deviceUUID so we have unique reference for user without having
        their phone's private information
     */
    private void getDeviceTokenHashed() {
        final TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + telephonyManager.getDeviceId();
        tmSerial = "" + telephonyManager.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        deviceToken = deviceUuid.toString();
    }

    private void showAlertDialog(String title, String message) {

        //track analytics
        Map<String, String> dictionary = new HashMap<String, String>();
        dictionary.put(title, message);
        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.ALERTS, dictionary);

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
