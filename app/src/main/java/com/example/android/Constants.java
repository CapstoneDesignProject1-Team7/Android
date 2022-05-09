package com.example.android;

import android.Manifest;

public class Constants {
    public static final int REQUEST_CHECK_SETTINGS = 2000;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    public static final int LOCATION_SERVICE_ID = 175;
    public static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };
    public static int NETWORK_FREQUENCY = 3000;
}
