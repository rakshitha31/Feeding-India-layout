package com.android.developer.feedingindia.pojos;

import java.util.HashMap;

public class DonationDetails {

    private String foodDescription,foodPreparedOn,additionalContactNumber,status,donorContactNumber,delivererName,donorName,delivererContactNumber;
    private boolean hasContainer,canDonate;
    private HashMap<String,Object> donorAddress;
    private String donationImageUrl,deliveryImgUrl;
    private String foodType,deliveredOn;
    private String donationUserId;
    private Long timeStamp = 0L;
    private String donationId = "";

    public DonationDetails(){

    }



    public DonationDetails(String foodDescription, String foodPreparedOn, String additionalContactNumber, String status, String donorContactNumber, String delivererName, String donorName, String delivererContactNumber, boolean hasContainer, boolean canDonate, HashMap<String, Object> donorAddress, String donationImageUrl, String deliveryImgUrl, String foodType, String deliveredOn) {
        this.foodDescription = foodDescription;
        this.foodPreparedOn = foodPreparedOn;
        this.additionalContactNumber = additionalContactNumber;
        this.status = status;
        this.donorContactNumber = donorContactNumber;
        this.delivererName = delivererName;
        this.donorName = donorName;
        this.delivererContactNumber = delivererContactNumber;
        this.hasContainer = hasContainer;
        this.canDonate = canDonate;
        this.donorAddress = donorAddress;
        this.donationImageUrl = donationImageUrl;
        this.deliveryImgUrl = deliveryImgUrl;
        this.foodType = foodType;
        this.deliveredOn = deliveredOn;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public String getFoodPreparedOn() {
        return foodPreparedOn;
    }

    public String getAdditionalContactNumber() {
        return additionalContactNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getDonorContactNumber() {
        return donorContactNumber;
    }

    public String getDelivererName() {
        return delivererName;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDelivererContactNumber() {
        return delivererContactNumber;
    }

    public boolean isHasContainer() {
        return hasContainer;
    }

    public boolean isCanDonate() {
        return canDonate;
    }

    public HashMap<String, Object> getDonorAddress() {
        return donorAddress;
    }

    public String getDonationImageUrl() {
        return donationImageUrl;
    }

    public String getDeliveryImgUrl() {
        return deliveryImgUrl;
    }

    public String getFoodType() {
        return foodType;
    }

    public String getDeliveredOn() {
        return deliveredOn;
    }

    public String getDonationUserId() {
        return donationUserId;
    }

    public void setDonationUserId(String donationUserId) {
        this.donationUserId = donationUserId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }
}
