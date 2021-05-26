package com.tave7.dobdob;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TagPostActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_post);

        ArrayList<PostInfo> tagPostLists = (ArrayList<PostInfo>) getIntent().getSerializableExtra("tagPostLists");
        UserInfo userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");

        Toolbar toolbar = (Toolbar) findViewById(R.id.tagPost_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        View customView = LayoutInflater.from(this).inflate(R.layout.main_actionbar, null);
        actionBar.setCustomView(customView);
        ImageView ivGPS = (ImageView) toolbar.findViewById(R.id.toolbar_gpspointer);
            ivGPS.setVisibility(View.GONE);
        TextView tvTag = (TextView) toolbar.findViewById(R.id.toolbar_town);
            tvTag.setText("# "+getIntent().getStringExtra("tagName"));
            tvTag.setTextColor(Color.parseColor("#5AAEFF"));


        RecyclerView rvTagPost = (RecyclerView) findViewById(R.id.tagPost);
        LinearLayoutManager manager = new LinearLayoutManager(TagPostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        PostRecyclerAdapter adapter = new PostRecyclerAdapter(tagPostLists, userInfo);
        rvTagPost.setAdapter(adapter);      //어댑터 등록
        rvTagPost.addItemDecoration(new DividerItemDecoration(TagPostActivity.this, 1)); //리스트 사이의 구분선 설정
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
