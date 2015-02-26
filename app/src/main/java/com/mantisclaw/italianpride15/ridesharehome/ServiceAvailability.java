package com.mantisclaw.italianpride15.ridesharehome;

import com.parse.ParseObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by italianpride15 on 2/23/15.
 */
public class ServiceAvailability {
    public Map rideShareDictionary;
    public Integer numberOfAvailableServices;

    public ServiceAvailability(List<ParseObject> rideShareList) {
        rideShareDictionary = new HashMap<String, ParseObject>();
        for (ParseObject object : rideShareList) {
            rideShareDictionary.put(object.getString("ServiceName"), object);
        }
        numberOfAvailableServices = rideShareList.size();
    }
}
