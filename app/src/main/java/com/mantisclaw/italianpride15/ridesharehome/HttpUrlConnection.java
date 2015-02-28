package com.mantisclaw.italianpride15.ridesharehome;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by italianpride15 on 2/26/15.
 */
public class HttpUrlConnection extends AsyncTask <String, Integer, JSONObject> {

    private String urlString;
    private String headerString;

    public HttpUrlConnection(String urlString, String headerString) {
        this.urlString = urlString;
        this.headerString = headerString;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        URL url = null;
        HttpsURLConnection urlConnection = null;
        JSONObject jsonObject = new JSONObject();

        try {
            // get the JSON And parse it to get the directions data.
            url = new URL(urlString);
            urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            if (headerString != null) {
                urlConnection.setRequestProperty("Authorization", headerString);
            }

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

            jsonObject = (JSONObject) new JSONTokener(response).nextValue();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null)
                urlConnection.disconnect();
        }
        return jsonObject;
    }
}
