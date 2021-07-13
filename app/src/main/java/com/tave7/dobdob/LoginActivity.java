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
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class LoginActivity extends AppCompatActivity {
    Button kakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLogin = findViewById(R.id.btLogin);
        kakaoLogin.setEnabled(false);
        
        //  -> userID를 통해 검색을 한 후에 닉네임이 없으면 넘기고(InitialSet로) 아니면 Main으로 넘어감(UserInfo를 전달해야 함!)
        if (PreferenceManager.getInt(LoginActivity.this, "userID") != -1) {      //토큰이 있을 시 자동로그인 가능
            RetrofitClient.getApiService().getUserInfo(PreferenceManager.getInt(LoginActivity.this, "userID")).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("LoginA user정보받기 성공", response.body());
                    if (response.code() == 200) {
                        try {
                            JSONObject loginInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            int userID = loginInfo.getJSONObject("user").getInt("id");
                            PreferenceManager.setInt(LoginActivity.this, "userID", userID);

                            if (loginInfo.getJSONObject("user").getString("nickName").equals("")) {
                                Intent showIS = new Intent(LoginActivity.this, InitialSettingActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putInt("userID", userID);        //TODO: 추후에 변경될 가능성 있음??!?!!
                                showIS.putExtras(bundle);
                                startActivity(showIS);
                            }
                            else {
                                JSONObject user = loginInfo.getJSONObject("user");
                                if (user.isNull("profileUrl"))
                                    myInfo = new UserInfo(userID, null, user.getString("nickName"),
                                            user.getJSONObject("Location").getString("dong"), user.getJSONObject("Location").getString("detail"),
                                            user.getJSONObject("Location").getDouble("locationX"), user.getJSONObject("Location").getDouble("locationY"));
                                else
                                    myInfo = new UserInfo(userID, user.getString("profileUrl"), user.getString("nickName"),
                                            user.getJSONObject("Location").getString("dong"), user.getJSONObject("Location").getString("detail"),
                                            user.getJSONObject("Location").getDouble("locationX"), user.getJSONObject("Location").getDouble("locationY"));

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                            finish();
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            kakaoLogin.setEnabled(true);    //로그인을 해야 함!

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
            JsonObject kakaoToken = new JsonObject();
            kakaoToken.addProperty("access_token", oAuthToken.getAccessToken());
            RetrofitClient.getApiService().postKakaoToken(kakaoToken).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("Login 연결 성공", response.body());
                    if (response.code() == 200 || response.code() == 201) {
                        try {
                            JSONObject loginInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            int userID = loginInfo.getJSONObject("user").getInt("id");
                            PreferenceManager.setInt(LoginActivity.this, "userID", userID);

                            if (loginInfo.getJSONObject("user").getString("nickName").equals("")) {
                                Intent showIS = new Intent(LoginActivity.this, InitialSettingActivity.class);
                                Bundle bundle = new Bundle();
                                    bundle.putInt("userID", userID);        //TODO: 추후에 변경될 가능성 있음??!?!!
                                showIS.putExtras(bundle);
                                startActivity(showIS);
                            }
                            else {
                                JSONObject user = loginInfo.getJSONObject("user");
                                if (user.isNull("profileUrl"))
                                    myInfo = new UserInfo(userID, null, user.getString("nickName"),
                                            user.getJSONObject("Location").getString("dong"), user.getJSONObject("Location").getString("detail"),
                                            user.getJSONObject("Location").getDouble("locationX"), user.getJSONObject("Location").getDouble("locationY"));
                                else
                                    myInfo = new UserInfo(userID, user.getString("profileUrl"), user.getString("nickName"),
                                            user.getJSONObject("Location").getString("dong"), user.getJSONObject("Location").getString("detail"),
                                            user.getJSONObject("Location").getDouble("locationX"), user.getJSONObject("Location").getDouble("locationY"));

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                            finish();
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else
                        Toast.makeText(LoginActivity.this, "다시 로그인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";

        if (!TextUtils.isEmpty(temp))
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);
        else
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
