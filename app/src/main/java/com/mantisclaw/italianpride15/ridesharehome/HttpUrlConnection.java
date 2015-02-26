package com.mantisclaw.italianpride15.ridesharehome;

import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by italianpride15 on 2/24/15.
 */
public class HttpUrlConnection {

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

    public static void makeUberAPICall(BaseRideModel rideModel) throws IOException, JSONException {

        StringBuilder headerString = new StringBuilder();
        headerString.append("Token ");
        headerString.append(UberRideModel.getUber_server_token());

        JSONObject response = makeNetworkRequest(rideModel.requestURL, headerString.toString());
        JSONArray array = response.getJSONArray("prices");

        JSONObject uberX = null;
        for (Integer i = 0; i < array.length()-1; i++) {
            JSONObject object = array.getJSONObject(i);
            if (object.getString("display_name").equals("uberX")) {
                uberX = object;
            }
        }
        rideModel.estimatedCost = uberX.getString("high_estimate");
        rideModel.surgeRate = uberX.getString("surge_multiplier");
    }

    public static void makeLyftAPICall(BaseRideModel rideModel, ParseObject object, GoogleDirectionsAPI info)
            throws IOException, JSONException {
        Double baseRate = object.getDouble("BaseRate");
        Double minCost = object.getDouble("MinCost");
        Double mileRate = object.getDouble("MileRate");
        Double minuteRate = object.getDouble("MinuteRate");
        Double fees = object.getDouble("Fees");

        Double cost = baseRate + (mileRate * info.distance) + (minuteRate/60 * info.duration);
        if (cost < minCost) {
            cost = minCost;
        }
        if (fees > 0) {
            cost += fees;
        }
        rideModel.estimatedCost = cost.toString();
    }

    public static void makeSidecarAPICall(BaseRideModel rideModel, ParseObject object, GoogleDirectionsAPI info)
            throws IOException, JSONException {
        makeLyftAPICall(rideModel, object, info); //will eventually both have APIs
    }

    public static void makeTaxiAPICall(BaseRideModel rideModel) throws IOException, JSONException {

    }

    public static JSONObject makeNetworkRequest(String urlString, String headerString) throws IOException, JSONException {

        // get the JSON And parse it to get the directions data.
        URL url = new URL(urlString);
        HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod("GET");
        if (headerString != null) {
            urlConnection.setRequestProperty("Authorization", headerString);
        }
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.connect();

        InputStream inStream = urlConnection.getInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

        String temp, response = "";
        while((temp = bReader.readLine()) != null){
            //Parse data
            response += temp;
        }
        //Close the reader, stream & connection
        bReader.close();
        inStream.close();
        urlConnection.disconnect();

        return (JSONObject) new JSONTokener(response).nextValue();
    }
}
