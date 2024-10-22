package com.psik22a.uas.myappsdewiputrisagita22a;

import android.location.Location;
import android.os.Bundle;

public class LocationListener implements android.location.LocationListener {

    public static double latitude;
    public static double longitude;
    public static double speed;
    public static double altitude;

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();       // Speed in meters/second
        altitude = location.getAltitude(); // Altitude in meters
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
