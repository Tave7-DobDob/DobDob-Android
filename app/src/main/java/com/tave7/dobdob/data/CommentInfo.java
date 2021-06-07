package com.tave7.dobdob.data;

public class CommentInfo {
    private String commenterProfile = "";           //TODO: UserInfo와 일치해야 함
    private String commenterName = "";
    private String commenterTown = "";
    private String commentTime = "";    //포스트가 올려진 시간
    //private ArrayList<String> mention = null;      (멘션된 사용자의 id가 포함되어야 함)
    private String content = "";

    public CommentInfo(String commenterProfile, String commenterName, String commenterTown, String commentTime, String content){
        this.commenterProfile = commenterProfile;
        this.commenterName = commenterName;
        this.commenterTown = commenterTown;
        this.commentTime = commentTime;
        //this.mention = mention;
        this.content = content;
    }

    public String getCommenterProfile() { return commenterProfile; }
    public String getCommenterName() { return commenterName; }
    public String getCommenterTown() { return commenterTown; }
    public String getCommentTime() { return commentTime; }
    //public String getMention() { return mention; }
    public String getContent() { return content; }
}
