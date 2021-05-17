package com.tave7.dobdob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    Button kakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLogin = findViewById(R.id.btLogin);

        kakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: 카카오 로그인 성공 시 (초기 로그인이라면 닉네임과 동네를 설정함)
                startActivity(new Intent(LoginActivity.this, InitialSettingActivity.class));
                finish();
            }
        });

    }
}
