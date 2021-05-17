package com.tave7.dobdob;

import java.util.ArrayList;

public class PostInfo {
    private String writerNickName = "";
    private String writerPicture = "";
    private String postTime = "";    //포스트가 올려진 시간
    private String postTitle = "";
    private int heartNum = 0;
    private int commentNum = 0;
    ArrayList<String> postTag = null;

    PostInfo(String writerNickName, String writerPicture, String postTime, String postTitle, int heartNum, int commentNum, ArrayList<String> postTag){
        this.writerNickName = writerNickName;
        this.writerPicture = writerPicture;
        this.postTime = postTime;
        this.postTitle = postTitle;
        this.heartNum = heartNum;
        this.commentNum = commentNum;

        this.postTag = new ArrayList<String>();
        this.postTag = postTag;
    }

    public String getWriterNickName() { return writerNickName; }
    public String getWriterPicture() { return writerPicture; }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public int getHeartNum() { return heartNum; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }
}
