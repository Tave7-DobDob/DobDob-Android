package com.tave7.dobdob.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private byte[] userProfileUrl; //TODO: 추후에 Uri로 변경해야 함!
    private String userName;
    private String userTown;       //XX동 -> 마이페이지 설정 town 혹은 실제 writerTown이 저장될 수 있음

    public UserInfo(byte[] userProfileUrl, String userName, String userTown) {
        this.userProfileUrl = userProfileUrl;       //userProfileUrl값이 null이라면 기본 R.drawable.user_image 사용해야 함
        this.userName = userName;
        this.userTown = userTown;
    }

    protected UserInfo(Parcel in) {
        userProfileUrl = in.createByteArray();
        userName = in.readString();
        userTown = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) { return new UserInfo(in); }

        @Override
        public UserInfo[] newArray(int size) { return new UserInfo[size]; }
    };

    public byte[] getUserProfileUrl() { return userProfileUrl; }
    public String getUserName() { return userName; }
    public String getUserTown() { return userTown; }

    public void setUserProfileUrl(byte[] userProfileUrl) { this.userProfileUrl = userProfileUrl; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserTown(String userTown) { this.userTown = userTown; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(userProfileUrl);
        dest.writeString(userName);
        dest.writeString(userTown);
    }

    /*
    public Bitmap getBitmapProfile() {      //TODO: Bitmap 가능한지 확인하기(스레드로 만들어야 함)
        URL imgUrl = null;
        HttpURLConnection connection = null;
        Bitmap retBitmap = null;

        try {
            imgUrl = new URL(userProfileUrl);
            connection = (HttpURLConnection) imgUrl.openConnection();
            connection.setDoInput(true);        //url로 input받는 flag 허용
            connection.connect();

            retBitmap = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection!=null)
                connection.disconnect();

            return retBitmap;
        }
    }
    
     */
}