package com.example.smartdiab;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SmartDiabApi {
    @Multipart
    @POST("analyze-meal")
    Call<ResponseBody> analyzeMeal(
            @Part("diabete_type") RequestBody type,
            @Part MultipartBody.Part file
    );

    @FormUrlEncoded
    @POST("ask-ai")
    Call<ResponseBody> askAi(
            @Field("question") String question,
            @Field("diabete_type") String type
    );
}