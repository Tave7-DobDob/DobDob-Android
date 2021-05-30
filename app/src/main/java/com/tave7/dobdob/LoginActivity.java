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
    Button kakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean isDidLogin = PreferenceManager.getBoolean(LoginActivity.this, "isDidLogin");
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

        kakaoLogin = (Button) findViewById(R.id.btLogin);
        kakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //카카오 로그인을 해본 적이 없는 사용자가 로그인 클릭 시
                //TODO: 카카오 로그인 완료 후 액티비티 넘김!
                PreferenceManager.setBoolean(LoginActivity.this, "isDidLogin", true);
                startActivity(new Intent(LoginActivity.this, InitialSettingActivity.class));
                finish();
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
