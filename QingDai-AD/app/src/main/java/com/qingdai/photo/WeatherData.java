package com.qingdai.photo;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
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
} 