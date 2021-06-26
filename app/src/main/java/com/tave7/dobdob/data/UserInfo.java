package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private int userID;
    private String userProfileUrl;
    private String userName;
    private String userTown;       //XX동 -> 마이페이지 설정 town 혹은 실제 writerTown이 저장될 수 있음
    private String userAddress;

    public UserInfo(int userID, String userProfileUrl, String userName, String userTown, String userAddress) {
        this.userID = userID;
        //this.userProfileUrl = userProfileUrl;       //userProfileUrl값이 null이라면 기본 R.drawable.user_image 사용해야 함
        //TODO: 변경해야 함!!!
        this.userProfileUrl = "https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http%3A%2F%2Fcfile7.uf.tistory.com%2Fimage%2F24283C3858F778CA2EFABE";
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
