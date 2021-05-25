package com.tave7.dobdob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InitialSettingActivity extends AppCompatActivity {
    boolean isCheckedNick = false, isSetTown = false;
    EditText etName;
    TextView tvNickError, tvTownError, tvFullAddress;
    LinearLayout llResultTown;
    Button btCheckNick, btSelectTown, btSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialsetting);

        etName = (EditText) findViewById(R.id.is_etName);
        tvNickError = (TextView) findViewById(R.id.is_tvNickError);
            tvNickError.setVisibility(View.GONE);
        btCheckNick = (Button) findViewById(R.id.is_btCheckNick);
        btSelectTown = (Button) findViewById(R.id.is_btSelectTown);
        tvTownError = (TextView) findViewById(R.id.is_tvTownError);
            tvTownError.setVisibility(View.GONE);
        llResultTown = (LinearLayout) findViewById(R.id.is_llResultTown);
            llResultTown.setVisibility(View.GONE);
        tvFullAddress = (TextView) findViewById(R.id.is_tvFullAddress);
            tvFullAddress.setVisibility(View.GONE);
        btSubmit = (Button) findViewById(R.id.is_btSubmit);
        
        initialSettingClickListener();
    }

    public void initialSettingClickListener(){
        btCheckNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체

                if (etName.getText().toString().equals("")) {
                    tvNickError.setVisibility(View.VISIBLE);
                    tvNickError.setText("닉네임을 입력하지 않았습니다.");
                }

                else {
                    //TODO: 데이터베이스에서 닉네임 중복 확인함
                    //(중복 확인 후 사용가능하다면 isCheckedNick = true;과 tvNickError.setVisibility(View.VISIBLE);)
                    isCheckedNick = true;
                    tvNickError.setVisibility(View.VISIBLE);
                    tvNickError.setText("사용 가능한 닉네임입니다.");
                }
            }
        });
        
        btSelectTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTownError.setVisibility(View.GONE);

                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
                
                //TODO: 주소검색을 함 (각각의 시, 구, 동과 전체 주소를 TextView에 setText함)
                //주소 검색 완료 후
                isSetTown = true;
                llResultTown.setVisibility(View.VISIBLE);
                tvFullAddress.setVisibility(View.VISIBLE);
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);

                if (isCheckedNick && isSetTown) {
                    startActivity(new Intent(InitialSettingActivity.this, MainActivity.class));
                    finish();
                }
                else if (!isCheckedNick) {      //닉네임 중복 확인을 하지 않은 경우
                    tvNickError.setVisibility(View.VISIBLE);
                    tvNickError.setText("닉네임 중복 확인해 주세요.");
                }
                else if (!isSetTown)            //동네 설정을 하지 않은 경우
                    tvTownError.setVisibility(View.VISIBLE);
            }
        });
    }
}
