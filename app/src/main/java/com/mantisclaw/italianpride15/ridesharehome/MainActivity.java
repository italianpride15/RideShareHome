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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;

import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
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
    public static String deviceToken;

    //application objects
    public static UserModel user;
    private BaseRideModel[] rideModels;
    private ProgressBar progressBar;
    private TextView progressText;

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

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        getDeviceTokenHashed();
        //onConnect proceeds with execution
    }

    protected void onResume() {
        super.onResume();
        startSpinner();
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
        List<Address> homeAddress;
        if (mLastLocation == null) {
            toastMessage("Network Error", "Could not retrieve location. Please check connection and relaunch app.");
            updateViewWithData();
        } else if (getUser().homeAddress != null) {
            try {
                addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                homeAddress = gcd.getFromLocationName(getUser().homeAddress, 1);
                if (addresses.size() > 0 && homeAddress.size() > 0) {
                    getUser().currentCity = addresses.get(0).getLocality();
                    getUser().homeLatitude = String.valueOf(addresses.get(0).getLatitude());
                    getUser().homeLongitude = String.valueOf(addresses.get(0).getLongitude());

                    //track analytics
                    Map<String, String> dictionary = new HashMap<String, String>();
                    dictionary.put("Locality", getUser().currentCity);
                    PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.LOCATION, dictionary);

                    proceedIfServiceIsAvailable(getUser().currentCity);
                }
            } catch (IOException e) {
                e.printStackTrace();
                updateViewWithData();
            }
        }
    }

    public void onConnectionSuspended(int i) {
        return;
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        toastMessage("Network Error", "Could not retrieve location. Please check connection and relaunch app.");
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
        deepLinkToApp(0);
    }

    public void deepLinkToApp(int index) {

        if (rideModels != null) {
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
                    if (rideModels[index].deepLinkQuery != null) {
                        LaunchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rideModels[index].deepLinkQuery));
                    }
                    startActivity(LaunchIntent);
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    //track analytics
                    Map<String, String> dictionary = new HashMap<String, String>();
                    dictionary.put("AppNotInstalled", rideModels[index].deepLinkAppName);
                    PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.DEEPLINK, dictionary);

                    // App not installed! Launch popup.
                    if (!rideModels[index].serviceName.equals("Taxi")) {
                        toastMessage("App Not Installed", "Please install the " + rideModels[index].serviceName + " app.");
                    }
                }
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

            toastMessage("Enter Address", "Please enter an address to continue.");
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
        mGoogleApiClient.reconnect();
        startSpinner();
    }

    private void proceedIfServiceIsAvailable(final String currentCity) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CityRate");
        query.whereEqualTo("City", getUser().currentCity);
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
                        //track analytics
                        Map<String, String> dictionary = new HashMap<String, String>();
                        dictionary.put("LocationUnavailable", getUser().currentCity);
                        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.STATS, dictionary);
                        toastMessage("Services Not Available", "Sorry, your devices shows you are currently in " +
                                getUser().currentCity + ". We hope to add availability in " + getUser().currentCity + " soon.");
                    }
                } else {
                    toastMessage("Network Error", "Could not retrieve available services.");
                }
            }
        });
    }

    private void getPricesFromAvailableServices(ServiceAvailability services) {

        rideModels = new BaseRideModel[services.numberOfAvailableServices];
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

        object = (ParseObject)services.rideShareDictionary.get("Taxi");
        if (object != null) {
            TaxiRideModel taxi = new TaxiRideModel(getUser());
            APIManager.makeAPICall(taxi, object, info);
            rideModels[index] = taxi;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Taxi");
            dictionary.put("ServiceCost", taxi.estimatedCost);
            dictionary.put("ServiceSurge", taxi.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        Arrays.sort(rideModels);
        updateViewWithData();
    }

    public void updateViewWithData() {
        endSpinner();

        //setup autocomplete for home address
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.list_item, getUser()));
        autoCompView.setOnItemClickListener(this);

        //update home address
        autoCompView.setText(getUser().homeAddress);

        if (rideModels != null) {

            ImageButton cheapestServiceImage = (ImageButton) findViewById(R.id.rideShareImageButton);
            TextView cheapestServiceCost = (TextView) findViewById(R.id.rideShareTextViewCost);
            TextView cheapestServiceSurge = (TextView) findViewById(R.id.rideShareTextViewSurge);

            int id = getResources().getIdentifier(rideModels[0].drawableImageResource, "drawable", getContext().getPackageName());
            cheapestServiceImage.setImageResource(id);
            cheapestServiceCost.setText("Estimate Cost: $" + rideModels[0].estimatedCost);
            if (rideModels[0].surgeRate != null) {
                cheapestServiceSurge.setText("Surge Rate: " + rideModels[0].surgeRate);
            } else {
                cheapestServiceSurge.setText("Surge Rate: n/a");
            }

            RideServicesAdapter adapter = new RideServicesAdapter(rideModels);
            ListView serviceList = (ListView) findViewById(R.id.rideShareListView);
            serviceList.setAdapter(adapter);
            serviceList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                        long id) {
                    deepLinkToApp(position+1);
                }
            });
        }

        autoCompView.clearFocus();
    }
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String newAddress = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, newAddress, Toast.LENGTH_SHORT).show();
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);
        autoCompView.clearFocus();
        getUser().homeAddress = newAddress;
        storeHomeAddress();
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
        deviceToken = deviceUuid.toString().substring(deviceUuid.toString().length()-12, deviceUuid.toString().length());
    }

    private void toastMessage(String title, String message) {

        //track analytics
        Map<String, String> dictionary = new HashMap<String, String>();
        dictionary.put(title, message);
        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.ALERTS, dictionary);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        if (!title.equals("App Not Installed")) {
            updateViewWithData();
        }
    }
    //endregion

    //region Spinner
    private void startSpinner() {
        RelativeLayout layout = new RelativeLayout(this);
        progressBar = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setId(1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(progressBar, params);

        progressText = new TextView(MainActivity.this, null);
        progressText.setText("Calculating prices...");
        progressText.setTextSize(18);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 80);
        textParams.setMargins(0, 0, 0, 30);
        textParams.addRule(RelativeLayout.ABOVE, progressBar.getId());
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layout.addView(progressText, textParams);

        setContentView(layout);
    }

    private void endSpinner() {
        RelativeLayout layout = new RelativeLayout(this);
        layout.removeView(progressBar);
        layout.removeView(progressText);
        progressBar = null;
        progressText = null;
        setContentView(R.layout.activity_main);
    }
    //endregion


}
