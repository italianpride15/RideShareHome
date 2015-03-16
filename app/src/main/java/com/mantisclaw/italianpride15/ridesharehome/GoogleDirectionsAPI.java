package com.mantisclaw.italianpride15.ridesharehome;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by italianpride15 on 2/23/15.
 */
public class GoogleDirectionsAPI {
    public Double distance;
    public Double duration;

    private String directions_base_url = "https://maps.googleapis.com/maps/api/directions/json?";
    private String directions_client_id = "AIzaSyAtkemj4ZgNEX98GYK6v6cv2glJiDcyXAE";

    public GoogleDirectionsAPI(UserModel user) {
        try {
            getDirections(user);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getDirections(UserModel user) throws ExecutionException, InterruptedException, JSONException {

        //build url string
        StringBuilder urlString = new StringBuilder();
        urlString.append(directions_base_url);
        urlString.append("origin=");
        urlString.append(user.currentLatitude);
        urlString.append(",");
        urlString.append(user.currentLongitude);
        urlString.append("&destination=");
        urlString.append(user.homeAddress.replaceAll("\\s",""));
        urlString.append("&key=");
        urlString.append(directions_client_id);

        //make request and get back json object
        JSONObject object = APIManager.makeNetworkRequest(urlString.toString(), null);

        //parse through json object
        JSONArray array = object.getJSONArray("routes");
        JSONObject routes = array.getJSONObject(0);
        JSONArray array1 = routes.getJSONArray("legs");
        JSONObject legs = array1.getJSONObject(0);

        user.startAddress = legs.getString("start_address");
        user.homeLatitude = legs.getJSONObject("end_location").getString("lat");
        user.homeLongitude = legs.getJSONObject("end_location").getString("lng");

        this.distance = legs.getJSONObject("distance").getDouble("value");
        this.duration = legs.getJSONObject("duration").getDouble("value");
    }
}
