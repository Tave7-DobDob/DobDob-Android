package com.tave7.dobdob;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageActivity extends AppCompatActivity {
    CircleImageView civUserProfile;
    TextView tvUserNick, tvUserTown, tvUserPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Toolbar toolbar = findViewById(R.id.myPage_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        civUserProfile = findViewById(R.id.myPage_userProfile);     //TODO: 해당 user의 이미지로 setImageResource변경
        tvUserNick = findViewById(R.id.myPage_userNick);            //TODO: 해당 user의 닉네임으로 setText("")변경
        tvUserTown = findViewById(R.id.myPage_userTown);            //TODO: 해당 user의 동네로 setText("")변경
        tvUserPosts = findViewById(R.id.myPage_tvUserPost);         //TODO: 해당 user의 닉네임으로 setText(nickName+" 님이 작성한 글")변경

        //TODO: 임시 postList 생성
            ArrayList<PostInfo> postList = new ArrayList<>();
            ArrayList<String> tmpTag = new ArrayList<>();
                tmpTag.add("산책");
                tmpTag.add("동네산책");
                tmpTag.add("4명모집");
            postList.add(new PostInfo("", "테이비1", "2021.05.16 20:00", "오늘 저녁에 산책할 사람 구해요!", 12, 4, tmpTag));
            ArrayList<String> tmpTag2 = new ArrayList<>();
                tmpTag2.add("XX동");
                tmpTag2.add("공구");
            postList.add(new PostInfo("", "테이비1", "2021.05.18 15:00", "개별 포장 빨대 200개 공구하실 분 구합니다!", 3, 2, tmpTag2));
            postList.add(new PostInfo("", "테이비1", "2021.05.20 11:30", "맥모닝 같이 먹을 사람 구해요!", 1, 0, null));
            postList.add(new PostInfo("", "테이비1", "2021.05.21 13:10", "동네에 맛있는 반찬 가게 알려주세요!", 30, 43, null));
        RecyclerView rvPost = findViewById(R.id.myPagePosts);
        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        PostRecyclerAdapter adapter = new PostRecyclerAdapter(postList);
        rvPost.setAdapter(adapter);      //어댑터 등록
        rvPost.addItemDecoration(new DividerItemDecoration(MyPageActivity.this, 1)); //리스트 사이의 구분선 설정

        myPageClickListener();
    }

    public void myPageClickListener() {
        TextView tvChangeProfile = findViewById(R.id.myPage_tvChangeProfile);
        tvChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 갤러리에서 사진을 불러와 그 이미지로 civUserProfile의 리소스를 변경하고 DB에 저장함
            }
        });

        ImageView ivEditNick = findViewById(R.id.myPage_ivEditNick);
        ivEditNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 다이얼로그로 닉네임 변경 후 중복확인 후 DB에 저장함
                //tvUserNick.setText("입력한 닉네임 값");
            }
        });

        Button btChangeTown = findViewById(R.id.myPage_btChangeTown);
        btChangeTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 동네 설정을 다시 한 후 DB에 저장함
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
}
