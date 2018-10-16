package com.android.developer.feedingindia.pojos;

public class HungerSpot {

    private String addedBy;
    private String status;
    private double latitude;
    private double longitude;

    public HungerSpot(){

    }

    public HungerSpot(String addedBy, String status, double latitude, double longitude) {
        this.addedBy = addedBy;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getStatus() {
        return status;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
