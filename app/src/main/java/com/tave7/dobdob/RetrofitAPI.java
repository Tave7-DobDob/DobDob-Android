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

    @GET("/user/{id}")
    Call<String> getUserInfo(@Path("id") int userID);               //서버로부터 해당 유저 id의 정보를 받음

    @PATCH("/user/{id}")
    Call<String> patchUserInfo(@Path("id") int userID, @Body JsonObject userData);  //서버로 수정할 유저의 정보를 전달

    @Multipart
    @PATCH("/user/profile/{id}")
    Call<String> patchUserProfileImg(@Path("id") int userID, @Part MultipartBody.Part profileImage);  //서버로 수정할 유저의 프로필 이미지 전달

    @GET("/user/{id}/posts")
    Call<String> getUserPosts(@Path("id") int userID);                  //서버로부터 해당 id의 유저가 포스팅한 글을 받음

    @GET("/user/nickname/{nickname}")
    Call<String> checkExistNick(@Path("nickname") String nickname);     //서버로부터 해당 nickname이 이미 존재하는 지를 확인받음

    @POST("/post/list")
    Call<String> postLocationPost(@Body JsonObject locationData);       //서버로부터 전체 포스트를 받음

    @Multipart
    @POST("/post/upload")
    Call<String> postNewPost(@Part ArrayList<MultipartBody.Part> postImage, @PartMap Map<String, RequestBody> data);

    @GET("/post/{id}")
    Call<String> getIDPost(@Path("id") int postID);                     //서버로부터 해당 id의 포스트를 받음

    @PATCH("/post/{id}")
    Call<String> patchIDPost(@Path("id") int postID, @Body JsonObject postData);     //서버로 수정할 포스트를 전달(이미지 제외)

    @DELETE("/post/{id}")
    Call<String> deleteIDPost(@Path("id") int postID);                  //해당 id의 포스트를 삭제하라고 함

    @POST("/post/list/title")
    Call<String> postTitlePost(@Body JsonObject postInfo);              //서버로부터 해당 제목의 포스트를 받음

    @POST("/post/list/tag")
    Call<String> postTagPost(@Body JsonObject postInfo);                //서버로부터 해당 태그가 속한 포스트를 받음

    @POST("/comment")
    Call<String> postComment(@Body JsonObject kakaoToken);              //서버로 해당 포스트글의 댓글 전달

    @DELETE("/comment/{id}")
    Call<String> deleteIDComment(@Path("id") int commentID);            //해당 id의 댓글을 삭제하라고 함

    @POST("/like")
    Call<String> postLike(@Body JsonObject ids);                        //서버로 해당 포스트글의 좋아요

    @DELETE("/like/{UserId}/{PostId}")
    Call<String> deleteIDLike(@Path("UserId") int userID, @Path("PostId") int postID); //서버로 해당 포스트글의 좋아요취소
}
