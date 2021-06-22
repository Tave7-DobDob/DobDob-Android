package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tave7.dobdob.data.UserInfo;

import java.io.InputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModifyProfileActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;

    UserInfo userInfo = null, tmpUserInfo = null;
    Bitmap tmpChangeProfile;
    boolean isChangeProfile = false, isChangeName = false, isChangeTown = false;

    CircleImageView civUserProfile;
    EditText etUserName;
    TextView tvNameCheckInfo, tvUserTown, tvFullAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        tmpUserInfo = new UserInfo(null, "", "");
        userInfo = (UserInfo) getIntent().getExtras().getParcelable("userInfo");

        Toolbar toolbar = findViewById(R.id.modify_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_modify_profile, null);
        actionBar.setCustomView(customView);

        civUserProfile = (CircleImageView) findViewById(R.id.modify_userProfile);
        if (userInfo.getUserProfileUrl() == null)
            civUserProfile.setImageResource(R.drawable.user);
        else {    //TODO: 고쳐야 함(안됨)     //TODO: 해당 user의 이미지로 setImageResource변경
            //user.setUserProfileUrl("https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http%3A%2F%2Fcfile7.uf.tistory.com%2Fimage%2F24283C3858F778CA2EFABE");
            //civUserProfile.setImageBitmap(user.getBitmapProfile());
        }
        etUserName = (EditText) findViewById(R.id.modify_userName);            //TODO: 변경 시 해당 user의 닉네임으로 setText("")변경
            etUserName.setText(userInfo.getUserName());
        tvNameCheckInfo = (TextView) findViewById(R.id.modify_tvNameCheckInfo);
            tvNameCheckInfo.setVisibility(View.GONE);
        tvUserTown = (TextView) findViewById(R.id.modify_userTown);            //TODO: 해당 user의 동네로 setText("")변경(클릭시 주소 결정할 수 있게)
            tvUserTown.setText(userInfo.getUserTown());
        tvFullAddress = (TextView) findViewById(R.id.modify_tvFullAddress);     //TODO: 해당 user의 Full Address를 저장함
            //tvFullAddress.setText();      //TODO: 구현해야함!
        toolbarListener(toolbar);
        modifyProfileListener();
    }

    public void toolbarListener(Toolbar toolbar){
        TextView tvCancel = (TextView) toolbar.findViewById(R.id.toolbar_mp_cancel);
        tvCancel.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            
            finish();
        });
        
        TextView tvOK = (TextView) toolbar.findViewById(R.id.toolbar_mp_ok);
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
            if (isChangeTown)
                bUserInfo.putString("userTown", tmpUserInfo.getUserTown());
            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
            finish();
        });
    }

    public void modifyProfileListener() {
        TextView tvChangeProfile = (TextView) findViewById(R.id.modify_tvChangeProfile);
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

        Button btCheckName = (Button) findViewById(R.id.modify_btCheckName);
        btCheckName.setOnClickListener(v -> {
            isChangeName = false;

            String username = etUserName.getText().toString().trim();
            if (username.length() > 0) {
                //TODO: DB로부터 아이디 중복 확인
                // 중복 확인 후 가능할 시  ********************************************************
                isChangeName = true;
                tmpUserInfo.setUserName(username);
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("사용가능한 닉네임입니다:)");
                tvNameCheckInfo.setTextColor(Color.parseColor("#0000FF"));
    
                /* 중복 확인 후 가능하지 않을 시
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("이미 존재하는 닉네임입니다. 다른 닉네임을 사용해 주세요:)");
                tvNameCheckInfo.setTextColor(Color.parseColor("#FF0000"));
                 */
            }
            else {
                tvNameCheckInfo.setVisibility(View.VISIBLE);
                tvNameCheckInfo.setText("닉네임을 입력해 주세요:)");
                tvNameCheckInfo.setTextColor(Color.parseColor("#FF0000"));
            }
        });

        Button btChangeTown = (Button) findViewById(R.id.modify_btChangeTown);
        btChangeTown.setOnClickListener(v -> {
            /*
            //TODO: 서버로부터 주소를 받고 해당 주소를 DB에 저장
            isChangeTown = true;
            tmpUserInfo.setUserTown("XXX동");
            tvUserTown.setText("XXX동");      //TODO: 해당 주소를 변경
            //TODO: 주소를 입력하여 변경할 수 있도록 해야함 -> 취소버튼을 누르지 않았다면 isChangeTown = true;가 됨
             */
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
                InputStream is = getContentResolver().openInputStream(data.getData());
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
    }
}
