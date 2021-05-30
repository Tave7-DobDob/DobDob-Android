package com.tave7.dobdob;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String userProfileUrl = "";
    private String userName = "";
    private String userTown = "";       //XX동

    UserInfo(String userProfileUrl, String userName, String userTown) {
        this.userProfileUrl = userProfileUrl;       //userProfileUrl.length = 0이라면 기본 R.drawable.user_image 사용해야 함
        this.userName = userName;
        this.userTown = userTown;
    }

    public String getUserProfileUrl() { return userProfileUrl; }
    public String getUserName() { return userName; }
    public String getUserTown() { return userTown; }

    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = ""+userProfileUrl; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserTown(String userTown) { this.userTown = userTown; }

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
