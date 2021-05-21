package com.tave7.dobdob;

import java.util.ArrayList;

public class PostInfo {
    private String writerProfile = "";
    private String writerName = "";
    private String postTime = "";    //포스트가 올려진 시간
    private String postTitle = "";
    private int heartNum = 0;
    private int commentNum = 0;
    ArrayList<String> postTag = null;
    //ArrayList<CommentInfo> comments = null;

    PostInfo(String writerProfile, String writerName, String postTime, String postTitle, int heartNum, int commentNum, ArrayList<String> postTag){
        this.writerProfile = writerProfile;
        this.writerName = writerName;
        this.postTime = postTime;
        this.postTitle = postTitle;
        this.heartNum = heartNum;
        this.commentNum = commentNum;

        this.postTag = new ArrayList<String>();
        this.postTag = postTag;
        //this.comments = new ArrayList<CommentInfo>();
        //this.comments = comments;
    }

    public String getWriterProfile() { return writerProfile; }
    public String getWriterName() { return writerName; }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public int getHeartNum() { return heartNum; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }
}
