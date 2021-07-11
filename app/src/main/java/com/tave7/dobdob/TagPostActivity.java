package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class TagPostActivity extends AppCompatActivity {
    private String tagName = "";
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

        Toolbar toolbar = findViewById(R.id.tagPost_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_tagpost, null);
        actionBar.setCustomView(customView);
        TextView tvTag = toolbar.findViewById(R.id.toolbar_tag);
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
        tvPostInfo.setText("해당 태그를 가진 글을 찾고 있습니다. \uD83D\uDD0D");
        RetrofitClient.getApiService().getTagPost(tagName).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("TagPostA 태그검색 성공", response.body());
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
                            else {
                                writerInfo = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"), postObject.getJSONObject("Location").getString("dong"));
                                Bitmap writerProfile;
                                try {
                                    writerProfile = new DownloadFileTask(userObject.getString("profileUrl")).execute().get();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    writerProfile.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    writerInfo.setUserProfile(stream.toByteArray());
                                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            }
                            String postTime = postObject.getString("createdAt");
                            String title = postObject.getString("title");

                            ArrayList<UserInfo> likes = new ArrayList<>();
                            int myLikePos = -1;
                            JSONArray likesArray = postObject.getJSONArray("Likes");
                            for (int j=0; j<likesArray.length(); j++) {
                                JSONObject likeObject = likesArray.getJSONObject(j);
                                JSONObject likeUserObject = likeObject.getJSONObject("User");
                                UserInfo likeUser = new UserInfo(likeUserObject.getInt("id"), likeUserObject.getString("profileUrl"), likeUserObject.getString("nickName"));
                                likes.add(likeUser);

                                if (likeUserObject.getInt("id") == myInfo.getUserID())
                                    myLikePos = j;
                            }
                            int commentNum = postObject.getInt("commentCount");

                            ArrayList<String> tags = new ArrayList<>();
                            JSONArray tagsArray = postObject.getJSONArray("Tags");
                            for (int j=0; j<tagsArray.length(); j++){
                                JSONObject tagObject = tagsArray.getJSONObject(j);
                                tags.add(tagObject.getString("name"));
                            }

                            PostInfoSimple post = new PostInfoSimple(postID, writerInfo, postTime, title, myLikePos, likes, commentNum, tags);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
