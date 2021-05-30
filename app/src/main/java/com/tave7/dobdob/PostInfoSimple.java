package com.tave7.dobdob;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfoSimple implements Serializable {
    private String writerProfile = "";      //TODO: UserInfo 로 writer정보를 바꿔야 함!
    private String writerName = "";
    private String writerTown = "";
    private String postTime = "";                   //포스트가 올려진 시간
    private String postTitle = "";
    private ArrayList<String> heartUsers = null;    //사용자 이름들 저장
    private int commentNum = 0;
    ArrayList<String> postTag = null;

    PostInfoSimple(String writerProfile, String writerName, String writerTown, String postTime, String postTitle, ArrayList<String> heartUsers, int commentNum, ArrayList<String> postTag){
        this.writerProfile = writerProfile;
        this.writerName = writerName;
        this.writerTown = writerTown;
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

    public String getWriterProfile() { return writerProfile; }
    public String getWriterName() { return writerName; }
    public String getWriterTown() { return writerTown; }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    public ArrayList<String> getHeartUsers() { return heartUsers; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }

    public void setWriterName(String writerName) { this.writerName = writerName; }
}
