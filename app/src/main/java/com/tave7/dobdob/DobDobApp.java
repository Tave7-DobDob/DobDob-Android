package com.tave7.dobdob;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class DobDobApp extends Application {        //Kakao SDK를 사용하기 위한 초기화
    private static DobDobApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        KakaoSdk.init(this, getResources().getString(R.string.KAKAO_APP_KEY));
    }
}
