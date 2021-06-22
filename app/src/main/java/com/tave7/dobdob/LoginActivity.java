package com.tave7.dobdob;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    Button kakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLogin = findViewById(R.id.btLogin);
        kakaoLogin.setEnabled(false);

        //TODO: InitialSettingActivity를 한 사용자 혹은 웹에서 회원가임 폼을 작성한 사용자라면 MainActivity가 바로 보일 수 있도록 함!
        if (!PreferenceManager.getString(LoginActivity.this, "access_token").equals("")) {      //토큰이 있을 시 자동로그인 가능
            Log.i("Login 자동로그인", "가능");
            //서버로부터 사용자 정보와, post글들을 받아옴
            //TODO: 이후에 삭제해야 하는 부분!!! 화면 보기 위해 추가된 코드!!
            startActivity(new Intent(this, InitialSettingActivity.class));
            finish();

            /*
            startActivity(new Intent(this, MainActivity.class));
            finish();
             */
        }
        else {
            Log.i("Login 자동로그인", "불가능");
            kakaoLogin.setEnabled(true);    //로그인을 해야 함!
        }

        kakaoLogin.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this))
                UserApiClient.getInstance().loginWithKakaoTalk(this, kakaoCallback);
            else
                UserApiClient.getInstance().loginWithKakaoAccount(this, kakaoCallback);
        });

        checkDangerousPermissions();
    }

    //카카오 로그인 콜백
    Function2<OAuthToken, Throwable, Unit> kakaoCallback = (oAuthToken, throwable) -> {
        if (oAuthToken != null) {
            Log.i("Login 기본 토큰", oAuthToken.toString());

            JsonObject kakaoToken = new JsonObject();
            kakaoToken.addProperty("access_token", oAuthToken.getAccessToken());
            RetrofitClient.getApiService().postKakaoToken(kakaoToken).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("Login 연결성공1", response.toString());
                    Log.i("Login 연결성공2", response.body());
                    if (response.code() == 201) {
                        PreferenceManager.setString(LoginActivity.this, "access_token", oAuthToken.getAccessToken());
                        //TODO: 연결 성공 시 Initial로 감!! -> 웹이랑 연결???!?!!?!
                        //서버로부터 초기 세팅을 해야하는 지 값을 받아와야 함
                        startActivity(new Intent(LoginActivity.this, InitialSettingActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("Login 연결실패", t.getMessage());
                    Toast.makeText(LoginActivity.this, "서버에 연결이 되지 않았습니다.\n 다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (throwable != null) {
            Toast.makeText(LoginActivity.this, "다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
        }

        return null;
    };

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
