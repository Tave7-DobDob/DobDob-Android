package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tave7.dobdob.MainActivity.myInfo;

public class MyPageActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST = 8000;

    boolean isMyPage = true;   //본인의 페이지인지?
    boolean isChangeProfile = false, isChangeName = false, isChangeAddress = false;
    UserInfo otherInfo = null;
    ArrayList<PostInfoSimple> userPostList = null;        //user가 올린 글 모음

    CircleImageView civUserProfile;
    ImageView ivEdit;
    PostRecyclerAdapter adapter;
    RecyclerView rvMyPagePosts;
    TextView tvUserName, tvUserTown, tvUserPosts;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Toolbar toolbar = findViewById(R.id.myPage_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        civUserProfile = findViewById(R.id.myPage_userProfile);

        if (getIntent().hasExtra("userID")) {
            //TODO: DB로부터 해당 id 소유자의 정보 및 내용들을 받아옴**********************************(구현해야 함!)
            isMyPage = false;
            otherInfo = getIntent().getExtras().getParcelable("userInfo");      //다른 사람의 값을 받아옴(임시로 작성함!!!!! 나중에 변경해야 함!)

            if (otherInfo.getUserProfileUrl() == null)
                civUserProfile.setImageResource(R.drawable.user);
            else {
                Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                try {
                    userProfile = new DownloadFileTask(otherInfo.getUserProfileUrl()).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                civUserProfile.setImageBitmap(userProfile);
            }
        }
        userPostList = getIntent().getExtras().getParcelableArrayList("userPosts");

        if (isMyPage) {   //현재 사용자의 페이지를 보는 경우
            @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_mypage, null);
            actionBar.setCustomView(customView);
            toolbarListener(toolbar);

            if (myInfo.getUserProfileUrl() == null)
                civUserProfile.setImageResource(R.drawable.user);
            else {
                Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                try {
                    userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                civUserProfile.setImageBitmap(userProfile);
            }
        }
        tvUserName = findViewById(R.id.myPage_userName);
            tvUserName.setText(isMyPage? myInfo.getUserName() : otherInfo.getUserName());
        tvUserTown = findViewById(R.id.myPage_userTown);
            tvUserTown.setText(isMyPage? myInfo.getUserTown() : otherInfo.getUserTown());
        tvUserPosts = findViewById(R.id.myPage_tvUserPost);
            tvUserPosts.setText(isMyPage? myInfo.getUserName()+" 님이 작성한 글" : otherInfo.getUserName()+" 님이 작성한 글");
        rvMyPagePosts = findViewById(R.id.myPagePosts);

        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvMyPagePosts.setLayoutManager(manager);
        if (isMyPage)
            adapter = new PostRecyclerAdapter(userPostList, myInfo);
        else
            adapter = new PostRecyclerAdapter(userPostList, otherInfo);
        rvMyPagePosts.setAdapter(adapter);
        DividerItemDecoration devider = new DividerItemDecoration(MyPageActivity.this, 1);
        devider.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null)));
        rvMyPagePosts.addItemDecoration(devider); //리스트 사이의 구분선 설정
    }

    public void toolbarListener(Toolbar toolbar){
        ivEdit = toolbar.findViewById(R.id.toolbar_edit);
        ivEdit.setOnClickListener(v -> {
            Intent editProfile = new Intent(this, ModifyProfileActivity.class);
            startActivityForResult(editProfile, EDIT_PROFILE_REQUEST);
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
    public void finish() {
        if (isMyPage) {
            //TODO: 메인인지를 구별해야 함!!(MainActivity일 때!라는 코드가 있어야 함!)
            Intent giveChangedUserInfo = new Intent();
            Bundle bUserInfo = new Bundle();
            if (isChangeProfile || isChangeName || isChangeAddress)
                bUserInfo.putBoolean("isChanged", true);

            //모두 삭제해야함
            /*
            if (isChangeProfile)
                bUserInfo.putString("userProfileUrl", );
                //tmpChangeProfile를 DB에 전달해 서버로부터 URI를 받아 해당 값을 String형태로 전달함
            if (isChangeName)
                bUserInfo.putString("userName", myInfo.getUserName());
            if (isChangeAddress)
                bUserInfo.putString("userTown", myInfo.getUserTown());
            */

            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getExtras().getBoolean("isChanged")) {
                //TODO: DB로부터 user의 post들 다 받아오기
                //DB에서 userPostList를 다시 설정함(clear후에 데이터 새로고침)
                adapter.notifyDataSetChanged();
            }
            if (data != null && data.hasExtra("isChangeProfile")) {
                isChangeProfile = true;
                if (myInfo.getUserProfileUrl() == null)
                    civUserProfile.setImageResource(R.drawable.user);
                else {
                    Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                    try {
                        userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    civUserProfile.setImageBitmap(userProfile);
                }
            }
            if (data != null && data.hasExtra("isChangeName")) {
                isChangeName = true;
                tvUserName.setText(myInfo.getUserName());
                tvUserPosts.setText(myInfo.getUserName() + " 님이 작성한 글");
            }
            if (data != null && data.hasExtra("isChangeAddress")) {
                isChangeAddress = true;
                tvUserTown.setText(data.getExtras().getString("userTown"));
            }
        }
    }
}
