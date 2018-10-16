package com.android.developer.feedingindia.pojos;



public class FeedingIndiaEvent {
    private String eventName;
    private String eventDescription;
    private String imageUrl;
    private Long timeStamp;
    public FeedingIndiaEvent(){}
    public FeedingIndiaEvent(String eventName,String eventDescription,String imageUrl){
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.imageUrl = imageUrl;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }
}
