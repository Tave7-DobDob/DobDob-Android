package com.tave7.dobdob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class InitialSettingActivity extends AppCompatActivity {
    EditText etName;
    Button btCheckNick, btSubmit;
    Spinner spCity, spCountry, spDong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialsetting);

        etName = (EditText) findViewById(R.id.ISetName);
        btCheckNick = (Button) findViewById(R.id.ISbtCheckNick);
        spCity = (Spinner) findViewById(R.id.ISspCityProvince);
        spCountry = (Spinner) findViewById(R.id.ISspCountyDistrict);
        spDong = (Spinner) findViewById(R.id.ISspDong);
        btSubmit = (Button) findViewById(R.id.ISbtSubmit);

        spSetContent();
        btClickListener();
    }

    public void spSetContent(){
        //TODO: 스피너의 내용을 설정함
    }

    public void btClickListener(){
        btCheckNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체

                //TODO: 데이터베이스에서 닉네임 중복 확인함
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);

                //TODO: 닉네임 중복확인하고 내용이 모두 선택되었는 지 확인 후 메인페이지로 넘어감
                startActivity(new Intent(InitialSettingActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
