package com.mantisclaw.italianpride15.ridesharehome;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by italianpride15 on 3/8/15.
 */
public class RideServicesAdapter extends BaseAdapter {

    List<BaseRideModel> rideShareList;

    public RideServicesAdapter(BaseRideModel[] array) {
        rideShareList = new ArrayList<BaseRideModel>(Arrays.asList(array));
    }

    @Override
    public int getCount() {
        return rideShareList.size()-1;
    }

    @Override
    public Object getItem(int position) {
        return rideShareList.get(position+1);
    }

    @Override
    public long getItemId(int position) {
        return position+1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) MainActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ride_service_item, parent, false);
        }

        TextView rideCost = (TextView) convertView.findViewById(R.id.rideShareCost);
        TextView rideSurge = (TextView) convertView.findViewById(R.id.rideShareSurge);
        ImageView rideImage = (ImageView) convertView.findViewById(R.id.rideShareImage);

        BaseRideModel rideModel = rideShareList.get(position+1);

        rideCost.setText("Estimate Cost: $" + rideModel.estimatedCost);
        if (rideModel.surgeRate != null) {
            rideSurge.setText("Surge Rate: " + rideModel.surgeRate);
        } else {
            rideSurge.setText("Surge Rate: n/a");
        }
        int id = MainActivity.context.getResources().getIdentifier(rideModel.drawableImageResource, "drawable", MainActivity.context.getPackageName());
        rideImage.setImageResource(id);

        return convertView;
    }
}
