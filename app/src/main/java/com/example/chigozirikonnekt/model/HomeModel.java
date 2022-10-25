package com.example.chigozirikonnekt.model;

public class HomeModel {
    private String userName, timeStamp, profileImage, postImage, uid;
    private int likeCount;

    public HomeModel(){

    }
    public HomeModel(String userName, String timeStamp, String profileImage, String postImage, String uid, int likeCount){
        this.userName = userName;
        this.timeStamp = timeStamp;
        this.profileImage = profileImage;
        this.postImage = postImage;
        this.uid = uid;
        this.likeCount = likeCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
