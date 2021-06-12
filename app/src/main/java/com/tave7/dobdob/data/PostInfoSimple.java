package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfoSimple implements Parcelable {
    private UserInfo writerInfo;
    private String postTime = "";                   //포스트가 올려진 시간
    private String postTitle = "";
    private ArrayList<String> heartUsers = null;    //사용자 이름들 저장
    private int commentNum = 0;
    ArrayList<String> postTag = null;

    public PostInfoSimple(UserInfo writerInfo, String postTime, String postTitle, ArrayList<String> heartUsers, int commentNum, ArrayList<String> postTag){
        this.writerInfo = writerInfo;
        this.postTime = postTime;
        this.postTitle = postTitle;

        this.heartUsers = new ArrayList<String>();
        if (heartUsers != null)
            this.heartUsers = heartUsers;

        this.commentNum = commentNum;

        this.postTag = new ArrayList<String>();
        if (postTag != null)
            this.postTag = postTag;
    }

    protected PostInfoSimple(Parcel in) {
        writerInfo = in.readParcelable(UserInfo.class.getClassLoader());
        postTime = in.readString();
        postTitle = in.readString();
        heartUsers = in.createStringArrayList();
        commentNum = in.readInt();
        postTag = in.createStringArrayList();
    }

    public static final Creator<PostInfoSimple> CREATOR = new Creator<PostInfoSimple>() {
        @Override
        public PostInfoSimple createFromParcel(Parcel in) { return new PostInfoSimple(in); }

        @Override
        public PostInfoSimple[] newArray(int size) { return new PostInfoSimple[size]; }
    };

    public UserInfo getWriterInfo() { return writerInfo; }
    public byte[] getWriterProfileUrl() { return writerInfo.getUserProfileUrl(); }
    public String getWriterName() { return writerInfo.getUserName(); }
    public String getWriterTown() { return writerInfo.getUserTown(); }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public ArrayList<String> getHeartUsers() { return heartUsers; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }

    public void setWriterName(String writerName) { this.writerInfo.setUserName(writerName); }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(writerInfo, flags);
        dest.writeString(postTime);
        dest.writeString(postTitle);
        dest.writeStringList(heartUsers);
        dest.writeInt(commentNum);
        dest.writeStringList(postTag);
    }
}
