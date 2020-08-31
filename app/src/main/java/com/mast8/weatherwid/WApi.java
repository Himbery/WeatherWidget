package com.mast8.weatherwid;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface WApi {
    @GET("/data/2.5/weather?units=metric")
    Call<WeatherList> call(@Query("q") String city, @Query("appid") String id, @Query("lang") String lang);
}
