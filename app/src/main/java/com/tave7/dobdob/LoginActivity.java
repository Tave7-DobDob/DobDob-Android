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

        if (!PreferenceManager.getString(LoginActivity.this, "jwt").equals("")) {
            RetrofitClient.getApiService().postAutoLogin(PreferenceManager.getString(LoginActivity.this, "jwt")).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200) {
                        try {
                            JSONObject loginInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            int userID = loginInfo.getJSONObject("user").getInt("id");

                            if (loginInfo.getJSONObject("user").getString("nickName").equals("")) {
                                Intent showIS = new Intent(LoginActivity.this, InitialSettingActivity.class);
                                Bundle bundle = new Bundle();
                                    bundle.putInt("userID", userID);
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
                    else if (response.code() == 419) {
                        kakaoLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.removeKey(LoginActivity.this, "jwt");
                    }
                    else {
                        kakaoLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    kakaoLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "????????? ???????????? ???????????????. ????????? ?????????:)", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            kakaoLogin.setEnabled(true);

        kakaoLogin.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this))
                UserApiClient.getInstance().loginWithKakaoTalk(this, kakaoCallback);
            else
                UserApiClient.getInstance().loginWithKakaoAccount(this, kakaoCallback);
        });

        checkDangerousPermissions();
    }

    //????????? ????????? ??????
    Function2<OAuthToken, Throwable, Unit> kakaoCallback = (oAuthToken, throwable) -> {
        if (oAuthToken != null) {
            JsonObject kakaoToken = new JsonObject();
            kakaoToken.addProperty("access_token", oAuthToken.getAccessToken());
            RetrofitClient.getApiService().postLogin(kakaoToken).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200) {
                        try {
                            JSONObject loginInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            int userID = loginInfo.getJSONObject("user").getInt("id");
                            PreferenceManager.setString(LoginActivity.this, "jwt", loginInfo.getString("jwt"));

                            if (loginInfo.getJSONObject("user").getString("nickName").equals("")) {
                                Intent showIS = new Intent(LoginActivity.this, InitialSettingActivity.class);
                                Bundle bundle = new Bundle();
                                    bundle.putInt("userID", userID);
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
                    else if (response.code() == 201) {
                        try {
                            JSONObject loginInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            int userID = loginInfo.getJSONObject("user").getInt("id");

                            Intent showIS = new Intent(LoginActivity.this, InitialSettingActivity.class);
                            Bundle bundle = new Bundle();
                                bundle.putInt("userID", userID);
                            showIS.putExtras(bundle);
                            startActivity(showIS);
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else
                        Toast.makeText(LoginActivity.this, "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, "????????? ???????????? ???????????????. ????????? ?????????:)", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (throwable != null) {
            Toast.makeText(LoginActivity.this, "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
        }

        return null;
    };

    private void checkDangerousPermissions() {
        String temp = "";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";

        if (!TextUtils.isEmpty(temp))
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);
        else
            Log.i("permissions", "????????? ?????? ??????");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int i=0; i<permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    Log.i("permissions", permissions[i]+"????????? ?????????");
                else
                    Log.i("permissions", permissions[i]+"????????? ???????????? ??????");
            }
        }
    }
}
