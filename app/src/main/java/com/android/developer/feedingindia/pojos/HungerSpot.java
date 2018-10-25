package com.android.developer.feedingindia.pojos;

public class HungerSpot {

    private String addedBy;
    private String status;
    private String userRole,addedOn;
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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(String addedOn) {
        this.addedOn = addedOn;
    }
}