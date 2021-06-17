package com.tave7.dobdob;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @GET("/")
    Call<String> getServerString();     //test

    /*
    @FormUrlEncoded
    @POST("/auth/kakao")
    Call<String> postKakaoToken(@Field("kakaoToken") JSONObject kakaoToken);      //서버로 카카오 토큰 전달

     */

    @POST("/auth/kakao")
    Call<String> postKakaoToken(@Body LoginActivity.KakaoToken kakaoToken);      //서버로 카카오 토큰 전달
}
