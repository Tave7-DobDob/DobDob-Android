package com.tave7.dobdob;

public class CommentInfo {
    private String commenterProfile = "";
    private String commenterName = "";
    private String commenterTown = "";
    private String commentTime = "";    //포스트가 올려진 시간
    private String mention = "";
    private String content = "";

    CommentInfo(String commenterProfile, String commenterName, String commenterTown, String commentTime, String mention, String content){
        this.commenterProfile = commenterProfile;
        this.commenterName = commenterName;
        this.commenterTown = commenterTown;
        this.commentTime = commentTime;
        this.mention = mention;
        this.content = content;
    }

    public String getCommenterProfile() { return commenterProfile; }
    public String getCommenterName() { return commenterName; }
    public String getCommenterTown() { return commenterTown; }
    public String getCommentTime() { return commentTime; }
    public String getMention() { return mention; }
    public String getContent() { return content; }
}
