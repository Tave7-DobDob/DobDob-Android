package com.tave7.dobdob;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfo implements Serializable {
    private String writerProfile = "";
    private String writerName = "";
    private String writerTown = "";
    private String postTime = "";    //포스트가 올려진 시간
    private String postTitle = "";
    //private String postContent = "";
    //private ArrayList<Bitmap> = null;     //사진 저장
    //TODO: heartNum과 commentNum이 아닌 아래와 같이 ArrayList로 생성해야함
    //private ArrayList<String> heartUsers = null;
    //private ArrayList<CommentInfo> commentUsers = null;
    private int heartNum = 0;
    private int commentNum = 0;
    ArrayList<String> postTag = null;

    PostInfo(String writerProfile, String writerName, String writerTown, String postTime, String postTitle, int heartNum, int commentNum, ArrayList<String> postTag){
        this.writerProfile = writerProfile;
        this.writerName = writerName;
        this.writerTown = writerTown;
        this.postTime = postTime;
        this.postTitle = postTitle;
        //this.postContent = postContent;
        this.heartNum = heartNum;
        this.commentNum = commentNum;
        //this.commentUsers = comments;

        this.postTag = new ArrayList<String>();
        this.postTag = postTag;
    }

    public String getWriterProfile() { return writerProfile; }
    public String getWriterName() { return writerName; }
    public String getWriterTown() { return writerTown; }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    //public String getPostContent() { return postContent; }
    public int getHeartNum() { return heartNum; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }
}
