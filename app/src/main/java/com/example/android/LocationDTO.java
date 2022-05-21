package com.example.android;

public class LocationDTO {
    private double latitude;
    private double longitude;
    private int velocity;
    private boolean within10m;

    public LocationDTO(double latitude, double longitude, int velocity) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.velocity = velocity;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public int getVelocity() { return velocity; }
    public void setVelocity(int velocity) { this.velocity = velocity; }
    public boolean isWithin10m() { return within10m; }
    public void setWithin10m(boolean within10m) { this.within10m = within10m; }
}
