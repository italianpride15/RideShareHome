package com.mantisclaw.italianpride15.ridesharehome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, OnItemClickListener {

    //region VARIABLES
    public static String deviceToken;

    //application objects
    private static UserModel user;
    public static BaseRideModel[] rideModels;

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
        deviceToken = UtilityMethods.getDeviceTokenHashed(getBaseContext(), getContentResolver());
    }

    protected void onResume() {
        super.onResume();
        resumeApp();
    }
    //endregion

    //region Address Helper Methods
    private void resumeApp() {

        Boolean hasAddress = UtilityMethods.retrieveDestinationAddress(getContext(), getUser());
        setupAutoCompleteView();

        if (hasAddress == true) {
            makeGoogleMapsRequest();
            mGoogleApiClient.connect();
        } else {
            UtilityMethods.toastMessage("Enter Address", "Please enter an address to continue.", this);
        }
    }

    //endregion

    //region Location Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {

        Boolean completedWithoutErrors;
        completedWithoutErrors = retrieveCurrentLocation();

        if (completedWithoutErrors == false) {
            UtilityMethods.toastMessage("Network Error", "Could not retrieve location. Please check connection and relaunch app.", this);
        } else {
            proceedIfServiceIsAvailable();
        }
    }

    public void onConnectionSuspended(int i) {}

    public void onConnectionFailed(ConnectionResult connectionResult) {
        UtilityMethods.toastMessage("Network Error", "Could not retrieve location. Please check connection and relaunch app.", this);
    }

    private void makeGoogleMapsRequest() {
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    //endregion

    //region Helper Methods
    private Boolean retrieveCurrentLocation() {
        //get last location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            getUser().currentLatitude = String.valueOf(mLastLocation.getLatitude());
            getUser().currentLongitude = String.valueOf(mLastLocation.getLongitude());
        }

        //get current city from location and check if service is available in user's area
        Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;
        List<Address> homeAddress;
        if (mLastLocation == null) {
            return false;
        } else if (getUser().homeAddress != null) {
            try {
                addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                homeAddress = gcd.getFromLocationName(getUser().homeAddress, 1);
                if (addresses.size() > 0 && homeAddress.size() > 0) {
                    getUser().currentCity = addresses.get(0).getLocality();
                    getUser().homeLatitude = String.valueOf(addresses.get(0).getLatitude());
                    getUser().homeLongitude = String.valueOf(addresses.get(0).getLongitude());

                    //track analytics
                    Map<String, String> dictionary = new HashMap<>();
                    dictionary.put("Locality", getUser().currentCity);
                    PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.LOCATION, dictionary);
                }
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private void proceedIfServiceIsAvailable() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CityRate");
        query.whereEqualTo("City", getUser().currentCity);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> rideShareList, ParseException e) {
                if (e == null) {
                    if (rideShareList.size() > 0) {

                        //track analytics
                        Map<String, String> dictionary = new HashMap<>();
                        dictionary.put("NumberOfServices", Integer.toString(rideShareList.size()));
                        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);

                        ServiceAvailability services = new ServiceAvailability(rideShareList);
                        getPricesFromAvailableServices(services);
                    } else {
                        //track analytics
                        Map<String, String> dictionary = new HashMap<>();
                        dictionary.put("LocationUnavailable", getUser().currentCity);
                        PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.STATS, dictionary);
                        UtilityMethods.toastMessage("Services Not Available", "Sorry, your devices shows you are currently in " +
                                getUser().currentCity + ". We hope to add availability in " + getUser().currentCity + " soon.", MainActivity.this);
                    }
                } else {
                    UtilityMethods.toastMessage("Network Error", "Could not retrieve available services.", MainActivity.this);
                }
            }
        });
    }

    private Boolean getPricesFromAvailableServices(ServiceAvailability services) {

        MainActivity.rideModels = new BaseRideModel[services.numberOfAvailableServices];
        GoogleDirectionsAPI info = new GoogleDirectionsAPI(getUser());

        Integer index = 0;
        ParseObject object;

        Map<String, String> dictionary = new HashMap<>();

        object = services.rideShareDictionary.get("Uber");
        if (object != null) {
            UberRideModel uber = new UberRideModel(getUser());
            APIManager.makeAPICall(uber, object, info);
            MainActivity.rideModels[index] = uber;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Uber");
            dictionary.put("ServiceCost", uber.estimatedCost);
            dictionary.put("ServiceSurge", uber.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        object = services.rideShareDictionary.get("Lyft");
        if (object != null) {
            LyftRideModel lyft = new LyftRideModel(getUser());
            APIManager.makeAPICall(lyft, object, info);
            MainActivity.rideModels[index] = lyft;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Lyft");
            dictionary.put("ServiceCost", lyft.estimatedCost);
            dictionary.put("ServiceSurge", lyft.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        object = services.rideShareDictionary.get("Sidecar");
        if (object != null) {
            SidecarRideModel sidecar = new SidecarRideModel(getUser());
            APIManager.makeAPICall(sidecar, object, info);
            MainActivity.rideModels[index] = sidecar;
            index++;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Sidecar");
            dictionary.put("ServiceCost", sidecar.estimatedCost);
            dictionary.put("ServiceSurge", sidecar.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        object = services.rideShareDictionary.get("Taxi");
        if (object != null) {
            TaxiRideModel taxi = new TaxiRideModel(getUser());
            APIManager.makeAPICall(taxi, object, info);
            MainActivity.rideModels[index] = taxi;

            //track analytics
            dictionary.clear();
            dictionary.put("ServiceAvailable", "Taxi");
            dictionary.put("ServiceCost", taxi.estimatedCost);
            dictionary.put("ServiceSurge", taxi.surgeRate);
            PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.SERVICES, dictionary);
        }

        sortArrayByCostAndInstalled();
        updateViewWithData();
        return true;
    }

    private void sortArrayByCostAndInstalled() {

        PackageManager pm = getContext().getPackageManager();

        //check if apps are installed
        for (BaseRideModel model : rideModels) {
            if (model.getClass() != TaxiRideModel.class) {
                try {
                    pm.getPackageInfo(model.deepLinkAppName, PackageManager.GET_ACTIVITIES);
                    model.isInstalled = true;
                } catch (Exception e) {
                    model.isInstalled = false;
                }
            } else {
                //since we don't have a taxi app, assumed installed to not discourage use
                model.isInstalled = true;
            }
        }

        Arrays.sort(rideModels);
    }
    //endregion

    //region UI
    private void setupAutoCompleteView() {
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.list_item, getUser()));
        autoCompView.setOnItemClickListener(this);

        //update home address
        if (getUser().homeAddress != null) {
            autoCompView.setText(getUser().homeAddress);
        }
    }
    private void updateViewWithData() {
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
    }

    //region Touch Events

    /* Image Button onClick call to deeplink to specific apps */
    public void deepLinkToApp(View view) {
        deepLinkToApp(0);
    }

    private void deepLinkToApp(int index) {

        if (rideModels != null) {
            if (index >= 0 && index < rideModels.length) {

                PackageManager pm = getContext().getPackageManager();
                try
                {
                    pm.getPackageInfo(rideModels[index].deepLinkAppName, PackageManager.GET_ACTIVITIES);
                    // The app is installed! Launch App.

                    //track analytics
                    Map<String, String> dictionary = new HashMap<>();
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
                    Map<String, String> dictionary = new HashMap<>();
                    dictionary.put("AppNotInstalled", rideModels[index].deepLinkAppName);
                    PFAnalytics.trackEvent(PFAnalytics.AnalyticsCategory.DEEPLINK, dictionary);

                    // App not installed! Launch popup.
                    if (!rideModels[index].serviceName.equals("Taxi")) {
                        UtilityMethods.toastMessage("App Not Installed", "Please install the " + rideModels[index].serviceName + " app.", this);
                    }
                }
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String newAddress = (String) adapterView.getItemAtPosition(position);
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.HomeAddress);
        Toast.makeText(this, newAddress, Toast.LENGTH_SHORT).show();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);
        autoCompView.clearFocus();
        getUser().homeAddress = newAddress;
        UtilityMethods.storeDestinationAddress(getContext(), getUser());
        makeGoogleMapsRequest();
        mGoogleApiClient.connect();
    }
    //endregion

}
