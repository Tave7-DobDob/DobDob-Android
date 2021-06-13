package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PostInfoDetail implements Parcelable {
    PostInfoSimple postInfoSimple;
    private String postContent;
    private ArrayList<byte[]> postPhotos;     //사진 저장
    private ArrayList<CommentInfo> comments;

    public PostInfoDetail(PostInfoSimple postInfoSimple, String postContent){
        this.postInfoSimple = postInfoSimple;
        this.postContent = postContent;

        postPhotos = new ArrayList<byte[]>();
        comments = new ArrayList<CommentInfo>();
        //this.comments = comments;
    }

    protected PostInfoDetail(Parcel in) {
        postInfoSimple = in.readParcelable(PostInfoSimple.class.getClassLoader());
        postContent = in.readString();
        postPhotos = new ArrayList<byte[]>();
        postPhotos = (ArrayList<byte[]>) in.readSerializable();
        comments = new ArrayList<CommentInfo>();
        in.readTypedList(comments, CommentInfo.CREATOR);
    }


    public static final Creator<PostInfoDetail> CREATOR = new Creator<PostInfoDetail>() {
        @Override
        public PostInfoDetail createFromParcel(Parcel in) {
            return new PostInfoDetail(in);
        }

        @Override
        public PostInfoDetail[] newArray(int size) {
            return new PostInfoDetail[size];
        }
    };

    public PostInfoSimple getPostInfoSimple() { return postInfoSimple; }
    public String getPostContent() { return postContent; }
    public ArrayList<byte[]> getPostPhotos() { return postPhotos; }
    public ArrayList<CommentInfo> getComments() { return comments; }

    public void setPostContent(String postContent) { this.postContent = postContent; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(postInfoSimple, flags);
        dest.writeString(postContent);
        dest.writeSerializable(postPhotos);
        dest.writeTypedList(comments);
    }
}
