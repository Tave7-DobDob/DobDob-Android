package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.adapter.UserRecyclerAdapter;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikeUserActivity extends AppCompatActivity {
    private ArrayList<UserInfo> likeUserLists = null;
    private UserRecyclerAdapter adapter;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_user);

        int postID = getIntent().getExtras().getInt("postID");

        Toolbar toolbar = findViewById(R.id.likeUser_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_tag_or_like, null);
        actionBar.setCustomView(customView);
        TextView tvLike = toolbar.findViewById(R.id.toolbar_tv);
        tvLike.setText("좋아요");
        tvLike.setTextColor(Color.parseColor("#000000"));

        likeUserLists = new ArrayList<>();
        RecyclerView rvTagPost = findViewById(R.id.likeUsers);
        LinearLayoutManager manager = new LinearLayoutManager(LikeUserActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        adapter = new UserRecyclerAdapter(likeUserLists);
        rvTagPost.setAdapter(adapter);
        rvTagPost.addItemDecoration(new DividerItemDecoration(LikeUserActivity.this, 1));

        tvInfo = findViewById(R.id.likeUser_info);
        tvInfo.setVisibility(View.GONE);

        setLikeUsers(postID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setLikeUsers(int postID) {
        RetrofitClient.getApiService().getIDPost(postID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    try {
                        likeUserLists.clear();
                        JSONObject postInfo = new JSONObject(Objects.requireNonNull(response.body())).getJSONObject("post");
                        JSONArray likesArray = postInfo.getJSONArray("Likes");
                        for (int i=0; i<likesArray.length(); i++) {
                            JSONObject likeObject = likesArray.getJSONObject(i);
                            JSONObject userObject = likeObject.getJSONObject("User");
                            UserInfo likeUser = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"));
                            Bitmap userProfile = null;
                            try {
                                userProfile = new DownloadFileTask(likeUser.getUserProfileUrl()).execute().get();
                            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            likeUser.setUserProfileBM(userProfile);
                            likeUserLists.add(likeUser);
                        }
                        if (likeUserLists.size() == 0) {
                            tvInfo.setVisibility(View.VISIBLE);
                            tvInfo.setText("글에 좋아요를 누른 사용자가 없습니다.");
                        }
                        else
                            adapter.notifyDataSetChanged();
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                else {
                    tvInfo.setVisibility(View.VISIBLE);
                    tvInfo.setText("좋아요를 누른 사용자를 로드할 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                tvInfo.setVisibility(View.VISIBLE);
                tvInfo.setText("좋아요를 누른 사용자를 로드할 수 없습니다.");
            }
        });
    }
}
