package com.tave7.dobdob;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    RecyclerView rvPost;
    PostRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);    //기본 제목을 없앰
        View customView = LayoutInflater.from(this).inflate(R.layout.main_actionbar, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        //TODO: 임시 postList 생성
            ArrayList<PostInfo> postList = new ArrayList<>();
            ArrayList<String> tmpTag = new ArrayList<>();
            tmpTag.add("산책");
            tmpTag.add("동네산책");
            tmpTag.add("4명모집");
            postList.add(new PostInfo("", "테이비", "2021.05.16 20:00", "오늘 저녁에 배드민턴 칠 사람 구해요!", 12, 4, tmpTag));

        rvPost = findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(postList);
        rvPost.setAdapter(adapter);      //어댑터 등록
        rvPost.addItemDecoration(new DividerItemDecoration(MainActivity.this, 1)); //리스트 사이의 구분선 설정

        FloatingActionButton fabAddPost = findViewById(R.id.mainFabAddPost);
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostingActivity.class));    //글쓰기 창으로 화면이 넘어감
            }
        });
    }

    public void toolbarListener(Toolbar toolbar){
        TextView tvTown = toolbar.findViewById(R.id.toolbar_town);
        ImageView ivSearch = toolbar.findViewById(R.id.toolbar_search);
        CircleImageView civProfile = toolbar.findViewById(R.id.toolbar_profile);

        tvTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 동네를 클릭했을 때, 동네 변경이 가능해야 함
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 검색 기능을 넣어야 함
            }
        });

        civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyPageActivity.class));     //마이페이지 화면으로 넘어감
            }
        });
    }
}
