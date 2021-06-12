package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentInfo implements Parcelable {
    private UserInfo commenterInfo;
    private String commentTime = "";    //포스트가 올려진 시간
    //private ArrayList<String> mention = null;      (멘션된 사용자의 id가 포함되어야 함)
    private String content = "";

    public CommentInfo(UserInfo commenterInfo, String commentTime, String content){
        this.commenterInfo = commenterInfo;
        this.commentTime = commentTime;
        //this.mention = mention;
        this.content = content;
    }

    protected CommentInfo(Parcel in) {
        commenterInfo = in.readParcelable(UserInfo.class.getClassLoader());
        commentTime = in.readString();
        content = in.readString();
    }

    public static final Creator<CommentInfo> CREATOR = new Creator<CommentInfo>() {
        @Override
        public CommentInfo createFromParcel(Parcel in) { return new CommentInfo(in); }

        @Override
        public CommentInfo[] newArray(int size) { return new CommentInfo[size]; }
    };

    public UserInfo getCommenterInfo() { return commenterInfo; }
    public String getCommentTime() { return commentTime; }
    //public String getMention() { return mention; }
    public String getContent() { return content; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(commenterInfo, flags);
        dest.writeString(commentTime);
        dest.writeString(content);
    }
}
