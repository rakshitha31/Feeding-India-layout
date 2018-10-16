package com.android.developer.feedingindia.pojos;

public class FeedingIndiaDonor {

    private String name,email,mobileNumber,dateOfBirth,userType,city,previousRole;
    private boolean requestedToBeAdmin;

    public FeedingIndiaDonor(){

    }

    public FeedingIndiaDonor(String name, String email, String mobileNumber, String dateOfBirth, String userType, String city, boolean requestedToBeAdmin,String previousRole) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.userType = userType;
        this.city = city;
        this.requestedToBeAdmin = requestedToBeAdmin;
        this.previousRole = previousRole;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getUserType() {
        return userType;
    }

    public String getCity() {
        return city;
    }

    public boolean isRequestedToBeAdmin() {
        return requestedToBeAdmin;
    }

    public String getPreviousRole() {
        return previousRole;
    }
}


