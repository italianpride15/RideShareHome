package com.mantisclaw.italianpride15.ridesharehome;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by italianpride15 on 3/1/15.
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String placesAPIURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String placesAPIkey = "AIzaSyAtkemj4ZgNEX98GYK6v6cv2glJiDcyXAE";

    private ArrayList<String> resultList;
    private UserModel user;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, UserModel user) {
        super(context, textViewResourceId);
        this.user = user;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    try {
                        resultList = autocomplete(constraint.toString(), user);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private ArrayList<String> autocomplete(String input, UserModel user)
            throws UnsupportedEncodingException, ExecutionException, InterruptedException, JSONException {
        ArrayList<String> resultList = null;

        StringBuilder urlString = new StringBuilder(placesAPIURL);
        urlString.append("input=" + URLEncoder.encode(input, "utf8"));
        urlString.append("&location=" + user.homeLatitude + "," + user.homeLongitude);
        urlString.append("&key=" + placesAPIkey);

        // Create a JSON object hierarchy from the results
        JSONObject jsonObj = APIManager.makeNetworkRequest(urlString.toString(), null);
        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

        // Extract the Place descriptions from the results
        resultList = new ArrayList<String>(predsJsonArray.length());
        for (Integer i = 0; i < predsJsonArray.length(); i++) {
            resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
        }

        return resultList;
    }
}
