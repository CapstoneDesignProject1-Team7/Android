package com.example.android;

public class UserDTO {
    private String id;
    private int type;
    private double latitude;
    private double longitude;
    private int speed;

    public UserDTO(String id, int type, double latitude, double longitude, int speed) {
        super();
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public UserDTO(String id, int type, double latitude, double longitude) {
        super();
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getSpeed() { return speed; }

    public void setSpeed(int speed) { this.speed = speed; }
}
