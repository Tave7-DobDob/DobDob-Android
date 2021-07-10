package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private int userID;
    private String userProfileUrl;
    private String userName;
    private String userTown;       //XX동 -> 마이페이지 설정 town 혹은 실제 writerTown이 저장될 수 있음
    private String userAddress = null;

    public UserInfo(int userID, String userProfileUrl, String userName, String userTown) {
        this.userID = userID;
        this.userProfileUrl = userProfileUrl;
        this.userName = userName;
        this.userTown = userTown;
    }

    public UserInfo(int userID, String userProfileUrl, String userName, String userTown, String userAddress) {
        this.userID = userID;
        this.userProfileUrl = userProfileUrl;
        this.userName = userName;
        this.userTown = userTown;
        this.userAddress = userAddress;
    }

    protected UserInfo(Parcel in) {
        userID = in.readInt();
        userProfileUrl = in.readString();
        userName = in.readString();
        userTown = in.readString();
        userAddress = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) { return new UserInfo(in); }

        @Override
        public UserInfo[] newArray(int size) { return new UserInfo[size]; }
    };

    public int getUserID() { return userID; }
    public String getUserProfileUrl() { return userProfileUrl; }
    public String getUserName() { return userName; }
    public String getUserTown() { return userTown; }
    public String getUserAddress() { return userAddress; }

    public void setUserID(int userID) { this.userID = userID; }
    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserTown(String userTown) { this.userTown = userTown; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userID);
        dest.writeString(userProfileUrl);
        dest.writeString(userName);
        dest.writeString(userTown);
        dest.writeString(userAddress);
    }
}
