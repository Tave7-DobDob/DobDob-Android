package com.tave7.dobdob.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private int userID;
    private String userProfileUrl;
    private Bitmap userProfileBM = null;
    private String userName;
    private String userTown = "";
    private String userAddress = null;
    private double locationX = -1;
    private double locationY = -1;

    public UserInfo(int userID, String userProfileUrl, String userName) {      //좋아요에서 활용
        this.userID = userID;
        this.userProfileUrl = userProfileUrl;
        this.userName = userName;
    }

    public UserInfo(int userID, String userProfileUrl, String userName, String userTown) {
        this.userID = userID;
        this.userProfileUrl = userProfileUrl;
        this.userName = userName;
        this.userTown = userTown;
    }

    public UserInfo(int userID, String userProfileUrl, String userName, String userTown, String userAddress, double locationX, double locationY) {
        this.userID = userID;
        this.userProfileUrl = userProfileUrl;
        this.userName = userName;
        this.userTown = userTown;
        this.userAddress = userAddress;
        this.locationX = locationX;
        this.locationY = locationY;
    }

    protected UserInfo(Parcel in) {
        userID = in.readInt();
        userProfileUrl = in.readString();
        userName = in.readString();
        userTown = in.readString();
        userAddress = in.readString();
        locationX = in.readDouble();
        locationY = in.readDouble();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) { return new UserInfo(in); }

        @Override
        public UserInfo[] newArray(int size) { return new UserInfo[size]; }
    };

    public int getUserID() { return userID; }
    public String getUserProfileUrl() { return userProfileUrl; }
    public Bitmap getUserProfileBM() { return userProfileBM; }
    public String getUserName() { return userName; }
    public String getUserTown() { return userTown; }
    public String getUserAddress() { return userAddress; }
    public double getLocationX() { return locationX; }
    public double getLocationY() { return locationY; }

    public void setUserID(int userID) { this.userID = userID; }
    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }
    public void setUserProfileBM(Bitmap userProfileBM) { this.userProfileBM = userProfileBM; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserTown(String userTown) { this.userTown = userTown; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
    public void setLocationX(double locationX) { this.locationX = locationX; }
    public void setLocationY(double locationY) { this.locationY = locationY; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userID);
        dest.writeString(userProfileUrl);
        dest.writeString(userName);
        dest.writeString(userTown);
        dest.writeString(userAddress);
        dest.writeDouble(locationX);
        dest.writeDouble(locationY);
    }
}
