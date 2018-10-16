package com.android.developer.feedingindia.pojos;

import java.util.HashMap;

public class DeliveryDetails {

    private String donationId,hungerSpotId,donationUserId;
    private String donorName,donorConactNumber,deliveredOn;
    private HashMap<String,String> donorAddress,hungerSpotAddress;
    private String status;
    private String deliveryImgUrl,donationImgUrl;
    private Long timeStamp = 0L;

    public DeliveryDetails(){

    }

    public DeliveryDetails(String donationId, String hungerSpotId, String donationUserId, String donorName, String donorConactNumber, String deliveredOn, HashMap<String, String> donorAddress, HashMap<String, String> hungerSpotAddress, String status, String deliveryImgUrl, String donationImgUrl) {
        this.donationId = donationId;
        this.hungerSpotId = hungerSpotId;
        this.donationUserId = donationUserId;
        this.donorName = donorName;
        this.donorConactNumber = donorConactNumber;
        this.deliveredOn = deliveredOn;
        this.donorAddress = donorAddress;
        this.hungerSpotAddress = hungerSpotAddress;
        this.status = status;
        this.deliveryImgUrl = deliveryImgUrl;
        this.donationImgUrl = donationImgUrl;
    }

    public String getDonationId() {
        return donationId;
    }

    public String getHungerSpotId() {
        return hungerSpotId;
    }

    public String getDonationUserId() {
        return donationUserId;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonorConactNumber() {
        return donorConactNumber;
    }

    public String getDeliveredOn() {
        return deliveredOn;
    }

    public HashMap<String, String> getDonorAddress() {
        return donorAddress;
    }

    public HashMap<String, String> getHungerSpotAddress() {
        return hungerSpotAddress;
    }

    public String getStatus() {
        return status;
    }

    public String getDeliveryImgUrl() {
        return deliveryImgUrl;
    }

    public String getDonationImgUrl() {
        return donationImgUrl;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public void setHungerSpotId(String hungerSpotId) {
        this.hungerSpotId = hungerSpotId;
    }

    public void setDonationUserId(String donationUserId) {
        this.donationUserId = donationUserId;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public void setDonorConactNumber(String donorConactNumber) {
        this.donorConactNumber = donorConactNumber;
    }

    public void setDeliveredOn(String deliveredOn) {
        this.deliveredOn = deliveredOn;
    }

    public void setDonorAddress(HashMap<String, String> donorAddress) {
        this.donorAddress = donorAddress;
    }

    public void setHungerSpotAddress(HashMap<String, String> hungerSpotAddress) {
        this.hungerSpotAddress = hungerSpotAddress;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDeliveryImgUrl(String deliveryImgUrl) {
        this.deliveryImgUrl = deliveryImgUrl;
    }

    public void setDonationImgUrl(String donationImgUrl) {
        this.donationImgUrl = donationImgUrl;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
