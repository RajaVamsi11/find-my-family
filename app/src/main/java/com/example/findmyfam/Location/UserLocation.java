package com.example.findmyfam.Location;

public class UserLocation {

    private double latitude,longitude;
    private String date;

    public UserLocation(){

    }

    public UserLocation(double latitude, double longitude,String date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date=date;

    }

    public String getDate() {
        return date;
    }



    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
