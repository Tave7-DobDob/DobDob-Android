package com.tave7.dobdob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.exception.KakaoException;

public class SessionCallback implements ISessionCallback {
    Context context;
    Activity activity;

    @Override
    public void onSessionOpened() {     //카카오 계정 로그인에 성공한 상태
        Log.d("kakao 세션", "열림");
        requestMe();
    }

    @Override
    public void onSessionOpenFailed(KakaoException exception) {     //카카오 계정 로그인에 실패한 상태
        Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    public void giveContext(Context context) {   //Context와 Activity 저장
        this.context = context;
        this.activity = ((LoginActivity) context);
    }

    //사용자 정보 요청
    public void requestMe() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("kakao_API", "세션이 닫혀 있음: " + errorResult);
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("kakao_API", "사용자 정보 요청 실패: " + errorResult);
            }

            @Override
            public void onSuccess(MeV2Response result) {    //로그인 성공
                Log.i("kakao_API", "사용자 아이디: " + result.getId());

                UserAccount kakaoAccount = result.getKakaoAccount();
                if (kakaoAccount != null) {
                    String email = kakaoAccount.getEmail(); //카카오 계정
                    if (email != null) {
                        Log.d("kakao_API", "onSuccess:email " + email);

                        //TODO: DB에 전달!!
                        PreferenceManager.setBoolean(context, "isDidLogin", true);
                        context.startActivity(new Intent(context, InitialSettingActivity.class));
                        activity.finish();
                    }
                }
                else{
                    Log.i("kakao_API", "onSuccess: kakaoAccount null");

                    //TODO: 이후에 삭제해야 하는 부분!!! 화면 보기 위해 추가된 코드!!     /* */
                    context.startActivity(new Intent(context, InitialSettingActivity.class));
                    activity.finish();
                    /**/
                }
            }
        });
    }
}
