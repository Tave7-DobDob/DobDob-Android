package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PostInfoDetail implements Parcelable {
    private PostInfoSimple postInfoSimple;
    private String postContent = "";
    private ArrayList<String> postImages;
    private ArrayList<CommentInfo> comments;

    public PostInfoDetail(PostInfoSimple postInfoSimple){
        this.postInfoSimple = postInfoSimple;

        postImages = new ArrayList<>();
        comments = new ArrayList<>();
    }

    protected PostInfoDetail(Parcel in) {
        postInfoSimple = in.readParcelable(PostInfoSimple.class.getClassLoader());
        postContent = in.readString();
        postImages = new ArrayList<>();
        postImages = (ArrayList<String>) in.readSerializable();
        comments = new ArrayList<>();
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
    public ArrayList<String> getPostImages() { return postImages; }
    public ArrayList<UserInfo> getLikes() { return getPostInfoSimple().getLikes(); }
    public ArrayList<CommentInfo> getComments() { return comments; }

    public void setPostContent(String postContent) { this.postContent = postContent; }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(postInfoSimple, flags);
        dest.writeString(postContent);
        dest.writeSerializable(postImages);
        dest.writeTypedList(comments);
    }
}
