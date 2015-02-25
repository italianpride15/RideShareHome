package com.mantisclaw.italianpride15.ridesharehome;

/**
 * Created by italianpride15 on 2/23/15.
 */
public class ServiceAvailability {
    public Boolean appServiceAvailable;
    public Boolean uberAvailable;
    public Boolean lyftAvailable;
    public Boolean sidecarAvailable;
    public Boolean taxiAvailable;
    public Integer numberOfAvailableServices;

    public ServiceAvailability() {
        numberOfAvailableServices = 4;
    }
}
