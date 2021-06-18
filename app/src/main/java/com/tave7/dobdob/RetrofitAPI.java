package com.tave7.dobdob;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

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

    @Multipart
    @POST("/post/upload")
    Call<String> postNewPost(@Part ArrayList<MultipartBody.Part> postImage, @PartMap Map<String, RequestBody> data);

}
