package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfoSimple implements Parcelable {
    private int postID = -1;     //TODO: 넣어야 함!!
    private UserInfo writerInfo;
    private String postTime = "";                   //포스트가 올려진 시간(TODO: Date로 바뀌어야 함!)
    private String postTitle = "";
    private ArrayList<String> heartUsers = null;    //사용자 이름들 저장
    private int commentNum = 0;
    private ArrayList<String> postTag = null;

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
        postID = in.readInt();      //TODO: 확인해야함!!!(있어야 하는 건가?)
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

    public int getPostID() { return postID; }
    public UserInfo getWriterInfo() { return writerInfo; }
    public int getWriterID() { return writerInfo.getUserID(); }
    public byte[] getWriterProfileUrl() { return writerInfo.getUserProfileUrl(); }
    public String getWriterName() { return writerInfo.getUserName(); }
    public String getWriterTown() { return writerInfo.getUserTown(); }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public ArrayList<String> getHeartUsers() { return heartUsers; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }

    public void setPostID(int postID) { this.postID = postID; }
    public void setWriterName(String writerName) { this.writerInfo.setUserName(writerName); }
    public void setWriterTown(String writerTown) { this.writerInfo.setUserTown(writerTown); }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setPostTag(ArrayList<String> postTag) { this.postTag = postTag; }       //TODO: 삭제해야 함!!

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postID);
        dest.writeParcelable(writerInfo, flags);
        dest.writeString(postTime);
        dest.writeString(postTitle);
        dest.writeStringList(heartUsers);
        dest.writeInt(commentNum);
        dest.writeStringList(postTag);
    }
}
