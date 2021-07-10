package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfoSimple implements Parcelable {
    private int postID;
    private UserInfo writerInfo;
    private String postTime = "";
    private String postTitle;
    private int likeNum = 0;
    private int commentNum = 0;
    private ArrayList<String> postTag = null;

    public PostInfoSimple(int postID, UserInfo writerInfo, String postTime, String postTitle, int likeNum, int commentNum, ArrayList<String> postTag){
        this.postID = postID;
        this.writerInfo = writerInfo;
        this.postTime = postTime;
        this.postTitle = postTitle;

        /* TODO: 삭제 요망!!!!!!!!!!!!!!
        this.heartUsers = new ArrayList<String>();
        if (heartUsers != null)
            this.heartUsers = heartUsers;
         */
        this.likeNum = likeNum;
        this.commentNum = commentNum;

        /*
        this.postTag = new ArrayList<String>();
        if (postTag != null)
         */
            this.postTag = postTag;
    }

    protected PostInfoSimple(Parcel in) {
        postID = in.readInt();
        writerInfo = in.readParcelable(UserInfo.class.getClassLoader());
        postTime = in.readString();
        postTitle = in.readString();
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
    public int getLikeNum() { return likeNum; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }

    public void setPostID(int postID) { this.postID = postID; }
    public void setWriterName(String writerName) { this.writerInfo.setUserName(writerName); }
    public void setWriterTown(String writerTown) { this.writerInfo.setUserTown(writerTown); }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setLikeNum(int likeNum) { this.likeNum = likeNum; }
    public void setCommentNum(int commentNum) { this.commentNum = commentNum; }
    public void setPostTag(ArrayList<String> postTag) { this.postTag = postTag; }       //TODO: 삭제해야 함!!

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postID);
        dest.writeParcelable(writerInfo, flags);
        dest.writeString(postTime);
        dest.writeString(postTitle);
        dest.writeInt(likeNum);
        dest.writeInt(commentNum);
        dest.writeStringList(postTag);
    }
}
