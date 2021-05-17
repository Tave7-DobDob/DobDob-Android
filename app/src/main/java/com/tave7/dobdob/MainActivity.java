package com.tave7.dobdob;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView rvPost;
    PostRecyclerAdapter adapter;
    FloatingActionButton fabAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: 임시 postList 생성
        ArrayList<PostInfo> postList = new ArrayList<>();
        ArrayList<String> tmpTag = new ArrayList<>();
        tmpTag.add("산책");
        tmpTag.add("동네산책");
        tmpTag.add("4명모집");
        postList.add(new PostInfo("테이비", "", "2021.05.16 20:00", "오늘 저녁에 배드민턴 칠 사람 구해요!", 12, 4, tmpTag));

        rvPost = findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(postList);
        rvPost.setAdapter(adapter);      //어댑터 등록
        rvPost.addItemDecoration(new DividerItemDecoration(MainActivity.this, 1)); //리스트 사이의 구분선 설정

        fabAddPost = findViewById(R.id.mainfabAddPost);
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostingActivity.class));    //글쓰기 창으로 화면이 넘어감
            }
        });
    }
}