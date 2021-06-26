package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST = 8000;

    boolean isMyPage = false;   //본인의 페이지인지?
    boolean isChangeProfile = false, isChangeName = false, isChangeTown = false;
    UserInfo userInfo = null;
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

        userInfo = getIntent().getExtras().getParcelable("userInfo");
        userPostList = getIntent().getExtras().getParcelableArrayList("userPosts");


        isMyPage = getIntent().getExtras().getBoolean("isMyPage");
        if (isMyPage) {   //현재 사용자의 페이지를 보는 경우
            @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_mypage, null);
            actionBar.setCustomView(customView);
            toolbarListener(toolbar);
        }

        civUserProfile = findViewById(R.id.myPage_userProfile);
        if (userInfo.getUserProfileUrl() == null)
            civUserProfile.setImageResource(R.drawable.user);
        else {    //TODO: 고쳐야 함(안됨)     //TODO: 해당 user의 이미지로 setImageResource변경
            //user.setUserProfileUrl("https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http%3A%2F%2Fcfile7.uf.tistory.com%2Fimage%2F24283C3858F778CA2EFABE");
            //civUserProfile.setImageBitmap(user.getBitmapProfile());
        }
        tvUserName = findViewById(R.id.myPage_userName);            //TODO: 변경 시 해당 user의 닉네임으로 setText("")변경
            tvUserName.setText(userInfo.getUserName());
        tvUserTown = findViewById(R.id.myPage_userTown);            //TODO: 해당 user의 동네로 setText("")변경(클릭시 주소 결정할 수 있게)
            tvUserTown.setText(userInfo.getUserTown());
        tvUserPosts = findViewById(R.id.myPage_tvUserPost);         //TODO: 해당 user의 닉네임으로 setText(name+" 님이 작성한 글")변경
            tvUserPosts.setText(userInfo.getUserName()+" 님이 작성한 글");
        rvMyPagePosts = findViewById(R.id.myPagePosts);

        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvMyPagePosts.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(userPostList, userInfo);
        rvMyPagePosts.setAdapter(adapter);      //어댑터 등록
        DividerItemDecoration devider = new DividerItemDecoration(MyPageActivity.this, 1);
        devider.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null));
        rvMyPagePosts.addItemDecoration(devider); //리스트 사이의 구분선 설정
    }

    public void toolbarListener(Toolbar toolbar){
        ivEdit = toolbar.findViewById(R.id.toolbar_edit);
        ivEdit.setOnClickListener(v -> {
            Intent editProfile = new Intent(this, ModifyProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("userInfo", userInfo);
            editProfile.putExtras(bundle);
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
            /*  TODO: 이미지를 String으로 전달해야 함
            if (isChangeProfile)
                bUserInfo.putString("userProfileUrl", );
                //tmpChangeProfile를 DB에 전달해 서버로부터 URI를 받아 해당 값을 String형태로 전달함
             */
            if (isChangeName)
                bUserInfo.putString("userName", userInfo.getUserName());
            if (isChangeTown)
                bUserInfo.putString("userTown", userInfo.getUserTown());
            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            //if (data.hasExtra("userProfileUrl")) {
            //  isChangeProfile = true;
            //  userInfo.setUserProfileUrl(data.getExtras().getString("userProfileUrl"));
            //}
            if (data != null && data.hasExtra("userName")) {
                isChangeName = true;
                adapter.changeWriterName(userInfo.getUserName(), data.getExtras().getString("userName"));
                userInfo.setUserName(data.getExtras().getString("userName"));
                tvUserName.setText(userInfo.getUserName());
                tvUserPosts.setText(userInfo.getUserName() + " 님이 작성한 글");
                //PreferenceManager.setString(MyPageActivity.this, "userName", userInfo.getUserName());
                //(DB에 바뀐 이름을 전달하고 DB에서 totalPostList를 받아 search를 통해 notify 함)
            }
            if (data != null && data.hasExtra("userTown")) {
                isChangeTown = true;
                tvUserTown.setText(data.getExtras().getString("userTown"));
                userInfo.setUserTown(data.getExtras().getString("userTown"));
                userInfo.setUserAddress(data.getExtras().getString("userAddress"));
            }
        }
    }
}
