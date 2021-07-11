package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.adapter.UserRecyclerAdapter;
import com.tave7.dobdob.data.UserInfo;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LikeUserActivity extends AppCompatActivity {
    private ArrayList<UserInfo> likeUserLists = null;
    private UserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_user);

        Toolbar toolbar = findViewById(R.id.likeUser_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_tagpost, null);
        actionBar.setCustomView(customView);
        TextView tvLike = toolbar.findViewById(R.id.toolbar_tv);
        tvLike.setText("좋아요");
        tvLike.setTextColor(Color.parseColor("#000000"));

        likeUserLists = getIntent().getExtras().getParcelableArrayList("likeUsers");
        for (UserInfo user : likeUserLists) {
            Bitmap userProfile = null;
            try {
                userProfile = new DownloadFileTask(user.getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            user.setUserProfileBM(userProfile);
        }

        RecyclerView rvTagPost = findViewById(R.id.likeUsers);
        LinearLayoutManager manager = new LinearLayoutManager(LikeUserActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        adapter = new UserRecyclerAdapter(likeUserLists);
        rvTagPost.setAdapter(adapter);
        rvTagPost.addItemDecoration(new DividerItemDecoration(LikeUserActivity.this, 1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
