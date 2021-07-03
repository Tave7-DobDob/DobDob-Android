package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonObject;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

@SuppressLint("StaticFieldLeak")
public class InitialSettingActivity extends AppCompatActivity {
    public static final int DAUMADDRESS_REQUEST = 3000;
    private static JsonObject location;
    private int userID = -1;

    private boolean isCheckedName = false;
    private boolean isSetTown = false;
    private ImageView ivGPSPointer;
    private TextView tvResultTown, tvFullAddress;
    private Button btSelectTown;
    private ConstraintLayout clWhole;
    private EditText etName;
    private TextView tvNameError, tvTownError;
    private Button btCheckName, btSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialsetting);

        userID = getIntent().getExtras().getInt("userID");      //TODO: 추후에 변경될 가능성 있음??!?!!

        clWhole = findViewById(R.id.is_wholeLayout);
        etName = findViewById(R.id.is_etName);
        tvNameError = findViewById(R.id.is_tvNameError);
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

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void initialSettingClickListener(){
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isCheckedName = false;
                tvNameError.setTextColor(getColor(R.color.gray));
                tvNameError.setText("\u2713 2자 이상 20자 이하의 영문 소문자/한글(숫자혼합 가능)\n\u2713 공백 및 특수문자 불가");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btCheckName.setOnClickListener(v -> {
            String nickName = etName.getText().toString().trim();
            if (nickName.length() != etName.getText().toString().length()) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("닉네임에 공백이 포함되어 있습니다.");
            }
            else if (nickName.equals("")) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("닉네임을 입력하지 않았습니다.");
            }
            else if (nickName.length()<2 || nickName.length()>20) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("닉네임은 2자 이상 20자 이하여야 합니다.");
            }
            else if (!nickName.matches(".*[a-zㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("닉네임에 영문 소문자 혹은 한글이 1글자 이상 있어야 합니다.");
            }
            else if (nickName.matches(".*[^0-9a-zㄱ-ㅎㅏ-ㅣ가-힣].*")) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("영문 소문자/한글/숫자 이외의 문자는 사용 불가합니다:)");
            }
            else {
                RetrofitClient.getApiService().checkExistNick(nickName).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {   //서버 연결 성공
                            try {
                                JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                                if (result.getBoolean("isExisted")) {
                                    tvNameError.setTextColor(Color.parseColor("#FA5858"));
                                    tvNameError.setText("이미 존재하는 닉네임입니다.");
                                }
                                else {
                                    isCheckedName = true;
                                    tvNameError.setTextColor(Color.parseColor("#00AA7D"));
                                    tvNameError.setText("사용 가능한 닉네임입니다.");
                                }
                            } catch (JSONException e) { e.printStackTrace(); }
                        }
                        else {
                            Toast.makeText(InitialSettingActivity.this, "다시 한번 닉네임 중복 확인해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("Initial 닉중복확인 연결실패", t.getMessage());
                        Toast.makeText(InitialSettingActivity.this, "다시 한번 닉네임 중복 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btSelectTown.setOnClickListener(v -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            tvTownError.setVisibility(View.GONE);

            Intent itAddress = new Intent(InitialSettingActivity.this, DaumAddressActivity.class);  //도로명주소 API 실행
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        btSubmit.setOnClickListener(v -> {
            if (!isCheckedName) {      //닉네임 중복 확인을 하지 않은 경우
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("닉네임 중복 확인해 주세요.");
            }
            else if (!isSetTown)            //동네 설정을 하지 않은 경우
                tvTownError.setVisibility(View.VISIBLE);
            else {  //서버에 유저 정보를 전달함
                String nickName = etName.getText().toString().trim();
                Map<String, RequestBody> dataMap = new HashMap<>();
                dataMap.put("nickName", RequestBody.create(MediaType.parse("text/plain"), nickName));
                dataMap.put("location", RequestBody.create(MediaType.parse("application/json"), String.valueOf(location)));
                RetrofitClient.getApiService().patchUserInfo(userID, null, dataMap).enqueue(new Callback<String>() {       //DB전달
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("Initial 설정성공1", response.toString());
                        Log.i("Initial 설정성공2", response.body());
                        if (response.code() == 200) {
                            //TODO: 현재 위치가 저장이 안되고 있음 확인해야 함!!          --> 창우님이 해결해주셔야 함!!!!
                            //UserId를 넘겨줘야 함!!
                            myInfo = new UserInfo(userID, null, nickName, location.get("dong").getAsString(), location.get("detail").getAsString());
                            startActivity(new Intent(InitialSettingActivity.this, MainActivity.class));
                            finish();
                        }
                        else
                            Toast.makeText(InitialSettingActivity.this, "다시 돕돕 시작하기 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("Initial 설정실패", t.getMessage());
                        Toast.makeText(InitialSettingActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            new GetGEOTask(this, "initial", Objects.requireNonNull(data).getExtras().getString("address")).execute();
        }
    }

    public void initialSettingTown(JsonObject loc) {
        location = loc;

        isSetTown = true;
        ivGPSPointer.setVisibility(View.VISIBLE);
        btSelectTown.setText("주소 재검색");
        tvResultTown.setVisibility(View.VISIBLE);
            tvResultTown.setText(loc.get("dong").getAsString());
        tvFullAddress.setVisibility(View.VISIBLE);
            tvFullAddress.setText(loc.get("detail").getAsString());
    }
}
