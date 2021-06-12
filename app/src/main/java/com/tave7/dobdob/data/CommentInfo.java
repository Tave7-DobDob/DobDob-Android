package com.tave7.dobdob.data;

public class CommentInfo {
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

    public UserInfo getCommenterInfo() { return commenterInfo; }
    public String getCommentTime() { return commentTime; }
    //public String getMention() { return mention; }
    public String getContent() { return content; }
}
