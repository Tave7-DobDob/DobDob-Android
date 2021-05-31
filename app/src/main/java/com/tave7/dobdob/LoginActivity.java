package com.tave7.dobdob;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

public class LoginActivity extends AppCompatActivity {
    Button kakaoLogin;
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean isDidLogin = PreferenceManager.getBoolean(LoginActivity.this, "isDidLogin");
        /*
        if (isDidLogin) {      //이전에 로그인을 한 기록이 있는 경우  TODO: 7일 뒤라면 자동로그인 해제!!!!
            //TODO: 로그인을 했는데 초기 설정을 하지 않은 경우에는 그냥 다시 로그인하도록 할 것인가?  --> 그러면 여기서 DB에 저장하면 안됨!
            if (!PreferenceManager.getBoolean(LoginActivity.this, "isDidInitialSetting")) {      //로그인 이후 초기 설정을 하지 않은 경우
                startActivity(new Intent(LoginActivity.this, InitialSettingActivity.class));
                finish();
            }
            else {  //초기 설정까지 마친 사용자인 경우
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                //TODO: DB에서 postList 내용 받아와야 함!(이때)
            }
        }
         */

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        kakaoLogin = (Button) findViewById(R.id.btLogin);
        kakaoLogin.setOnClickListener(v -> {
            sessionCallback.giveContext(LoginActivity.this, LoginActivity.this);

            if (Session.getCurrentSession().checkAndImplicitOpen()) {
                Log.d("kakao", "onClick: 로그인 세션 살아있음");

                //sessionCallback.requestMe();   TODO: 없어야 됨! 아니면 중복됨!! 추후에 확인!! -> SessionCallback을 그냥 호출함(onSuccess)
            }
            else {
                Log.d("kakao", "onClick: 로그인 세션 끝남");

                session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);     //카카오 웹뷰가 보임
            }
        });

        checkDangerousPermissions();
    }

    @Override
    protected void onDestroy() {
        Session.getCurrentSession().removeCallback(sessionCallback);        //세션 콜백 삭제(TODO: 추후에 로그아웃 시 사용)

        super.onDestroy();
    }

    private void checkDangerousPermissions() {      //권한 체크
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        //파일 쓰기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";

        if (!TextUtils.isEmpty(temp))
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);      //권한 요청
        else    //모두 허용 상태
            Log.i("permissions", "권한을 모두 허용");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int i=0; i<permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    Log.i("permissions", permissions[i]+"권한이 승인됨");
                else
                    Log.i("permissions", permissions[i]+"권한이 승인되지 않음");
            }
        }
    }
}
