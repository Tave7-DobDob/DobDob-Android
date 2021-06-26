package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonObject;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.InitialSettingActivity.DAUMADDRESS_REQUEST;

public class ModifyProfileActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;
    private JsonObject location;

    private UserInfo userInfo = null;
    private UserInfo tmpUserInfo = null;
    private Bitmap tmpChangeProfile;
    private boolean isChangeProfile = false, isChangeName = false;

    private CircleImageView civUserProfile;
    private EditText etUserName;
    private TextView tvNameCheckInfo;
    private TextView tvUserTown;
    private TextView tvFullAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        tmpUserInfo = new UserInfo(null, "", "", "");
        userInfo = getIntent().getExtras().getParcelable("userInfo");

        Toolbar toolbar = findViewById(R.id.modify_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_modify_profile, null);
        actionBar.setCustomView(customView);

        civUserProfile = findViewById(R.id.modify_userProfile);
        if (userInfo.getUserProfileUrl() == null)
            civUserProfile.setImageResource(R.drawable.user);
        else {    //TODO: 고쳐야 함(안됨)     //TODO: 해당 user의 이미지로 setImageResource변경
            //user.setUserProfileUrl("https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http%3A%2F%2Fcfile7.uf.tistory.com%2Fimage%2F24283C3858F778CA2EFABE");
            //civUserProfile.setImageBitmap(user.getBitmapProfile());
        }
        etUserName = findViewById(R.id.modify_userName);
            etUserName.setText(userInfo.getUserName());
        tvNameCheckInfo = findViewById(R.id.modify_tvNameCheckInfo);
            tvNameCheckInfo.setVisibility(View.GONE);
        tvUserTown = findViewById(R.id.modify_userTown);
            tvUserTown.setText(userInfo.getUserTown());
        tvFullAddress = findViewById(R.id.modify_tvFullAddress);
            tvFullAddress.setText(userInfo.getUserAddress());
        toolbarListener(toolbar);
        modifyProfileListener();
    }

    public void toolbarListener(Toolbar toolbar){
        TextView tvCancel = toolbar.findViewById(R.id.toolbar_mp_cancel);
        tvCancel.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            
            finish();
        });
        
        TextView tvOK = toolbar.findViewById(R.id.toolbar_mp_ok);
        tvOK.setOnClickListener(v -> {      //완료 버튼 클릭 시
            Intent giveChangedUserInfo = new Intent();
            Bundle bUserInfo = new Bundle();
            /*  TODO: 이미지를 String으로 전달해야 함
            if (isChangeProfile)
                bUserInfo.putString("userProfileUrl", );
                //tmpChangeProfile를 DB에 전달해 서버로부터 URI를 받아 해당 값을 String형태로 전달함
             */
            if (isChangeName)
                bUserInfo.putString("userName", tmpUserInfo.getUserName());
            if (!tmpUserInfo.getUserAddress().equals("") && !tmpUserInfo.getUserAddress().equals(userInfo.getUserAddress())) {
                Log.i("확인용 user", userInfo.getUserTown());
                Log.i("확인용 tmpuser", tmpUserInfo.getUserTown());
                bUserInfo.putString("userTown", tmpUserInfo.getUserTown());
                bUserInfo.putString("userAddress", tmpUserInfo.getUserAddress());
            }
            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
            finish();
        });
    }

    public void modifyProfileListener() {
        TextView tvChangeProfile = findViewById(R.id.modify_tvChangeProfile);
        tvChangeProfile.setOnClickListener(v -> {
            //TODO: 갤러리에서 사진을 불러와 그 이미지로 civUserProfile의 리소스를 변경하고 DB에 저장함
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_FROM_GALLERY);
        });

        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvNameCheckInfo.getVisibility() == View.VISIBLE) {
                    tvNameCheckInfo.setVisibility(View.GONE);
                    isChangeName = false;       //이름이 바뀌었으므로
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        Button btCheckName = findViewById(R.id.modify_btCheckName);
        btCheckName.setOnClickListener(v -> {
            isChangeName = false;

            String username = etUserName.getText().toString().trim();
            if (username.length() != etUserName.getText().toString().length()) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("닉네임에 공백이 포함되어 있습니다.");
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (username.equals("")) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("닉네임을 입력하지 않았습니다.");
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
            }
            else {
                RetrofitClient.getApiService().checkExistNick(username).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {   //로그인 사용 가능
                            try {
                                JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));

                                if (result.getBoolean("isExisted")) {
                                    tvNameCheckInfo.setVisibility(View.VISIBLE);
                                    tvNameCheckInfo.setText("이미 존재하는 닉네임입니다. 다른 닉네임을 사용해 주세요:)");
                                    tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
                                }
                                else {
                                    isChangeName = true;
                                    tmpUserInfo.setUserName(username);
                                    tvNameCheckInfo.setVisibility(View.VISIBLE);
                                    tvNameCheckInfo.setText("사용 가능한 닉네임입니다:)");
                                    tvNameCheckInfo.setTextColor(Color.parseColor("#00AA7D"));
                                }
                            } catch (JSONException e) { e.printStackTrace(); }
                        }
                        else {
                            Toast.makeText(ModifyProfileActivity.this, "다시 한번 닉네임 중복 확인해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("Initial 닉중복확인 연결실패", t.getMessage());
                        Toast.makeText(ModifyProfileActivity.this, "다시 한번 닉네임 중복 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btChangeTown = findViewById(R.id.modify_btChangeTown);
        btChangeTown.setOnClickListener(v -> {
            Intent itAddress = new Intent(ModifyProfileActivity.this, DaumAddressActivity.class);  //도로명주소 API 실행
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);     //TODO: 확인!!!!

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data).getData());
                tmpChangeProfile = BitmapFactory.decodeStream(is);
                is.close();

                isChangeProfile = true;

                //TODO: 추가로 선택한 이미지를 변수로 저장한 후 수정완료 버튼 클릭 시 DB에 저장
                //url을 tmpUserInfo.setUserProfileUrl("");을 통해 저장
                civUserProfile.setImageBitmap(tmpChangeProfile);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_CANCELED){
            Toast.makeText(this,"사진 선택 취소", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            try {
                new GetGEOTask(this, "modifyProfile", Objects.requireNonNull(data).getExtras().getString("address")).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        }
    }

    public void modifyPSettingTown(JsonObject loc) {
        location = loc;

        tvUserTown.setText(loc.get("dong").getAsString());
        tvFullAddress.setText(loc.get("fullAddress").getAsString());
        tmpUserInfo.setUserTown(loc.get("dong").getAsString());
        tmpUserInfo.setUserAddress(loc.get("fullAddress").getAsString());
    }
}
