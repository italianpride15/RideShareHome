package com.mantisclaw.italianpride15.ridesharehome;

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
 * Created by italianpride15 on 2/23/15.
 */
public class GoogleDirectionsAPI {
    Double distance;
    Double duration;

    public GoogleDirectionsAPI(UserModel user) {
        try {
            getDirections(user);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDirections(UserModel user)
            throws IOException, JSONException {

        //build url string
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=");
        urlString.append(user.currentLatitude);
        urlString.append(",");
        urlString.append(user.currentLongitude);
        urlString.append("&destination=");
        urlString.append(user.homeAddress);
        urlString.append("&key=AIzaSyAtkemj4ZgNEX98GYK6v6cv2glJiDcyXAE");

        //make request and get back json object
        JSONObject object = HttpUrlConnection.makeNetworkRequest(urlString.toString(), null);

        //parse through json object
        JSONArray array = object.getJSONArray("routes");
        JSONObject routes = array.getJSONObject(0);
        JSONObject legs = routes.getJSONObject("legs");

        user.homeLatitude = legs.getJSONObject("end_location").getString("lat");
        user.homeLongitude = legs.getJSONObject("end_location").getString("lng");

        this.distance = legs.getJSONObject("distance").getDouble("value");
        this.duration = legs.getJSONObject("duration").getDouble("value");
    }
}
