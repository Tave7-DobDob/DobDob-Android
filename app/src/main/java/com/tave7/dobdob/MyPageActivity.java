package com.tave7.dobdob;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageActivity extends AppCompatActivity {
    UserInfo userInfo = null;
    ArrayList<PostInfo> userPostList = null;        //user가 올린 글 모음
    private static final int PICK_FROM_GALLERY = 100;
    boolean isEdit = false, isChangeImage = false, isChangeName = false, isChangeTown = false ;     //현재 글 수정중인지
    CircleImageView civUserProfile;
    TextView tvChangeProfile, tvUserName, tvUserTown, tvUserPosts;
    RecyclerView rvMyPagePosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myPage_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
        userPostList = (ArrayList<PostInfo>) getIntent().getSerializableExtra("userPosts");

        if (getIntent().getBooleanExtra("isMyPage", false)) {   //현재 사용자의 페이지를 보는 경우
            View customView = LayoutInflater.from(this).inflate(R.layout.other_actionbar, null);
            actionBar.setCustomView(customView);
            toolbarListener(toolbar);
        }

        civUserProfile = (CircleImageView) findViewById(R.id.myPage_userProfile);
        if (userInfo.getUserProfileUrl().length() == 0)
            civUserProfile.setImageResource(R.drawable.user_image);
        else {    //TODO: 고쳐야 함(안됨)     //TODO: 해당 user의 이미지로 setImageResource변경
            //user.setUserProfileUrl("https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http%3A%2F%2Fcfile7.uf.tistory.com%2Fimage%2F24283C3858F778CA2EFABE");
            //civUserProfile.setImageBitmap(user.getBitmapProfile());
        }

        tvChangeProfile = (TextView) findViewById(R.id.myPage_tvChangeProfile);
            tvChangeProfile.setVisibility(View.GONE);
        tvUserName = (TextView) findViewById(R.id.myPage_userName);            //TODO: 변경 시 해당 user의 닉네임으로 setText("")변경
            tvUserName.setText(userInfo.getUserName());
        tvUserTown = (TextView) findViewById(R.id.myPage_userTown);            //TODO: 해당 user의 동네로 setText("")변경(클릭시 주소 결정할 수 있게)
            tvUserTown.setText(userInfo.getUserTown());
        tvUserPosts = (TextView) findViewById(R.id.myPage_tvUserPost);         //TODO: 해당 user의 닉네임으로 setText(name+" 님이 작성한 글")변경
            tvUserPosts.setText(userInfo.getUserName()+" 님이 작성한 글");
        rvMyPagePosts = (RecyclerView) findViewById(R.id.myPagePosts);


        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvMyPagePosts.setLayoutManager(manager);
        PostRecyclerAdapter adapter = new PostRecyclerAdapter(userPostList, userInfo);
        rvMyPagePosts.setAdapter(adapter);      //어댑터 등록
        rvMyPagePosts.addItemDecoration(new DividerItemDecoration(MyPageActivity.this, 1)); //리스트 사이의 구분선 설정

        myPageClickListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivEditCancel = (ImageView) toolbar.findViewById(R.id.toolbar_editCancel);
            ivEditCancel.setVisibility(View.GONE);     //수정버튼이 눌릴 때만 수정취소버튼이 보이게 되어야 함
        TextView tvPostDelete = (TextView) toolbar.findViewById(R.id.toolbar_delete);
            tvPostDelete.setVisibility(View.GONE);
        ImageView ivEdit = (ImageView) toolbar.findViewById(R.id.toolbar_edit);

        ivEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //수정 취소
                isEdit = false;     //지금부터 수정 안됨
                isChangeImage = false;
                    civUserProfile.setImageResource(R.drawable.user_image);     //TODO: 다시 원래대로 사진 돌려놔야 함
                isChangeName = false;
                    tvUserName.setText("닉네임");     //TODO: 기존 닉네임으로 돌려놔야 함
                isChangeTown = false;
                    tvUserTown.setText("XX동");      //TODO: 기존 동네로 돌려놔야 함

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                ivEditCancel.setVisibility(View.GONE);
                tvPostDelete.setVisibility(View.VISIBLE);
                ivEdit.setImageResource(R.drawable.edit);

                tvChangeProfile.setVisibility(View.GONE);
                tvUserName.setTextColor(Color.parseColor("#000000"));
                tvUserTown.setTextColor(Color.parseColor("#000000"));
                tvUserPosts.setVisibility(View.VISIBLE);
                rvMyPagePosts.setVisibility(View.VISIBLE);
            }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;   //지금부터 수정가능여부

                if (isEdit) {   //작성한 글(post)을 수정할 수 있음
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);    //뒤로가기 버튼 안보이게 함
                    ivEditCancel.setVisibility(View.VISIBLE);
                    tvPostDelete.setVisibility(View.GONE);
                    ivEdit.setImageResource(R.drawable.ok);

                    tvChangeProfile.setVisibility(View.VISIBLE);
                    tvUserName.setTextColor(Color.parseColor("#7112FF"));
                    tvUserTown.setTextColor(Color.parseColor("#7112FF"));
                    tvUserPosts.setVisibility(View.GONE);
                    rvMyPagePosts.setVisibility(View.GONE);
                }
                else {          //수정완료함(수정한 것 반영)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                    ivEditCancel.setVisibility(View.GONE);
                    tvPostDelete.setVisibility(View.VISIBLE);
                    ivEdit.setImageResource(R.drawable.edit);

                    tvChangeProfile.setVisibility(View.GONE);
                    tvUserName.setTextColor(Color.parseColor("#000000"));
                    tvUserTown.setTextColor(Color.parseColor("#000000"));
                    tvUserPosts.setVisibility(View.VISIBLE);
                    rvMyPagePosts.setVisibility(View.VISIBLE);

                    if (isChangeImage) {
                        //TODO: DB에 바뀐 이미지 저장 + 마이페이지 내용 변경
                    }
                    if (isChangeName) {
                        //TODO: DB에 바뀐 닉네임 저장 + 마이페이지 내용 변경
                    }
                    if (isChangeTown) {
                        //TODO: DB에 바뀐 동네 저장 + 마이페이지 내용 변경
                    }
                }
            }
        });
    }

    public void myPageClickListener() {
        tvChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 갤러리에서 사진을 불러와 그 이미지로 civUserProfile의 리소스를 변경하고 DB에 저장함
                Intent intent = new Intent();
                intent.setType("image/*");
                //intent.setType(MediaStore.Images.Media.CONTENT_TYPE);       //기기 기본 갤러리 접근
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_FROM_GALLERY);
            }
        });

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    //마지막으로 닉네임 입력받음
                    NameChangeDialog nameChangeDialog = new NameChangeDialog(MyPageActivity.this, new NameChangeDialog.NameChangeDialogListener() {
                        @Override
                        public void onClickChangeBt() {    //닉네임 중복확인 완료 후 닉네임 변경
                            isChangeName = true;

                            //TODO: DB에 사용자 계정 추가 요청
                        }
                    });
                    nameChangeDialog.show();
                }
            }
        });

        tvUserTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    //주소를 입력하여 변경할 수 있도록 해야함 -> 취소버튼을 누르지 않았다면 isChangeTown = true;가 됨
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{    //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                isChangeImage = true;
                
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap bm = BitmapFactory.decodeStream(is);
                is.close();
                //TODO: 추가로 선택한 이미지를 변수로 저장한 후 수정완료 버튼 클릭 시 DB에 저장
                civUserProfile.setImageBitmap(bm);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_CANCELED){
            Toast.makeText(this,"취소", Toast.LENGTH_SHORT).show();
        }

    }
}
