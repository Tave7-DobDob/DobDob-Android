package com.tave7.dobdob;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInfo implements Serializable {
    //TODO: UserInfo 로 writer정보를 바꿔야함!
    private String writerProfile = "";
    private String writerName = "";
    private String writerTown = "";
    private String postTime = "";    //포스트가 올려진 시간
    private String postTitle = "";
    //private String postContent = "";
    //private ArrayList<Bitmap> = null;     //사진 저장
    private ArrayList<String> heartUsers = null;          //사용자 이름들 저장
    //private ArrayList<CommentInfo> comments = null;
    private int commentNum = 0;     //TODO: commentNum이 아닌 아래와 같이 ArrayList로 생성해야함
    ArrayList<String> postTag = null;

    PostInfo(String writerProfile, String writerName, String writerTown, String postTime, String postTitle, ArrayList<String> heartUsers, int commentNum, ArrayList<String> postTag){
        //TODO: 메인에서 보일 친구들만 생성자로 표시하고 이후에는 main에서 DB에 다시 접근해서 최신 정보(Content, 사진, tag) 데려옴!
        this.writerProfile = writerProfile;
        this.writerName = writerName;
        this.writerTown = writerTown;
        this.postTime = postTime;
        this.postTitle = postTitle;
        //this.postContent = postContent;

        this.heartUsers = new ArrayList<String>();
        if (heartUsers != null)
            this.heartUsers = heartUsers;

        this.commentNum = commentNum;
        //this.comments = comments;

        this.postTag = new ArrayList<String>();
        if (postTag != null)
            this.postTag = postTag;
    }

    public String getWriterProfile() { return writerProfile; }
    public String getWriterName() { return writerName; }
    public String getWriterTown() { return writerTown; }
    public String getPostTime() { return postTime; }
    public String getPostTitle() { return postTitle; }
    //public String getPostContent() { return postContent; }
    public ArrayList<String> getHeartUsers() { return heartUsers; }
    public int getCommentNum() { return commentNum; }
    public ArrayList<String> getPostTag() { return postTag; }
}
