package com.qingdai.sunsetapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/")
    Call<SunsetResponse> getSunsetData(
        @Query("query_id") String queryId,
        @Query("intend") String intend,
        @Query("query_city") String queryCity,
        @Query("event_date") String eventDate,
        @Query("event") String event,
        @Query("times") String times
    );
} 