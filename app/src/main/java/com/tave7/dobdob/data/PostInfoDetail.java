package com.tave7.dobdob.data;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class PostInfoDetail {
    PostInfoSimple postInfoSimple;
    private String postContent = "";
    private ArrayList<Bitmap> postPhotos = null;     //사진 저장
    private ArrayList<CommentInfo> comments = null;

    public PostInfoDetail(PostInfoSimple postInfoSimple, String postContent){
        this.postInfoSimple = postInfoSimple;
        this.postContent = postContent;

        postPhotos = new ArrayList<Bitmap>();
        comments = new ArrayList<CommentInfo>();
        //this.comments = comments;
    }

    public PostInfoSimple getPostInfoSimple() { return postInfoSimple; }
    public String getPostContent() { return postContent; }
    public ArrayList<Bitmap> getPostPhotos() { return postPhotos; }
    public ArrayList<CommentInfo> getComments() { return comments; }
}
