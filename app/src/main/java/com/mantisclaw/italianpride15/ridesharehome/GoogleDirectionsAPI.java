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
    int distance;
    int duration;

    public GoogleDirectionsAPI(String currentLatitude, String currentLongitude, String homeAddress) {
        try {
            getDirections(currentLatitude, currentLongitude, homeAddress);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDirections(String currentLatitude, String currentLongitude, String homeAddress)
            throws IOException, JSONException {

        //build url string
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=");//from
        urlString.append(currentLatitude);
        urlString.append(",");
        urlString.append(currentLongitude);
        urlString.append("&destination=");//to
        urlString.append(homeAddress);
        urlString.append("&key=AIzaSyAtkemj4ZgNEX98GYK6v6cv2glJiDcyXAE");

        // get the JSON And parse it to get the directions data.
        URL url = new URL(urlString.toString());
        HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod("GET");
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

        //Sortout JSONresponse
        JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
        JSONArray array = object.getJSONArray("routes");
        JSONObject routes = array.getJSONObject(0);
        JSONObject legs = routes.getJSONObject("legs");

        JSONObject distance = legs.getJSONObject("distance");
        JSONObject duration = legs.getJSONObject("duration");

        this.distance = distance.getInt("value");
        this.duration = duration.getInt("value");
    }
}
