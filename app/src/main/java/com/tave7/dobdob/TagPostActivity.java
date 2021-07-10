package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagPostActivity extends AppCompatActivity {
    private String tagName = "";
    private ArrayList<PostInfoSimple> tagPostLists = null;
    private PostRecyclerAdapter adapter;
    private SwipeRefreshLayout srlPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_post);

        tagPostLists = new ArrayList<>();
        tagName = getIntent().getExtras().getString("tagName");

        Toolbar toolbar = findViewById(R.id.tagPost_toolbar);
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
            tvTag.setText("# ".concat(tagName));
            tvTag.setTextColor(Color.parseColor("#5AAEFF"));

        srlPosts = findViewById(R.id.tagPost_swipeRL);
        srlPosts.setDistanceToTriggerSync(400);
        srlPosts.setOnRefreshListener(() -> {
            setTagPost(true);
        });
        RecyclerView rvTagPost = findViewById(R.id.tagPost);
        LinearLayoutManager manager = new LinearLayoutManager(TagPostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter('t', tagPostLists);
        rvTagPost.setAdapter(adapter);
        rvTagPost.addItemDecoration(new DividerItemDecoration(TagPostActivity.this, 1));

        setTagPost(false);
    }

    private void setTagPost(boolean isSwipe) {
        RetrofitClient.getApiService().getTagPost(tagName).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("TagPostA 태그검색 성공", response.toString());
                Log.i("TagPostA 태그검색 성공2", response.body());
                if (response.code() == 200) {
                    tagPostLists.clear();
                    try {
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                        JSONArray jsonPosts = result.getJSONArray("posts");
                        for (int i=0; i<jsonPosts.length(); i++) {
                            JSONObject postObject = jsonPosts.getJSONObject(i);

                            int postID = postObject.getInt("id");
                            JSONObject userObject = postObject.getJSONObject("User");
                            UserInfo writerInfo;
                            if (userObject.isNull("profileUrl"))
                                writerInfo = new UserInfo(userObject.getInt("id"), null, userObject.getString("nickName"), postObject.getJSONObject("Location").getString("dong"));
                            else
                                writerInfo = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"), postObject.getJSONObject("Location").getString("dong"));
                            String postTime = postObject.getString("createdAt");
                            String title = postObject.getString("title");
                            int likeNum = postObject.getInt("likeCount");
                            int commentNum = postObject.getInt("commentCount");

                            ArrayList<String> tags = new ArrayList<>();
                            JSONArray tagsArray = postObject.getJSONArray("Tags");
                            for (int j=0; j<tagsArray.length(); j++){
                                JSONObject tagObject = tagsArray.getJSONObject(j);
                                tags.add(tagObject.getString("name"));
                            }

                            PostInfoSimple post = new PostInfoSimple(postID, writerInfo, postTime, title, likeNum, commentNum, tags);
                            tagPostLists.add(post);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                    adapter.notifyDataSetChanged();
                }
                else
                    Toast.makeText(TagPostActivity.this, "다시 한번 검색해 주세요:)", Toast.LENGTH_SHORT).show();

                if (isSwipe)
                    srlPosts.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isSwipe)
                    srlPosts.setRefreshing(false);
                Toast.makeText(TagPostActivity.this, "서버에 연결이 되지 않았습니다.\n 확인 부탁드립니다.", Toast.LENGTH_SHORT).show();
            }
        });
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
