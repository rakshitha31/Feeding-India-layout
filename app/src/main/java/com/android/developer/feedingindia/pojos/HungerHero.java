package com.android.developer.feedingindia.pojos;

import java.util.ArrayList;

public class HungerHero {

    private String name,dateOfBirth,email,mobileNumber,userType,educationalBackground,state,city,locality,pinCode,
            reasonForJoining,affordableTime,responsibility,currentlyPartOf,previousRole;
    private ArrayList<String> introducedToFIThrough,aboutMe;
    private boolean requestedToBeAdmin,emailVerified;

    public HungerHero(){

    }

    public HungerHero(String name, String dateOfBirth, String email, String mobileNumber, String userType, String educationalBackground, String state, String city, String locality, String pinCode, String reasonForJoining, String affordableTime, String responsibility, String currentlyPartOf, ArrayList<String> introducedToFIThrough, ArrayList<String> aboutMe, boolean requestedToBeAdmin,String previousRole,boolean emailVerified) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.userType = userType;
        this.educationalBackground = educationalBackground;
        this.state = state;
        this.city = city;
        this.locality = locality;
        this.pinCode = pinCode;
        this.reasonForJoining = reasonForJoining;
        this.affordableTime = affordableTime;
        this.responsibility = responsibility;
        this.currentlyPartOf = currentlyPartOf;
        this.introducedToFIThrough = introducedToFIThrough;
        this.aboutMe = aboutMe;
        this.requestedToBeAdmin = requestedToBeAdmin;
        this.previousRole = previousRole;
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getUserType() {
        return userType;
    }

    public String getEducationalBackground() {
        return educationalBackground;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getLocality() {
        return locality;
    }

    public String getPinCode() {
        return pinCode;
    }

    public String getReasonForJoining() {
        return reasonForJoining;
    }

    public String getAffordableTime() {
        return affordableTime;
    }

    public String getResponsibility() {
        return responsibility;
    }

    public String getCurrentlyPartOf() {
        return currentlyPartOf;
    }

    public ArrayList<String> getIntroducedToFIThrough() {
        return introducedToFIThrough;
    }

    public ArrayList<String> getAboutMe() {
        return aboutMe;
    }

    public boolean isRequestedToBeAdmin() {
        return requestedToBeAdmin;
    }

    public String getPreviousRole() {
        return previousRole;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
