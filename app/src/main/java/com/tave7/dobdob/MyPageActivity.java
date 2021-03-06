package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.POST_REQUEST;
import static com.tave7.dobdob.MainActivity.myInfo;

public class MyPageActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST = 8000;

    int userID = -1;
    boolean isMyPage = true;
    boolean isChangeProfile = false, isChangeName = false, isChangeAddress = false;
    UserInfo otherInfo = null;
    ArrayList<PostInfoSimple> userPostList = null;

    CircleImageView civUserProfile;
    ImageView ivEdit;
    PostRecyclerAdapter adapter;
    RecyclerView rvMyPagePosts;
    TextView tvUserName, tvUserTown, tvUserPosts, tvPostInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        userPostList = new ArrayList<>();
        isMyPage = !getIntent().hasExtra("userID");
        userID = isMyPage? myInfo.getUserID() : getIntent().getExtras().getInt("userID");

        Toolbar toolbar = findViewById(R.id.myPage_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView tvPageInfo = findViewById(R.id.myPage_pageInfo);
            tvPageInfo.setVisibility(View.GONE);
        NestedScrollView nsvPage = findViewById(R.id.myPage_scrollView);
        civUserProfile = findViewById(R.id.myPage_userProfile);
        tvUserName = findViewById(R.id.myPage_userName);
        tvUserTown = findViewById(R.id.myPage_userTown);
        tvUserPosts = findViewById(R.id.myPage_tvUserPost);
        tvPostInfo = findViewById(R.id.myPage_postInfo);
        rvMyPagePosts = findViewById(R.id.myPagePosts);
        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvMyPagePosts.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter('p', userPostList);
        rvMyPagePosts.setAdapter(adapter);
        DividerItemDecoration devider = new DividerItemDecoration(MyPageActivity.this, 1);
        devider.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null)));
        rvMyPagePosts.addItemDecoration(devider);

        if (!isMyPage) {
            nsvPage.setVisibility(View.GONE);
            RetrofitClient.getApiService().getUserInfo(PreferenceManager.getString(this, "jwt"), userID).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    nsvPage.setVisibility(View.VISIBLE);
                    if (response.code() == 200) {
                        try {
                            JSONObject userInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            JSONObject user = userInfo.getJSONObject("user");

                            if (user.isNull("profileUrl"))
                                otherInfo = new UserInfo(userID, null, user.getString("nickName"), user.getJSONObject("Location").getString("dong"));
                            else
                                otherInfo = new UserInfo(userID, user.getString("profileUrl"), user.getString("nickName"), user.getJSONObject("Location").getString("dong"));

                            if (otherInfo.getUserProfileUrl() == null)
                                civUserProfile.setImageResource(R.drawable.user);
                            else {
                                Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                                try {
                                    userProfile = new DownloadFileTask(otherInfo.getUserProfileUrl()).execute().get();
                                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                                civUserProfile.setImageBitmap(userProfile);
                            }
                            tvUserName.setText(otherInfo.getUserName());
                            tvUserTown.setText(otherInfo.getUserTown());
                            tvUserPosts.setText(otherInfo.getUserName().concat(" ?????? ????????? ???"));
                            tvPostInfo.setText(otherInfo.getUserName().concat("?????? ?????? ?????? ????????????."));

                            setWhosePosts(userID);
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else if (response.code() == 419) {
                        Toast.makeText(MyPageActivity.this, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.removeKey(MyPageActivity.this, "jwt");
                        Intent reLogin = new Intent(MyPageActivity.this, LoginActivity.class);
                        reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(reLogin);
                        finish();
                    }
                    else {
                        tvPageInfo.setVisibility(View.VISIBLE);
                        tvPageInfo.setText(getIntent().getExtras().getString("userName").concat("?????? ???????????? ????????? ??? ????????????."));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    tvPageInfo.setVisibility(View.VISIBLE);
                    tvPageInfo.setText(getIntent().getExtras().getString("userName").concat("?????? ???????????? ????????? ??? ????????????."));
                }
            });
        }
        else {   //?????? ???????????? ???????????? ?????? ??????
            @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_mypage, null);
            actionBar.setCustomView(customView);
            toolbarListener(toolbar);

            if (myInfo.getUserProfileUrl() == null)
                civUserProfile.setImageResource(R.drawable.user);
            else {
                Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                try {
                    userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                civUserProfile.setImageBitmap(userProfile);
            }
            tvUserName.setText(myInfo.getUserName());
            tvUserTown.setText(myInfo.getUserTown());
            tvUserPosts.setText(myInfo.getUserName().concat(" ?????? ????????? ???"));
            tvPostInfo.setText(myInfo.getUserName().concat("?????? ?????? ?????? ????????????."));

            setWhosePosts(myInfo.getUserID());
        }
    }

    public void toolbarListener(Toolbar toolbar){
        ivEdit = toolbar.findViewById(R.id.toolbar_edit);
        ivEdit.setOnClickListener(v -> {
            Intent editProfile = new Intent(this, ModifyProfileActivity.class);
            startActivityForResult(editProfile, EDIT_PROFILE_REQUEST);
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

    @Override
    public void finish() {
        if (isMyPage) {
            Intent giveChangedUserInfo = new Intent();
            Bundle bUserInfo = new Bundle();
            if (isChangeProfile || isChangeName || isChangeAddress)
                bUserInfo.putBoolean("isChanged", true);

            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getExtras().getBoolean("isChanged")) {
                setWhosePosts(myInfo.getUserID());
            }
            if (data != null && data.hasExtra("isChangeProfile")) {
                isChangeProfile = true;
                if (myInfo.getUserProfileUrl() == null)
                    civUserProfile.setImageResource(R.drawable.user);
                else {
                    Bitmap userProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                    try {
                        userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    civUserProfile.setImageBitmap(userProfile);
                }
            }
            if (data != null && data.hasExtra("isChangeName")) {
                isChangeName = true;
                tvUserName.setText(myInfo.getUserName());
                tvUserPosts.setText(myInfo.getUserName().concat(" ?????? ????????? ???"));
            }
            if (data != null && data.hasExtra("isChangeAddress")) {
                isChangeAddress = true;
                tvUserTown.setText(myInfo.getUserTown());
            }
        }
        else if (requestCode == POST_REQUEST && resultCode == RESULT_OK) {
            setWhosePosts(userID);
        }
    }

    public void setWhosePosts(int whoseID) {   //?????? user??? post????????? ?????? ?????????
        userPostList.clear();
        adapter.notifyDataSetChanged();

        tvPostInfo.setVisibility(View.VISIBLE);
        if (isMyPage)
            tvPostInfo.setText(myInfo.getUserName().concat("?????? ?????? ?????? ????????????."));
        else
            tvPostInfo.setText(otherInfo.getUserName().concat("?????? ?????? ?????? ????????????."));
        RetrofitClient.getApiService().getUserPosts(PreferenceManager.getString(this, "jwt"), whoseID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                        JSONArray jsonPosts = result.getJSONArray("posts");
                        for (int i=0; i < jsonPosts.length(); i++) {
                            JSONObject postObject = jsonPosts.getJSONObject(i);
                            JSONObject userObject = postObject.getJSONObject("User");
                            JSONObject locationObject = postObject.getJSONObject("Location");

                            int postID = postObject.getInt("id");

                            UserInfo writerInfo;
                            if (userObject.isNull("profileUrl"))
                                writerInfo = new UserInfo(userObject.getInt("id"), null, userObject.getString("nickName"),
                                        locationObject.getString("dong"), locationObject.getDouble("locationX"), locationObject.getDouble("locationY"));

                            else
                                writerInfo = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"),
                                        locationObject.getString("dong"), locationObject.getDouble("locationX"), locationObject.getDouble("locationY"));
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
                            userPostList.add(post);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }

                    if (userPostList.size() > 0) {
                        tvPostInfo.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        if (isMyPage)
                            tvPostInfo.setText(myInfo.getUserName().concat("?????? ????????? ?????? ????????????."));
                        else
                            tvPostInfo.setText(otherInfo.getUserName().concat("?????? ????????? ?????? ????????????."));
                    }
                }
                else if (response.code() == 419) {
                    Toast.makeText(MyPageActivity.this, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    PreferenceManager.removeKey(MyPageActivity.this, "jwt");
                    Intent reLogin = new Intent(MyPageActivity.this, LoginActivity.class);
                    reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(reLogin);
                    finish();
                }
                else {
                    if (isMyPage)
                        tvPostInfo.setText(myInfo.getUserName().concat("?????? ?????? ????????? ??? ??????\n?????? ????????? ?????????."));
                    else
                        tvPostInfo.setText(otherInfo.getUserName().concat("?????? ?????? ????????? ??? ??????\n?????? ????????? ?????????."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isMyPage)
                    tvPostInfo.setText(myInfo.getUserName().concat("?????? ?????? ????????? ??? ??????\n?????? ????????? ?????????."));
                else
                    tvPostInfo.setText(otherInfo.getUserName().concat("?????? ?????? ????????? ??? ??????\n?????? ????????? ?????????."));
            }
        });
    }
}
