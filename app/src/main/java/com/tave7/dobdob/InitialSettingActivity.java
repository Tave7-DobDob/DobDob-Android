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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@SuppressLint("StaticFieldLeak")
public class InitialSettingActivity extends AppCompatActivity {
    public static final int DAUMADDRESS_REQUEST = 3000;
    private static JsonObject location;

    private boolean isCheckedName = false;
    private static boolean isSetTown = false;
    private static ImageView ivGPSPointer;
    private static TextView tvResultTown, tvFullAddress;
    private static Button btSelectTown;
    private ConstraintLayout clWhole;
    private EditText etName;
    private TextView tvNameError, tvTownError;
    private Button btCheckName, btSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialsetting);

        clWhole = findViewById(R.id.is_wholeLayout);
        etName = findViewById(R.id.is_etName);
        tvNameError = findViewById(R.id.is_tvNameError);
            tvNameError.setVisibility(View.GONE);
        btCheckName = findViewById(R.id.is_btCheckName);
        btSelectTown = findViewById(R.id.is_btSelectTown);
        tvTownError = findViewById(R.id.is_tvTownError);
            tvTownError.setVisibility(View.GONE);
        ivGPSPointer = findViewById(R.id.is_ivGPS);
            ivGPSPointer.setVisibility(View.GONE);
        tvResultTown = findViewById(R.id.is_tvTown);
            tvResultTown.setVisibility(View.GONE);
        tvFullAddress = findViewById(R.id.is_tvFullAddress);
            tvFullAddress.setVisibility(View.GONE);
        btSubmit = findViewById(R.id.is_btSubmit);
        
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNameError.setVisibility(View.GONE);
                isCheckedName = false;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
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

            Intent itAddress = new Intent(InitialSettingActivity.this, DaumAddressActivity.class);  //도로명주소 API 실행
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        btSubmit.setOnClickListener(v -> {
            if (isCheckedName && isSetTown) {
                //TODO: DB에 결과를 보냄 -> 닉네임과 주소!  -->  그 주소를 저장함      (url 이름 바꿔야 함)
                PreferenceManager.setBoolean(InitialSettingActivity.this, "isDidInitialSetting", true);
                PreferenceManager.setString(InitialSettingActivity.this, "userName", etName.getText().toString());
                PreferenceManager.setString(InitialSettingActivity.this, "userTown", tvResultTown.getText().toString());
                PreferenceManager.setString(InitialSettingActivity.this, "userAddress", tvFullAddress.getText().toString());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            try {
                new GetGEOTask(this, "initial", Objects.requireNonNull(data).getExtras().getString("address")).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        }
    }

    public static void initialSettingTown(JsonObject loc) {
        location = loc;

        isSetTown = true;
        ivGPSPointer.setVisibility(View.VISIBLE);
        tvResultTown.setVisibility(View.VISIBLE);
            tvResultTown.setText(loc.get("dong").getAsString());
        tvFullAddress.setVisibility(View.VISIBLE);
            tvFullAddress.setText(loc.get("fullAddress").getAsString());
        btSelectTown.setText("주소 재검색");
    }
}
