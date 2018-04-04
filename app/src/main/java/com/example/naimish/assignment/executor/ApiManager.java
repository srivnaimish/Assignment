package com.example.naimish.assignment.executor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.naimish.assignment.Constants.AppConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private APIListener apiListener;

    public ApiManager(Context context){
        this.apiListener= (APIListener) context;
    }

    public void fetchTrackList(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getTopTracks().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    assert apiResponse != null;

                    apiListener.onApiResultsFetch(apiResponse.getTracks());
                } else {
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.d("Error",t.getLocalizedMessage());
            }
        });
    }

}
