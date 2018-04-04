package com.example.naimish.assignment.executor;

import com.example.naimish.assignment.Constants.AppConstants;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("/v2.2/tracks/top?apikey=" + AppConstants.API_KEY)
    Call<ApiResponse> getTopTracks();
}
