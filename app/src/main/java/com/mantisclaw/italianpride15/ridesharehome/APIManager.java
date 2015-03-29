package com.mantisclaw.italianpride15.ridesharehome;

import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class APIManager {

    public static void makeAPICall(BaseRideModel rideModel, ParseObject object, GoogleDirectionsAPI info) {
        //get object type and call correct API method
        try {
            if (rideModel.getClass() == UberRideModel.class) {
                makeUberAPICall(rideModel);
            } else if (rideModel.getClass() == LyftRideModel.class) {
                makeLyftAPICall(rideModel, object, info);
            } else if (rideModel.getClass() == SidecarRideModel.class) {
                makeSidecarAPICall(rideModel, object, info);
            } else if (rideModel.getClass() == TaxiRideModel.class) {
                makeTaxiAPICall(rideModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeUberAPICall(BaseRideModel rideModel) throws ExecutionException, InterruptedException, JSONException {

        StringBuilder headerString = new StringBuilder();
        headerString.append("Token ");
        headerString.append(UberRideModel.getUber_server_token());

        JSONObject response = makeNetworkRequest(rideModel.requestURL, headerString.toString());
        JSONArray array = response.getJSONArray("prices");

        JSONObject uberX = new JSONObject();
        for (Integer i = 0; i < array.length()-1; i++) {
            JSONObject object = array.getJSONObject(i);
            if (object.getString("display_name").equals("uberX")) {
                uberX = object;
            }
        }

        String highEstimate = uberX.getString("high_estimate");
        String lowEstimate = uberX.getString("low_estimate");
        String estimate = "n/a";

        if (highEstimate != null && lowEstimate != null) {
            Double average = ((Double.parseDouble(highEstimate) + Double.parseDouble(lowEstimate)) / 2);
            estimate = average.toString();
        } else if (highEstimate != null) {
            estimate = highEstimate;
        } else if (lowEstimate != null) {
            estimate = lowEstimate;
        }
        rideModel.estimatedCost = estimate;
        rideModel.surgeRate = uberX.getString("surge_multiplier");
        rideModel.deepLinkQuery += "&product_id=" + uberX.get("product_id");
    }

    public static void makeLyftAPICall(BaseRideModel rideModel, ParseObject object, GoogleDirectionsAPI info)
            throws IOException, JSONException {
        Double baseRate = object.getDouble("BaseRate");
        Double minCost = object.getDouble("MinCost");
        Double mileRate = object.getDouble("MileRate");
        Double minuteRate = object.getDouble("MinuteRate");
        Double fees = object.getDouble("Fees");

        Double cost = baseRate + (mileRate/1609.34 * info.distance) + (minuteRate/60 * info.duration);
        if (cost < minCost) {
            cost = minCost;
        }
        if (fees > 0) {
            cost += fees;
        }
        rideModel.estimatedCost = String.format("%.2f", cost);
        //TODO: get surge rate for Lyft
    }

    public static void makeSidecarAPICall(BaseRideModel rideModel, ParseObject object, GoogleDirectionsAPI info)
            throws IOException, JSONException {
        makeLyftAPICall(rideModel, object, info); //will eventually both have APIs
    }

    public static void makeTaxiAPICall(BaseRideModel rideModel) throws ExecutionException, InterruptedException, JSONException {

        JSONObject response = makeNetworkRequest(rideModel.requestURL, null);
        Double cost = Double.parseDouble(response.getString("total_fare")) - Double.parseDouble(response.getString("tip_amount"));
        rideModel.estimatedCost =  String.format("%.2f", cost);
    }

    public static JSONObject makeNetworkRequest(String urlString, String headerString) throws ExecutionException, InterruptedException {
        HttpUrlConnection connection = new HttpUrlConnection(urlString, headerString);
        return connection.execute().get();
    }
}
