package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PostInfoSimple implements Parcelable {
    private int postID;
    private UserInfo writerInfo;
    private String postTime;
    private String postTitle;
    private int isILike;    //0이면 false, 1이면 true
    private int likeNum;
    private int commentNum;
    private ArrayList<String> postTag;

    public PostInfoSimple(int postID, UserInfo writerInfo, String postTime, String postTitle, int isILike, int likeNum, int commentNum, ArrayList<String> postTag) {
        this.postID = postID;
        this.writerInfo = writerInfo;
        this.postTime = postTime;
        this.postTitle = postTitle;
        this.isILike = isILike;
        this.likeNum = likeNum;
        this.commentNum = commentNum;
        this.postTag = postTag;
    }

    protected PostInfoSimple(Parcel in) {
        postID = in.readInt();
        writerInfo = in.readParcelable(UserInfo.class.getClassLoader());
        postTime = in.readString();
        postTitle = in.readString();
        isILike = in.readInt();
        likeNum = in.readInt();
        commentNum = in.readInt();
        postTag = in.createStringArrayList();
    }

    public static final Creator<PostInfoSimple> CREATOR = new Creator<PostInfoSimple>() {
        @Override
        public PostInfoSimple createFromParcel(Parcel in) { return new PostInfoSimple(in); }

        @Override
        public PostInfoSimple[] newArray(int size) { return new PostInfoSimple[size]; }
    };

    public int getPostID() { return postID; }
    public UserInfo getWriterInfo() { return writerInfo; }
    public int getWriterID() { return writerInfo.getUserID(); }
    public String getWriterProfileUrl() { return writerInfo.getUserProfileUrl(); }
    public String getWriterName() { return writerInfo.getUserName(); }
    public String getWriterTown() { return writerInfo.getUserTown(); }
    public String getWriterAddress() { return writerInfo.getUserAddress(); }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public int getIsILike() { return isILike; }
    public int getLikeNum() { return likeNum; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }

    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setIsILike(int isILike) { this.isILike = isILike; }
    public void setLikeNum(int likeNum) { this.likeNum = likeNum; }
    public void setCommentNum(int commentNum) { this.commentNum = commentNum; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postID);
        dest.writeParcelable(writerInfo, flags);
        dest.writeString(postTime);
        dest.writeString(postTitle);
        dest.writeInt(isILike);
        dest.writeInt(likeNum);
        dest.writeInt(commentNum);
        dest.writeStringList(postTag);
    }
}
