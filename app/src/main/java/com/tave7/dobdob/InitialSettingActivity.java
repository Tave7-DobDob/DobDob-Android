package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.Objects;

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

        userID = getIntent().getExtras().getInt("userID");

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

    @SuppressLint("ClickableViewAccessibility")
    public void initialSettingClickListener(){
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();

            return false;
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isCheckedName = false;
                tvNameError.setTextColor(getColor(R.color.gray));
                tvNameError.setText(R.string.initialsetting_name_rule);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btCheckName.setOnClickListener(v -> {
            String nickName = etName.getText().toString().trim();
            if (nickName.length() != etName.getText().toString().length()) {
                tvNameError.setText(R.string.nickname_rule1);
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (nickName.equals("")) {
                tvNameError.setText(R.string.nickname_rule2);
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (nickName.length()<2 || nickName.length()>20) {
                tvNameError.setText(R.string.nickname_rule3);
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (!nickName.matches(".*[a-z???-??????-??????-???]+.*")) {
                tvNameError.setText(R.string.nickname_rule4);
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (nickName.matches(".*[^0-9a-z???-??????-??????-???].*")) {
                tvNameError.setText(R.string.nickname_rule5);
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
            }
            else {
                RetrofitClient.getApiService().checkExistNick(PreferenceManager.getString(this, "jwt"), nickName).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            try {
                                JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                                if (result.getBoolean("isExisted")) {
                                    tvNameError.setTextColor(Color.parseColor("#FA5858"));
                                    tvNameError.setText("?????? ???????????? ??????????????????.");
                                }
                                else {
                                    isCheckedName = true;
                                    tvNameError.setTextColor(Color.parseColor("#00AA7D"));
                                    tvNameError.setText("?????? ????????? ??????????????????.");
                                }
                            } catch (JSONException e) { e.printStackTrace(); }
                        }
                        else if (response.code() == 419) {
                            Toast.makeText(InitialSettingActivity.this, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.removeKey(InitialSettingActivity.this, "jwt");
                            Intent reLogin = new Intent(InitialSettingActivity.this, LoginActivity.class);
                            reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(reLogin);
                            finish();
                        }
                        else
                            Toast.makeText(InitialSettingActivity.this, "?????? ?????? ????????? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(InitialSettingActivity.this, "????????? ???????????? ???????????????. ????????? ?????????:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btSelectTown.setOnClickListener(v -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etName.getWindowToken(), 0);
            tvTownError.setVisibility(View.GONE);

            Intent itAddress = new Intent(InitialSettingActivity.this, DaumAddressActivity.class);
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        btSubmit.setOnClickListener(v -> {
            if (!isCheckedName) {
                tvNameError.setTextColor(Color.parseColor("#FA5858"));
                tvNameError.setText("????????? ?????? ????????? ?????????.");
            }
            else if (!isSetTown)
                tvTownError.setVisibility(View.VISIBLE);
            else {
                String nickName = etName.getText().toString().trim();
                JsonObject userData = new JsonObject();
                userData.addProperty("nickName", nickName);
                userData.addProperty("location", String.valueOf(location));
                RetrofitClient.getApiService().patchUserInfo(PreferenceManager.getString(this, "jwt"), userID, userData).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            myInfo = new UserInfo(userID, null, nickName, location.get("dong").getAsString(),
                                    location.get("detail").getAsString(), location.get("locationX").getAsDouble(), location.get("locationY").getAsDouble());
                            startActivity(new Intent(InitialSettingActivity.this, MainActivity.class));
                            finish();
                        }
                        else if (response.code() == 419) {
                            Toast.makeText(InitialSettingActivity.this, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.removeKey(InitialSettingActivity.this, "jwt");
                            Intent reLogin = new Intent(InitialSettingActivity.this, LoginActivity.class);
                            reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(reLogin);
                            finish();
                        }
                        else
                            Toast.makeText(InitialSettingActivity.this, "?????? ?????? ???????????? ????????? ???????????????:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(InitialSettingActivity.this, "????????? ???????????? ???????????????. ????????? ?????????:)", Toast.LENGTH_SHORT).show();
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
        btSelectTown.setText("?????? ?????????");
        tvResultTown.setVisibility(View.VISIBLE);
            tvResultTown.setText(loc.get("dong").getAsString());
        tvFullAddress.setVisibility(View.VISIBLE);
            tvFullAddress.setText(loc.get("detail").getAsString());
    }
}
