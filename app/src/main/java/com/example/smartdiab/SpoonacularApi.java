package com.example.smartdiab;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpoonacularApi {

    @GET("recipes/complexSearch")
    Call<ApiResponse> searchMeal(
            @Query("query") String query,
            @Query("apiKey") String apiKey
    );
}