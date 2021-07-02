package com.tave7.dobdob;

import android.annotation.SuppressLint;
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

import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;

import java.util.ArrayList;

public class TagPostActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_post);

        ArrayList<PostInfoSimple> tagPostLists = getIntent().getExtras().getParcelableArrayList("tagPostLists");

        Toolbar toolbar = findViewById(R.id.tagPost_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_main, null);
        actionBar.setCustomView(customView);
        ImageView ivGPS = toolbar.findViewById(R.id.toolbar_gpspointer);
            ivGPS.setVisibility(View.GONE);
        TextView tvTag = toolbar.findViewById(R.id.toolbar_town);
            tvTag.setText("# "+getIntent().getExtras().getString("tagName"));
            tvTag.setTextColor(Color.parseColor("#5AAEFF"));


        RecyclerView rvTagPost = findViewById(R.id.tagPost);
        LinearLayoutManager manager = new LinearLayoutManager(TagPostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        PostRecyclerAdapter adapter = new PostRecyclerAdapter(tagPostLists);
        rvTagPost.setAdapter(adapter);      //어댑터 등록
        rvTagPost.addItemDecoration(new DividerItemDecoration(TagPostActivity.this, 1)); //리스트 사이의 구분선 설정
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//toolbar의 back키 눌렀을 때 동작
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
