package com.tave7.dobdob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
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

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.InitialSettingActivity.DAUMADDRESS_REQUEST;

public class MainActivity extends AppCompatActivity {
    public static final int POSTING_REQUEST = 6000;
    public static final int POST_REQUEST = 7000;
    public static final int MYPAGE_REQUEST = 8000;

    public static UserInfo myInfo = null;
    private JsonObject location;
    private ArrayList<PostInfoSimple> postList = null;
    private ArrayList<PostInfoSimple> totalPostList = null;
    private boolean isSearchFocus = false;

    private CircleImageView civSubMenuUser;
    private LinearLayout llTown;
    private SearchView svSearch;
    private TextView tvPostInfo, tvTown;
    private SwipeRefreshLayout srlPosts;
    private PostRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_main, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        location = new JsonObject();
        location.addProperty("locationX", myInfo.getLocationX());
        location.addProperty("locationY", myInfo.getLocationY());

        postList = new ArrayList<>();
        totalPostList = new ArrayList<>();

        tvPostInfo = findViewById(R.id.main_postInfo);
        srlPosts = findViewById(R.id.main_swipeRL);
        srlPosts.setDistanceToTriggerSync(400);
        srlPosts.setOnRefreshListener(() -> updatePostList(true));
        RecyclerView rvPost = findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter('m', postList);
        rvPost.setAdapter(adapter);
        DividerItemDecoration devider = new DividerItemDecoration(MainActivity.this, 1);
        devider.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null)));
        rvPost.addItemDecoration(devider);

        FloatingActionButton fabAddPost = findViewById(R.id.mainFabAddPost);
        fabAddPost.setOnClickListener(v -> startActivityForResult(new Intent(MainActivity.this, PostingActivity.class), POSTING_REQUEST));

        updatePostList(false);
    }

    public void toolbarListener(Toolbar toolbar){
        tvTown = toolbar.findViewById(R.id.toolbar_town);
        tvTown.setText(myInfo.getUserTown());

        llTown = toolbar.findViewById(R.id.toolbar_main_town);
        llTown.setOnClickListener(v -> {
            Intent itAddress = new Intent(MainActivity.this, DaumAddressActivity.class);
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        svSearch = findViewById(R.id.toolbar_search);
        svSearch.setQueryHint("제목 및 #태그 검색");
        svSearch.setOnSearchClickListener(v -> {
            isSearchFocus = true;
            llTown.setVisibility(View.GONE);
        });
        svSearch.setOnCloseListener(() -> {
            isSearchFocus = false;

            postList.clear();
            postList.addAll(totalPostList);
            tvPostInfo.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();

            svSearch.onActionViewCollapsed();
            llTown.setVisibility(View.VISIBLE);

            return true;
        });
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().charAt(0) == '#') {   //태그 검색
                    String searchTag = query.trim().substring(1);
                    if (searchTag.length() > 0) {
                        postList.clear();
                        adapter.notifyDataSetChanged();
                        
                        tvPostInfo.setVisibility(View.VISIBLE);
                        tvPostInfo.setText("해당 태그를 가진 글을 찾고 있습니다. \uD83D\uDD0D");
                        RetrofitClient.getApiService().getTagPost(searchTag).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                Log.i("MainA 태그검색 성공", response.body());
                                if (response.code() == 200) {
                                    //TODO: 태그 내용 없을 시에 대한 상황 설정!!!!!!!!!!!!!!!필요!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
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
                                            postList.add(post);
                                        }
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    if (postList.size() > 0)
                                        tvPostInfo.setVisibility(View.GONE);
                                    else
                                        tvPostInfo.setText("동네의 글이 아직 없습니다.\n글을 작성해 보세요:)");
                                    adapter.notifyDataSetChanged();
                                }
                                else
                                    tvPostInfo.setText("해당 태그 글 로드할 수 없음\n다시 로드해 주세요.");
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                tvPostInfo.setText("해당 태그 글 로드할 수 없음\n다시 로드해 주세요.");
                            }
                        });
                    }
                    else
                        Toast.makeText(MainActivity.this, "검색할 내용을 입력하세요:)", Toast.LENGTH_SHORT).show();
                }
                else if (query.trim().length() > 0) {   //제목 검색
                    postList.clear();
                    adapter.notifyDataSetChanged();

                    tvPostInfo.setVisibility(View.VISIBLE);
                    tvPostInfo.setText("해당 제목을 가진 글을 찾고 있습니다. \uD83D\uDD0D");
                    RetrofitClient.getApiService().getTitlePost(query.trim()).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            Log.i("MainA 제목검색 성공", response.body());
                            if (response.code() == 200) {
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
                                        postList.add(post);
                                    }
                                } catch (JSONException e) { e.printStackTrace(); }
                                if (postList.size() > 0)
                                    tvPostInfo.setVisibility(View.GONE);
                                else
                                    tvPostInfo.setText("동네의 글이 아직 없습니다.\n글을 작성해 보세요:)");
                                adapter.notifyDataSetChanged();
                            }
                            else
                                tvPostInfo.setText("해당 제목 글 로드할 수 없음\n다시 로드해 주세요.");
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            tvPostInfo.setText("해당 제목 글 로드할 수 없음\n다시 로드해 주세요.");
                        }
                    });
                }
                else
                    Toast.makeText(MainActivity.this, "검색할 내용을 입력하세요:)", Toast.LENGTH_SHORT).show();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    postList.clear();
                    postList.addAll(totalPostList);
                    tvPostInfo.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        civSubMenuUser = findViewById(R.id.toolbar_subMenuUser);
        if (myInfo.getUserProfileUrl() == null)
            civSubMenuUser.setImageResource(R.drawable.user);
        else {
            Bitmap userProfile = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.user, null))).getBitmap();
            try {
                userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            civSubMenuUser.setImageBitmap(userProfile);
        }
        civSubMenuUser.setOnClickListener(view -> {
            final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
            getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.mypage)
                    startActivityForResult(new Intent(MainActivity.this, MyPageActivity.class), MYPAGE_REQUEST);
                else {      //로그아웃
                    PreferenceManager.removeKey(MainActivity.this, "access_token");         //어세스 토크 삭제 TODO: 수정 요망!!!!!!!!!!
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                    finish();
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public void onBackPressed() {
        if (isSearchFocus) {
            postList.clear();
            postList.addAll(totalPostList);
            adapter.notifyDataSetChanged();

            svSearch.onActionViewCollapsed();
            llTown.setVisibility(View.VISIBLE);

            isSearchFocus = false;
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == MYPAGE_REQUEST) {        //MyPage에서 User 정보 변경 시 적용 위함
                if (Objects.requireNonNull(data).hasExtra("isChanged")) {
                    if (myInfo.getUserProfileUrl() == null)
                        civSubMenuUser.setImageResource(R.drawable.user);
                    else {
                        Bitmap userProfile = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.user, null))).getBitmap();
                        try {
                            userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                        } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                        civSubMenuUser.setImageBitmap(userProfile);
                    }
                    if (location.get("locationX").getAsDouble() != myInfo.getLocationX() || location.get("locationY").getAsDouble() != myInfo.getLocationY()) {
                        location.remove("locationX");
                        location.addProperty("locationX", myInfo.getLocationX());
                        location.remove("locationY");
                        location.addProperty("locationY", myInfo.getLocationY());
                    }
                    tvTown.setText(myInfo.getUserTown());
                    updatePostList(false);
                }
            }
            else if (requestCode == POST_REQUEST || requestCode == POSTING_REQUEST) {
                updatePostList(false);
            }
            else if (requestCode == DAUMADDRESS_REQUEST) {
                new GetGEOTask(this, "main", Objects.requireNonNull(data).getExtras().getString("address")).execute();
            }
        }
    }

    public void mainSettingTown(JsonObject loc) {
        location = loc;
        tvTown.setText(loc.get("dong").getAsString());

        updatePostList(false);
    }

    public void updatePostList(boolean isSwipe) {
        postList.clear();
        adapter.notifyDataSetChanged();

        tvPostInfo.setVisibility(View.VISIBLE);
        tvPostInfo.setText("동네의 글을 찾고 있습니다. \uD83D\uDD0D");
        RetrofitClient.getApiService().postLocationPost(location).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("MainA 전체글 새로고침 성공", response.body());
                if (response.code() == 200) {
                    totalPostList.clear();
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
                            totalPostList.add(post);
                        }
                        postList.addAll(totalPostList);
                    } catch (JSONException e) { e.printStackTrace(); }
                    if (postList.size() > 0)
                        tvPostInfo.setVisibility(View.GONE);
                    else
                        tvPostInfo.setText("동네의 글이 아직 없습니다.\n글을 작성해 보세요:)");

                    adapter.notifyDataSetChanged();
                }
                else
                    tvPostInfo.setText("동네의 글 로드할 수 없음\n다시 로드해 주세요.");

                if (isSwipe)
                    srlPosts.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isSwipe)
                    srlPosts.setRefreshing(false);

                tvPostInfo.setText("동네의 글 로드할 수 없음\n다시 로드해 주세요.");
            }
        });
    }
}
