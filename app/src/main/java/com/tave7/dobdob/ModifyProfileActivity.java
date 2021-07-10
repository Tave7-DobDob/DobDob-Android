package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.JsonObject;
import com.tave7.dobdob.data.PhotoInfo;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.InitialSettingActivity.DAUMADDRESS_REQUEST;
import static com.tave7.dobdob.MainActivity.myInfo;

public class ModifyProfileActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;
    private JsonObject location;

    private UserInfo tmpUserInfo = null;
    private PhotoInfo tmpProfileImg = null;
    private boolean isChangeProfile = false, isChangeName = false;

    private CircleImageView civUserProfile;
    private EditText etUserName;
    private TextView tvNameCheckInfo, tvUserTown, tvFullAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        tmpUserInfo = new UserInfo(myInfo.getUserID(), null, "", "", "");

        Toolbar toolbar = findViewById(R.id.modify_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_modify_profile, null);
        actionBar.setCustomView(customView);

        civUserProfile = findViewById(R.id.modify_userProfile);
        if (myInfo.getUserProfileUrl() == null)
            civUserProfile.setImageResource(R.drawable.user);
        else {
            Bitmap userProfile = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.user, null))).getBitmap();
            try {
                userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            civUserProfile.setImageBitmap(userProfile);
        }
        etUserName = findViewById(R.id.modify_userName);
            etUserName.setText(myInfo.getUserName());
        tvNameCheckInfo = findViewById(R.id.modify_tvNameCheckInfo);
            tvNameCheckInfo.setVisibility(View.GONE);
        tvUserTown = findViewById(R.id.modify_userTown);
            tvUserTown.setText(myInfo.getUserTown());
        tvFullAddress = findViewById(R.id.modify_tvFullAddress);
            tvFullAddress.setText(myInfo.getUserAddress());
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
            boolean isChangeAddress = !tmpUserInfo.getUserAddress().equals("") && !tmpUserInfo.getUserAddress().equals(myInfo.getUserAddress());

            if (isChangeProfile) {      //TODO: null이 되는 지를 확인해야 함!!!!
                tvOK.setEnabled(false);

                MultipartBody.Part postImage = null;
                if (tmpProfileImg != null)
                    postImage = MultipartBody.Part.createFormData("profileImage", tmpProfileImg.getPhotoFile().getName(), RequestBody.create(MediaType.parse("multipart/form-data"), tmpProfileImg.getPhotoFile()));

                RetrofitClient.getApiService().patchUserProfileImg(myInfo.getUserID(), postImage).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("MProfileImg 설정성공1", response.toString());
                        Log.i("MProfileImg 설정성공2", response.body());
                        if (response.code() == 200) {
                            Intent giveChangedUserInfo = new Intent();
                            Bundle bUserInfo = new Bundle();
                                bUserInfo.putBoolean("isChangeProfile", true);
                                //myInfo.setUserProfileUrl();
                                //TODO: tmpChangeProfile를 DB에 전달해 서버로부터 URI를 받아 해당 값을 String형태로 전달함

                            if (isChangeName || isChangeAddress) {
                                JsonObject userData = new JsonObject();
                                if (isChangeName)
                                    userData.addProperty("nickName", tmpUserInfo.getUserName());
                                if (isChangeAddress)
                                    userData.addProperty("location", String.valueOf(location));

                                RetrofitClient.getApiService().patchUserInfo(myInfo.getUserID(), userData).enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                        Log.i("MProfile 설정성공1", response.toString());
                                        Log.i("MProfile 설정성공2", response.body());
                                        if (response.code() == 200) {
                                            tvOK.setEnabled(true);

                                            if (isChangeName) {
                                                bUserInfo.putBoolean("isChangeName", true);
                                                myInfo.setUserName(tmpUserInfo.getUserName());
                                            }
                                            if (isChangeAddress) {
                                                bUserInfo.putBoolean("isChangeAddress", true);
                                                myInfo.setUserTown(tmpUserInfo.getUserTown());
                                                myInfo.setUserAddress(tmpUserInfo.getUserAddress());
                                            }
                                            bUserInfo.putBoolean("isChanged", true);
                                            giveChangedUserInfo.putExtras(bUserInfo);
                                            setResult(RESULT_OK, giveChangedUserInfo);
                                            finish();
                                        }
                                        else
                                            Toast.makeText(ModifyProfileActivity.this, "다시 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                        Log.i("MProfile 설정실패", t.getMessage());
                                        Toast.makeText(ModifyProfileActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                tvOK.setEnabled(true);

                                bUserInfo.putBoolean("isChanged", true);
                                giveChangedUserInfo.putExtras(bUserInfo);
                                setResult(RESULT_OK, giveChangedUserInfo);
                                finish();
                            }
                        }
                        else
                            Toast.makeText(ModifyProfileActivity.this, "다시 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("MProfileImg 설정실패", t.getMessage());
                        Toast.makeText(ModifyProfileActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (isChangeName || isChangeAddress) {
                JsonObject userData = new JsonObject();
                if (isChangeName)
                    userData.addProperty("nickName", tmpUserInfo.getUserName());
                if (isChangeAddress)
                    userData.addProperty("location", String.valueOf(location));

                RetrofitClient.getApiService().patchUserInfo(myInfo.getUserID(), userData).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("MProfile 설정성공1", response.toString());
                        Log.i("MProfile 설정성공2", response.body());
                        if (response.code() == 200) {
                            tvOK.setEnabled(true);

                            Intent giveChangedUserInfo = new Intent();
                            Bundle bUserInfo = new Bundle();
                            if (isChangeName) {
                                bUserInfo.putBoolean("isChangeName", true);
                                myInfo.setUserName(tmpUserInfo.getUserName());
                            }
                            if (isChangeAddress) {
                                bUserInfo.putBoolean("isChangeAddress", true);
                                myInfo.setUserTown(tmpUserInfo.getUserTown());
                                myInfo.setUserAddress(tmpUserInfo.getUserAddress());
                            }
                            bUserInfo.putBoolean("isChanged", true);
                            giveChangedUserInfo.putExtras(bUserInfo);
                            setResult(RESULT_OK, giveChangedUserInfo);
                            finish();
                        }
                        else
                            Toast.makeText(ModifyProfileActivity.this, "다시 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("MProfile 설정실패", t.getMessage());
                        Toast.makeText(ModifyProfileActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void modifyProfileListener() {
        TextView tvChangeProfile = findViewById(R.id.modify_tvChangeProfile);
        tvChangeProfile.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_setprofile, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setView(dialogView);
            final AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();

            WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            int width = size.x;
            params.width = (int) (width*0.75);
            alertDialog.getWindow().setAttributes(params);

            TextView tvProfileDefault = dialogView.findViewById(R.id.pdialog_setDefault);
            tvProfileDefault.setOnClickListener(view -> {
                isChangeProfile = true;
                tmpProfileImg = null;
                civUserProfile.setImageResource(R.drawable.user);
                alertDialog.dismiss();
            });

            TextView tvProfileImg = dialogView.findViewById(R.id.pdialog_selectImg);
            tvProfileImg.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_FROM_GALLERY);
                alertDialog.dismiss();
            });
        });
        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvNameCheckInfo.getVisibility() == View.VISIBLE) {
                    tvNameCheckInfo.setVisibility(View.GONE);
                    isChangeName = false;
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
            else if (username.equals(myInfo.getUserName())) {
                tvNameCheckInfo.setVisibility(View.GONE);
            }
            else if (username.equals("")) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("닉네임을 입력하지 않았습니다.");
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
            }
            else if (username.length()<2 || username.length()>20) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
                tvNameCheckInfo.setText("닉네임은 2자 이상 20자 이내여야 합니다.");
            }
            else if (!username.matches(".*[a-zㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
                tvNameCheckInfo.setText("닉네임에 영문 소문자 혹은 한글이 1글자 이상 있어야 합니다.");
            }
            else if (username.matches(".*[^0-9a-zㄱ-ㅎㅏ-ㅣ가-힣].*")) {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setTextColor(Color.parseColor("#FA5858"));
                tvNameCheckInfo.setText("영문 소문자/한글/숫자 이외의 문자는 사용 불가합니다:)");
            }
            else {
                RetrofitClient.getApiService().checkExistNick(username).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("MProfile 닉중복확인 성공1", response.toString());
                        Log.i("MProfile 닉중복확인 성공2", response.body());
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
                        Log.i("MProfile 닉중복확인 연결실패", t.getMessage());
                        Toast.makeText(ModifyProfileActivity.this, "다시 한번 닉네임 중복 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btChangeTown = findViewById(R.id.modify_btChangeTown);
        btChangeTown.setOnClickListener(v ->
            startActivityForResult(new Intent(ModifyProfileActivity.this, DaumAddressActivity.class), DAUMADDRESS_REQUEST)
        );
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                Uri uri = Objects.requireNonNull(data).getData();
                String photoPath = getRealPathFromURI(uri);

                File file = new File(Objects.requireNonNull(photoPath));     //갤러리에서 선택한 파일
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data).getData());
                Bitmap photoBM = BitmapFactory.decodeStream(is);
                is.close();

                isChangeProfile = true;
                tmpProfileImg = new PhotoInfo(file, photoBM);
                civUserProfile.setImageBitmap(photoBM);
            } catch (Exception e){ e.printStackTrace(); }
        } else if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_CANCELED){
            Toast.makeText(this,"사진 선택 취소", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            new GetGEOTask(this, "modifyProfile", Objects.requireNonNull(data).getExtras().getString("address")).execute();
        }
    }

    //갤러리에서 선택한 사진의 절대경로를 반환함
    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        try (Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null)) {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    public void modifyPSettingTown(JsonObject loc) {
        location = loc;

        tvUserTown.setText(loc.get("dong").getAsString());
        tvFullAddress.setText(loc.get("detail").getAsString());
        tmpUserInfo.setUserTown(loc.get("dong").getAsString());
        tmpUserInfo.setUserAddress(loc.get("detail").getAsString());
    }
}
