package com.tave7.dobdob;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface RetrofitAPI {
    @POST("/auth/kakao")
    Call<String> postKakaoToken(@Body JsonObject kakaoToken);       //서버로 카카오 토큰 전달

    @GET("/user/nickname/{nickname}")
    Call<String> checkExistNick(@Path("nickname") String nickname);                 //서버로부터 해당 id의 포스트를 받음

    @Multipart
    @POST("/post/upload")
    Call<String> postNewPost(@Part ArrayList<MultipartBody.Part> postImage, @PartMap Map<String, RequestBody> data);

    @GET("/post")
    Call<String> getAllPost();                                      //서버로부터 전체 포스트를 받음

    @GET("/post/{id}")
    Call<String> getIDPost(@Path("id") int postID);                 //서버로부터 해당 id의 포스트를 받음

    @PATCH("/post/{id}")
    Call<String> patchIDPost(@Path("id") int postID, @Body JsonObject postData);     //서버로 수정할 포스트를 전달(이미지 제외)

    @DELETE("/post/{id}")
    Call<String> deleteIDPost(@Path("id") int postID);              //서버로 해당 id의 포스트를 삭제하라고 함
}
