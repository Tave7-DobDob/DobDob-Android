package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class MyPageActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST = 8000;

    boolean isMyPage = true;
    boolean isChangeProfile = false, isChangeName = false, isChangeAddress = false;
    UserInfo otherInfo = null;
    ArrayList<PostInfoSimple> userPostList = null;        //user가 올린 글 모음

    CircleImageView civUserProfile;
    ImageView ivEdit;
    PostRecyclerAdapter adapter;
    RecyclerView rvMyPagePosts;
    TextView tvUserName, tvUserTown, tvUserPosts;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        userPostList = new ArrayList<>();
        isMyPage = !getIntent().hasExtra("userID");

        Toolbar toolbar = findViewById(R.id.myPage_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        civUserProfile = findViewById(R.id.myPage_userProfile);
        tvUserName = findViewById(R.id.myPage_userName);
        tvUserTown = findViewById(R.id.myPage_userTown);
        tvUserPosts = findViewById(R.id.myPage_tvUserPost);
        rvMyPagePosts = findViewById(R.id.myPagePosts);
        LinearLayoutManager manager = new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.VERTICAL,false);
        rvMyPagePosts.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(userPostList);
        rvMyPagePosts.setAdapter(adapter);
        DividerItemDecoration devider = new DividerItemDecoration(MyPageActivity.this, 1);
        devider.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null)));
        rvMyPagePosts.addItemDecoration(devider); //리스트 사이의 구분선 설정

        if (!isMyPage) {
            int userID = getIntent().getExtras().getInt("userID");
            RetrofitClient.getApiService().getUserInfo(userID).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("MyPage user정보받기 성공1", response.toString());
                    Log.i("MyPage user정보받기 성공2", response.body());
                    if (response.code() == 200) {
                        try {
                            JSONObject userInfo = new JSONObject(Objects.requireNonNull(response.body()));
                            JSONObject user = userInfo.getJSONObject("user");
                            /*  TODO: location이 완료되면 이걸로 출력해야함!!!
                            otherInfo = new UserInfo(userID, user.getString("profileUrl"), user.getString("nickName"),
                                    user.getJSONObject("location").getString("dong"), user.getJSONObject("location").getString("detail"));
                             */
                            if (user.isNull("profileUrl"))
                                otherInfo = new UserInfo(userID, null, user.getString("nickName"), "역삼동", "강남구 역삼동 200");
                            else
                                otherInfo = new UserInfo(userID, user.getString("profileUrl"), user.getString("nickName"), "역삼동", "강남구 역삼동 200");

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
                            tvUserPosts.setText(otherInfo.getUserName()+" 님이 작성한 글");
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else
                        Toast.makeText(MyPageActivity.this, "다시 한번 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(MyPageActivity.this, "서버에 연결이 되지 않았습니다.\n 확인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                }
            });
            setWhosePosts(userID);
        }
        else {   //실제 사용자의 페이지를 보는 경우
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
            tvUserPosts.setText(myInfo.getUserName()+" 님이 작성한 글");

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
        if (item.getItemId() == android.R.id.home) {//toolbar의 back키 눌렀을 때 동작
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (isMyPage) {
            //TODO: 메인인지를 구별해야 함!!(MainActivity일 때!라는 코드가 있어야 함!)
            Intent giveChangedUserInfo = new Intent();
            Bundle bUserInfo = new Bundle();
            if (isChangeProfile || isChangeName || isChangeAddress)
                bUserInfo.putBoolean("isChanged", true);

            giveChangedUserInfo.putExtras(bUserInfo);
            setResult(RESULT_OK, giveChangedUserInfo);
        }
        super.finish();
    }

    @SuppressLint("SetTextI18n")
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
                tvUserPosts.setText(myInfo.getUserName() + " 님이 작성한 글");
            }
            if (data != null && data.hasExtra("isChangeAddress")) {
                isChangeAddress = true;
                tvUserTown.setText(myInfo.getUserTown());
            }
        }
    }

    private void setWhosePosts(int whoseID) {   //해당 user의 post글들을 모두 받아옴
        RetrofitClient.getApiService().getUserPosts(whoseID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("MyPage user글받기 성공1", response.toString());
                Log.i("MyPage user글받기 성공2", response.body());
                if (response.code() == 200) {
                    userPostList.clear();
                    try {
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                        JSONArray jsonPosts = result.getJSONArray("posts");
                        for (int i=0; i < jsonPosts.length(); i++) {
                            JSONObject postObject = jsonPosts.getJSONObject(i);

                            int postID = postObject.getInt("id");
                            JSONObject userObject = postObject.getJSONObject("User");
                            //TODO: 동네도 넣어야 함!!!!!!!!!!!!!!!!!!!!!!!!
                            UserInfo writerInfo;
                            if (userObject.isNull("profileUrl"))
                                writerInfo = new UserInfo(userObject.getInt("id"), null, userObject.getString("nickName"), "");
                            else
                                writerInfo = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"), "");
                            String postTime = postObject.getString("createdAt");
                            String title = postObject.getString("title");
                            //하트 수
                            int commentNum = 3;     //TODO: 서버로부터 받아와야 함!!
                            //태그 받아옴!
                            PostInfoSimple post = new PostInfoSimple(postID, writerInfo, postTime, title, null, commentNum, null);
                            userPostList.add(post);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                else
                    Toast.makeText(MyPageActivity.this, "해당 유저가 포스트한 글을 다시 한번 받아보세요:)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(MyPageActivity.this, "서버에 연결이 되지 않았습니다.\n 확인 부탁드립니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
