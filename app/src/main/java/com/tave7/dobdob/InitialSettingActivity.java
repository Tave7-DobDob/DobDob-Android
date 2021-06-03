package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class InitialSettingActivity extends AppCompatActivity {
    boolean isCheckedName = false, isSetTown = false;
    ConstraintLayout clWhole;
    EditText etName;
    TextView tvNameError, tvTownError, tvResultDong, tvFullAddress;
    LinearLayout llResultTown;
    Button btCheckName, btSelectTown, btSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialsetting);

        clWhole = findViewById(R.id.is_wholeLayout);
        etName = (EditText) findViewById(R.id.is_etName);
        tvNameError = (TextView) findViewById(R.id.is_tvNameError);
            tvNameError.setVisibility(View.GONE);
        btCheckName = (Button) findViewById(R.id.is_btCheckName);
        btSelectTown = (Button) findViewById(R.id.is_btSelectTown);
        tvTownError = (TextView) findViewById(R.id.is_tvTownError);
            tvTownError.setVisibility(View.GONE);
        llResultTown = (LinearLayout) findViewById(R.id.is_llResultTown);
            llResultTown.setVisibility(View.GONE);
        tvResultDong = (TextView) findViewById(R.id.is_tvDong);
        tvFullAddress = (TextView) findViewById(R.id.is_tvFullAddress);
            tvFullAddress.setVisibility(View.GONE);
        btSubmit = (Button) findViewById(R.id.is_btSubmit);
        
        initialSettingClickListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initialSettingClickListener(){
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNameError.setVisibility(View.GONE);
                isCheckedName = false;
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btCheckName.setOnClickListener(v -> {
            if (etName.getText().toString().equals("")) {
                tvNameError.setVisibility(View.VISIBLE);
                tvNameError.setText("닉네임을 입력하지 않았습니다.");
            }

            else {
                //TODO: 데이터베이스에서 닉네임 중복 확인함
                //(중복 확인 후 사용가능하다면 isCheckedName = true;과 tvNameError.setVisibility(View.VISIBLE);)
                isCheckedName = true;
                tvNameError.setVisibility(View.VISIBLE);
                tvNameError.setText("사용 가능한 닉네임입니다.");
            }
        });

        btSelectTown.setOnClickListener(v -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체

            tvTownError.setVisibility(View.GONE);

            //TODO: 주소검색을 함 (각각의 시, 구, 동과 전체 주소를 TextView에 setText함)
            //주소 검색 완료 후
            isSetTown = true;
            llResultTown.setVisibility(View.VISIBLE);
            tvFullAddress.setVisibility(View.VISIBLE);
        });

        btSubmit.setOnClickListener(v -> {
            if (isCheckedName && isSetTown) {
                //TODO: DB에 결과를 보냄 -> 닉네임과 주소!  -->  그 주소를 저장함      (url 이름 바꿔야 함)
                PreferenceManager.setBoolean(InitialSettingActivity.this, "isDidInitialSetting", true);
                PreferenceManager.setString(InitialSettingActivity.this, "userName", etName.getText().toString());
                PreferenceManager.setString(InitialSettingActivity.this, "userTown", tvResultDong.getText().toString());

                //TODO: DB에서 postList 내용 받아와야 함!(이때)
                Intent showMain = new Intent(InitialSettingActivity.this, MainActivity.class);
                startActivity(showMain);
                finish();
            }
            else if (!isCheckedName) {      //닉네임 중복 확인을 하지 않은 경우
                tvNameError.setVisibility(View.VISIBLE);
                tvNameError.setText("닉네임 중복 확인해 주세요.");
            }
            else if (!isSetTown)            //동네 설정을 하지 않은 경우
                tvTownError.setVisibility(View.VISIBLE);
        });
    }
}
