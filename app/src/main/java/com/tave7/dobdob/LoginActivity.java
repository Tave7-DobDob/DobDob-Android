package com.tave7.dobdob;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    boolean isFirstLogin = true;
    Button kakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLogin = (Button) findViewById(R.id.btLogin);
        kakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: 카카오 로그인을 처음했을 때는 isFirstLogin = true (초기 로그인이라면 닉네임과 동네를 설정함)
                //카카오 로그인을 이전에 해봤을 때는 isFirstLogin = false로 User정보를 MainActivity에 전달함
                if (isFirstLogin) {
                    startActivity(new Intent(LoginActivity.this, InitialSettingActivity.class));
                    finish();
                }
                else {
                    Intent showMain = new Intent(LoginActivity.this, MainActivity.class);
                    //showMain.putExtra("userInfo", );      TODO: 로그인한 유저 정보(UserInfo 형식)를 전달해야 함
                    startActivity(showMain);
                    finish();
                }
            }
        });

        checkDangerousPermissions();
    }

    private void checkDangerousPermissions() {      //권한 체크
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        //파일 쓰기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";

        if (TextUtils.isEmpty(temp) == false)
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
