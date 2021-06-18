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
import com.kakao.sdk.user.model.AccessTokenInfo;

import org.json.JSONException;
import org.json.JSONObject;

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

        kakaoLogin = (Button) findViewById(R.id.btLogin);
        kakaoLogin.setEnabled(false);

        //TODO: DB로부터 사용자의 로그인 정보를 받아와 PreferenceManger를 통해 SharedPreferences 값을 업데이트함(userProfile, userName, userTown!)
        //TODO: InitialSettingActivity를 한 사용자 혹은 웹에서 회원가임 폼을 작성한 사용자라면 MainActivity가 바로 보일 수 있도록 함!
        
        /*
        boolean isDidLogin = PreferenceManager.getBoolean(LoginActivity.this, "isDidLogin");        //자동 로그인을 위함(but 순서가 바뀌어야 함)
        
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

        //TODO: 유저 정보 SharedPreferences에 저장해야 함 -> getId()를 통해 회원 id를
        UserApiClient.getInstance().accessTokenInfo(new Function2<AccessTokenInfo, Throwable, Unit>() {     //토큰이 있을 시 자동로그인 가능
            @Override
            public Unit invoke(AccessTokenInfo accessTokenInfo, Throwable throwable) {
                if (accessTokenInfo != null) {
                    Log.i("확인용", "자동 로그인 가능!");
                    //TODO: 자동 로그인 되도록 하자!!!
                    PreferenceManager.setLong(LoginActivity.this, "userID", accessTokenInfo.getId());   //회원 정보 저장->그럼 어디서 get하는가?
                    Log.i("확인용ID", String.valueOf(accessTokenInfo.getId()));    //TODO 서버에서 이거로 로그인한 것이 식별 가능함!!
                    //TODO: 서버로부터 로그인 정보를 받아옴
                } else if (throwable != null) {
                    Log.e("No Token", "Message : " + throwable.getLocalizedMessage());
                }
                kakaoLogin.setEnabled(true);
                return null;
            }
        });

        kakaoLogin.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, kakaoCallback);
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, kakaoCallback);
            }
        });

        checkDangerousPermissions();
    }

    //카카오 로그인 콜백
    Function2<OAuthToken, Throwable, Unit> kakaoCallback = (oAuthToken, throwable) -> {
        if (oAuthToken != null) {
            Log.i("확인용 기본 토큰", oAuthToken.toString());

            JsonObject sendToken = new JsonObject();
            sendToken.addProperty("access_token", oAuthToken.getAccessToken());
            sendToken.addProperty("refresh_token", oAuthToken.getRefreshToken());
            KakaoToken kakaoToken = new KakaoToken(sendToken);

            Call<String> postToken = RetrofitClient.getApiService().postKakaoToken(kakaoToken);
            postToken.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("확인용 연결성공1", response.toString());
                    Log.i("확인용 연결성공2", response.body());
                    if (response.code() == 201) {
                        //TODO: 연결 성공 시 Initial로 감!! -> 웹이랑 연결???!?!!?!
                    }
                    else {
                        //오류 처리
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("확인용 연결실패", t.getMessage());
                }
            });


            /*
            PreferenceManager.setBoolean(this, "isDidLogin", true);
            startActivity(new Intent(this, InitialSettingActivity.class));
            finish();
             */

            //TODO: 이후에 삭제해야 하는 부분!!! 화면 보기 위해 추가된 코드!!
            startActivity(new Intent(this, InitialSettingActivity.class));
            finish();
        }
        else if (throwable != null) {
            Toast.makeText(LoginActivity.this, "다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
        }

        return null;
    };

    public class KakaoToken {
        JsonObject kakaoToken;

        public KakaoToken(JsonObject kakaoToken) {
            this.kakaoToken = kakaoToken;
        }

        public JsonObject getKakaoToken() { return kakaoToken; }
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
