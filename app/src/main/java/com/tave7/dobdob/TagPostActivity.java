package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonObject;
import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.POST_REQUEST;
import static com.tave7.dobdob.MainActivity.myInfo;

public class TagPostActivity extends AppCompatActivity {
    private String tagName = "";
    private Double locationX = -1.0;
    private Double locationY = -1.0;
    private ArrayList<PostInfoSimple> tagPostLists = null;

    private PostRecyclerAdapter adapter;
    private SwipeRefreshLayout srlPosts;
    private TextView tvPostInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_post);

        tagPostLists = new ArrayList<>();
        tagName = getIntent().getExtras().getString("tagName");
        locationX = getIntent().getExtras().getDouble("locationX");
        locationY = getIntent().getExtras().getDouble("locationY");

        Toolbar toolbar = findViewById(R.id.tagPost_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_tagpost, null);
        actionBar.setCustomView(customView);
        TextView tvTag = toolbar.findViewById(R.id.toolbar_tv);
            tvTag.setText("# ".concat(tagName));
            tvTag.setTextColor(Color.parseColor("#5AAEFF"));

        tvPostInfo = findViewById(R.id.tagPost_postInfo);
        srlPosts = findViewById(R.id.tagPost_swipeRL);
        srlPosts.setDistanceToTriggerSync(400);
        srlPosts.setOnRefreshListener(() -> setTagPost(true));
        RecyclerView rvTagPost = findViewById(R.id.tagPost);
        LinearLayoutManager manager = new LinearLayoutManager(TagPostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvTagPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter('t', tagPostLists);
        rvTagPost.setAdapter(adapter);
        rvTagPost.addItemDecoration(new DividerItemDecoration(TagPostActivity.this, 1));

        setTagPost(false);
    }

    public void setTagPost(boolean isSwipe) {
        tagPostLists.clear();
        adapter.notifyDataSetChanged();

        tvPostInfo.setVisibility(View.VISIBLE);
        tvPostInfo.setText("해당 태그를 가진 글을 찾고 있습니다.");

        JsonObject tagPostInfo = new JsonObject();
        tagPostInfo.addProperty("keyword", tagName);
        tagPostInfo.addProperty("locationX", locationX);
        tagPostInfo.addProperty("locationY", locationY);
        RetrofitClient.getApiService().postTagPost(tagPostInfo).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
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
                            Bitmap writerProfile = null;
                            try {
                                writerProfile = new DownloadFileTask(userObject.getString("profileUrl")).execute().get();
                            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            writerInfo.setUserProfileBM(writerProfile);

                            String postTime = postObject.getString("createdAt");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                            try {
                                Date date = sdf.parse(postTime);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss", Locale.getDefault());
                                postTime = dateFormat.format(Objects.requireNonNull(date));
                            } catch (ParseException e) { e.printStackTrace(); }

                            String title = postObject.getString("title");

                            int isILike = 0;
                            JSONArray likesArray = postObject.getJSONArray("Likes");
                            for (int j=0; j<likesArray.length(); j++) {
                                JSONObject likeObject = likesArray.getJSONObject(j);
                                if (likeObject.getJSONObject("User").getInt("id") == myInfo.getUserID()) {
                                    isILike = 1;
                                    break;
                                }
                            }
                            int likeNum = postObject.getInt("likeCount");
                            int commentNum = postObject.getInt("commentCount");

                            ArrayList<String> tags = new ArrayList<>();
                            JSONArray tagsArray = postObject.getJSONArray("Tags");
                            for (int j=0; j<tagsArray.length(); j++){
                                JSONObject tagObject = tagsArray.getJSONObject(j);
                                tags.add(tagObject.getString("name"));
                            }

                            PostInfoSimple post = new PostInfoSimple(postID, writerInfo, postTime, title, isILike, likeNum, commentNum, tags);
                            tagPostLists.add(post);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                    if (tagPostLists.size() > 0)
                        tvPostInfo.setVisibility(View.GONE);
                    else
                        tvPostInfo.setText("해당 태그를 가진 글이 존재하지 않습니다.\n다른 태그를 검색해 보세요:)");
                    adapter.notifyDataSetChanged();
                }
                else
                    tvPostInfo.setText("글을 로드할 수 없음\n다시 로드해 주세요.");

                if (isSwipe)
                    srlPosts.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isSwipe)
                    srlPosts.setRefreshing(false);

                tvPostInfo.setText("글을 로드할 수 없음\n다시 로드해 주세요.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == POST_REQUEST && resultCode == RESULT_OK) {
            setTagPost(false);
        }
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
