package com.qingdai.sunsetapp;

import com.google.gson.annotations.SerializedName;

public class SunsetResponse {
    @SerializedName("img_href")
    private String imgHref;
    
    @SerializedName("img_summary")
    private String imgSummary;
    
    @SerializedName("place_holder")
    private String placeHolder;
    
    @SerializedName("query_id")
    private String queryId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("tb_aod")
    private String tbAod;
    
    @SerializedName("tb_event_time")
    private String tbEventTime;
    
    @SerializedName("tb_quality")
    private String tbQuality;
    
    // Getters
    public String getImgHref() {
        return imgHref;
    }
    
    public String getImgSummary() {
        return imgSummary;
    }
    
    public String getPlaceHolder() {
        return placeHolder;
    }
    
    public String getQueryId() {
        return queryId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getTbAod() {
        return tbAod;
    }
    
    public String getTbEventTime() {
        return tbEventTime;
    }
    
    public String getTbQuality() {
        return tbQuality;
    }
    
    // Helper method to clean HTML tags from text
    public String getCleanImgSummary() {
        if (imgSummary == null) return "";
        return imgSummary.replaceAll("\\<.*?\\>", "");
    }
    
    public String getCleanTbAod() {
        if (tbAod == null) return "";
        return tbAod.replace("<br>", " ");
    }
    
    public String getCleanTbEventTime() {
        if (tbEventTime == null) return "";
        return tbEventTime.replace("<br>", " ");
    }
    
    public String getCleanTbQuality() {
        if (tbQuality == null) return "";
        return tbQuality.replace("<br>", " ");
    }
} 