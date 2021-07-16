package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.JsonObject;
import com.nex3z.flowlayout.FlowLayout;
import com.tave7.dobdob.adapter.CommentRecyclerAdapter;
import com.tave7.dobdob.adapter.PostPhotosPagerAdapter;
import com.tave7.dobdob.data.CommentInfo;
import com.tave7.dobdob.data.PostInfoDetail;
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
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class PostActivity extends AppCompatActivity {
    public static final int POST_EDIT_REQUEST = 7500;       //requestCode로 사용될 상수(글 수정)

    int postID = -1;
    boolean isWriter = false;
    boolean isDeleted = false, isEdited = false;
    PostInfoDetail postInfoDetail;
    ArrayList<Bitmap> postImagesBM = null;

    private CommentRecyclerAdapter commentAdapter;
    private FrameLayout flImages;
    private ImageView ivHeart;
    private NestedScrollView svEntirePost;
    private FlowLayout flTags;
    private PostPhotosPagerAdapter photoAdapter;
    private SwipeRefreshLayout srlPost;
    private TextView tvTitle, tvContent, tvHeartNums, tvCommentNums, tvNoComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostInfoSimple postInfo = getIntent().getParcelableExtra("postInfo");
        postID = postInfo.getPostID();
        postInfoDetail = new PostInfoDetail(postInfo);
        if (myInfo.getUserID() == postInfoDetail.getPostInfoSimple().getWriterID())
            isWriter = true;

        Toolbar toolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

        srlPost = findViewById(R.id.post_swipeRL);
        srlPost.setDistanceToTriggerSync(400);
        srlPost.setOnRefreshListener(() -> showPost(true));
        svEntirePost = findViewById(R.id.post_scrollView);
        CircleImageView civWriterProfile = findViewById(R.id.post_writerProfile);
        if (postInfoDetail.getPostInfoSimple().getWriterProfileUrl() != null) {
            Bitmap writerProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
            try {
                writerProfile = new DownloadFileTask(postInfoDetail.getPostInfoSimple().getWriterProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            civWriterProfile.setImageBitmap(writerProfile);
        }
        TextView tvWriterName = findViewById(R.id.post_writerName);
            tvWriterName.setText(postInfoDetail.getPostInfoSimple().getWriterName());
        TextView tvWriterTown = findViewById(R.id.post_writerTown);
            tvWriterTown.setText(postInfoDetail.getPostInfoSimple().getWriterTown());
        tvTitle = findViewById(R.id.post_title);
        TextView tvPostTime = findViewById(R.id.post_time);
            tvPostTime.setText(postInfoDetail.getPostInfoSimple().getPostTime());
        tvContent = findViewById(R.id.post_content);
            tvContent.setVisibility(View.VISIBLE);

        postImagesBM = new ArrayList<>();
        flImages = findViewById(R.id.post_flImages);
            flImages.setVisibility(View.VISIBLE);
        CircleIndicator3 indicator = findViewById(R.id.indicator);
        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        photoAdapter = new PostPhotosPagerAdapter(postImagesBM);
        viewpager2.setAdapter(photoAdapter);
        indicator.setViewPager(viewpager2);
        photoAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        ivHeart = findViewById(R.id.post_ivHeart);
        tvHeartNums = findViewById(R.id.post_heartNum);
        tvCommentNums = findViewById(R.id.post_commentNum);
        flTags = findViewById(R.id.post_flTags);

        tvNoComment = findViewById(R.id.postNoComment);
        RecyclerView rvComments = findViewById(R.id.postComments);
        LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvComments.setLayoutManager(manager);
        commentAdapter = new CommentRecyclerAdapter(postInfoDetail.getComments());
        rvComments.setAdapter(commentAdapter);
        rvComments.addItemDecoration(new DividerItemDecoration(PostActivity.this, 1));

        CircleImageView civCommenterProfile = findViewById(R.id.post_commenterProfile);
        if (myInfo.getUserProfileUrl() != null) {
            Bitmap myProfile = BitmapFactory.decodeResource(getResources(), R.drawable.user);
            try {
                myProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            civCommenterProfile.setImageBitmap(myProfile);
        }

        showPost(false);
        postClickListener();
    }

    public void showPost(boolean isSwipe) {
        RetrofitClient.getApiService().getIDPost(postID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    postInfoDetail.getPostImages().clear();
                    postInfoDetail.getComments().clear();
                    try {
                        JSONObject postInfo = new JSONObject(Objects.requireNonNull(response.body())).getJSONObject("post");
                        postInfoDetail.getPostInfoSimple().getWriterInfo().setUserTown(postInfo.getJSONObject("Location").getString("dong"));
                        postInfoDetail.getPostInfoSimple().getWriterInfo().setLocationX(postInfo.getJSONObject("Location").getDouble("locationX"));
                        postInfoDetail.getPostInfoSimple().getWriterInfo().setLocationY(postInfo.getJSONObject("Location").getDouble("locationY"));

                        if (!postInfoDetail.getPostInfoSimple().getPostTitle().equals(postInfo.getString("title"))){
                            postInfoDetail.getPostInfoSimple().setPostTitle(postInfo.getString("title"));
                            tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                        }
                        postInfoDetail.setPostContent(postInfo.getString("content"));

                        JSONArray postImages = postInfo.getJSONArray("PostImages");
                        for (int i=0; i<postImages.length(); i++) {
                            postInfoDetail.getPostImages().add(postImages.getJSONObject(i).getString("url"));
                            Bitmap postImageBM = null;
                            try {
                                postImageBM = new DownloadFileTask(postImages.getJSONObject(i).getString("url")).execute().get();
                            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            postImagesBM.add(postImageBM);
                        }

                        JSONArray tagsArray = postInfo.getJSONArray("Tags");
                        postInfoDetail.getPostInfoSimple().getPostTag().clear();
                        for (int i=0; i<tagsArray.length(); i++) {
                            JSONObject tagObject = tagsArray.getJSONObject(i);
                            postInfoDetail.getPostInfoSimple().getPostTag().add(tagObject.getString("name"));
                        }

                        JSONArray likesArray = postInfo.getJSONArray("Likes");
                        for (int j=0; j<likesArray.length(); j++) {
                            JSONObject likeObject = likesArray.getJSONObject(j);
                            if (likeObject.getJSONObject("User").getInt("id") == myInfo.getUserID()) {
                                postInfoDetail.getPostInfoSimple().setIsILike(1);
                                break;
                            }
                        }
                        postInfoDetail.getPostInfoSimple().setLikeNum(postInfo.getInt("likeCount"));

                        JSONArray commentArray = postInfo.getJSONArray("Comments");
                        postInfoDetail.getComments().clear();
                        for (int i=commentArray.length()-1; i>=0; i--) {
                            JSONObject commentObject = commentArray.getJSONObject(i);

                            JSONObject commenterObject = commentObject.getJSONObject("User");
                            UserInfo commenterInfo;
                            if (commenterObject.isNull("profileUrl"))
                                commenterInfo = new UserInfo(commenterObject.getInt("id"), null, commenterObject.getString("nickName"), commenterObject.getJSONObject("Location").getString("dong"));
                            else
                                commenterInfo = new UserInfo(commenterObject.getInt("id"), commenterObject.getString("profileUrl"), commenterObject.getString("nickName"), commenterObject.getJSONObject("Location").getString("dong"));
                            Bitmap commenterProfile = null;
                            try {
                                commenterProfile = new DownloadFileTask(commenterObject.getString("profileUrl")).execute().get();
                            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            commenterInfo.setUserProfileBM(commenterProfile);

                            String commentTime = commentObject.getString("createdAt");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                            try {
                                Date date = sdf.parse(commentTime);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss", Locale.getDefault());
                                commentTime = dateFormat.format(Objects.requireNonNull(date));
                            } catch (ParseException e) { e.printStackTrace(); }

                            CommentInfo comment = new CommentInfo(commentObject.getInt("id"), commenterInfo, commentTime, commentObject.getString("content").concat(" "));
                            postInfoDetail.getComments().add(comment);
                        }
                        if (postInfoDetail.getPostInfoSimple().getCommentNum() != postInfoDetail.getComments().size())
                            postInfoDetail.getPostInfoSimple().setCommentNum(postInfoDetail.getComments().size());
                    } catch (JSONException e) { e.printStackTrace(); }

                    tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                    tvContent.setVisibility(View.VISIBLE);
                    tvContent.setText(postInfoDetail.getPostContent());

                    if (postInfoDetail.getPostImages().size() == 0)
                        flImages.setVisibility(View.GONE);
                    else {
                        flImages.setVisibility(View.VISIBLE);
                        photoAdapter.notifyDataSetChanged();
                    }

                    if (postInfoDetail.getPostInfoSimple().getIsILike() == 1)
                        ivHeart.setImageResource(R.drawable.heart_click);
                    else
                        ivHeart.setImageResource(R.drawable.heart);
                    tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getLikeNum()));
                    tvCommentNums.setText(String.valueOf(postInfoDetail.getComments().size()));

                    if (postInfoDetail.getPostInfoSimple().getPostTag().size() > 0) {
                        flTags.setVisibility(View.VISIBLE);
                        settingTags();
                    }
                    else
                        flTags.setVisibility(View.GONE);

                    if (postInfoDetail.getComments().size() <= 0)
                        tvNoComment.setVisibility(View.VISIBLE);
                    else
                        tvNoComment.setVisibility(View.GONE);

                    commentAdapter.notifyDataSetChanged();
                }
                else
                    Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();

                if (isSwipe)
                    srlPost.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isSwipe)
                    srlPost.setRefreshing(false);
                Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void postClickListener(){
        ConstraintLayout clWhole = findViewById(R.id.post_wholeCL);
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        TextView tvWriterName = findViewById(R.id.post_writerName);
        tvWriterName.setOnClickListener(v -> {
            Intent showProfilePage = new Intent(PostActivity.this, MyPageActivity.class);
            Bundle sppBundle = new Bundle();
            if (myInfo.getUserID() != postInfoDetail.getPostInfoSimple().getWriterID()) {
                sppBundle.putInt("userID", postInfoDetail.getPostInfoSimple().getWriterID());
                sppBundle.putString("userName", postInfoDetail.getPostInfoSimple().getWriterName());
            }
            showProfilePage.putExtras(sppBundle);
            startActivity(showProfilePage);
        });

        ivHeart.setOnClickListener(v -> {
            if (postInfoDetail.getPostInfoSimple().getIsILike() == 1) {
                RetrofitClient.getApiService().deleteIDLike(myInfo.getUserID(), postID).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            postInfoDetail.getPostInfoSimple().setIsILike(0);
                            postInfoDetail.getPostInfoSimple().setLikeNum(postInfoDetail.getPostInfoSimple().getLikeNum()-1);

                            ivHeart.setImageResource(R.drawable.heart);
                            tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getLikeNum()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                JsonObject likeInfo = new JsonObject();
                likeInfo.addProperty("userId", myInfo.getUserID());
                likeInfo.addProperty("postId", postID);
                RetrofitClient.getApiService().postLike(likeInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 201) {
                            postInfoDetail.getPostInfoSimple().setIsILike(1);
                            postInfoDetail.getPostInfoSimple().setLikeNum(postInfoDetail.getPostInfoSimple().getLikeNum()+1);

                            ivHeart.setImageResource(R.drawable.heart_click);
                            tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getLikeNum()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        LinearLayout llLike = findViewById(R.id.post_lllike);
        llLike.setOnClickListener(v -> {
            Intent showLikeUsers = new Intent(PostActivity.this, LikeUserActivity.class);
            Bundle sluBundle = new Bundle();
                sluBundle.putInt("postID", postID);
            showLikeUsers.putExtras(sluBundle);
            startActivity(showLikeUsers);
        });

        EditText etWriteComment = findViewById(R.id.post_etComment);
        etWriteComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                //TODO: mention이름을 저장해야 함
                if (s.length() != 0 && s.charAt(s.length()-1) == '@') {
                    //사용자들의 id를 filter를 사용해 드롭다운으로 보여줘야함
                }
            }
        });

        TextView tvAddComment = findViewById(R.id.post_postComment);
        tvAddComment.setOnClickListener(v -> {
            etWriteComment.setEnabled(false);

            String writeComment = etWriteComment.getText().toString().trim();
            if (writeComment.length() > 0) {
                JsonObject commentInfo = new JsonObject();
                commentInfo.addProperty("postId", postID);
                commentInfo.addProperty("userId", myInfo.getUserID());
                commentInfo.addProperty("content", writeComment);
                RetrofitClient.getApiService().postComment(commentInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        etWriteComment.setEnabled(true);
                        if (response.code() == 201) {
                            showPost(false);
                            etWriteComment.setText("");
                            svEntirePost.post(() -> svEntirePost.fullScroll(View.FOCUS_DOWN));
                        }
                        else
                            Toast.makeText(PostActivity.this, "죄송합니다. 다시 한번 댓글을 전송해주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        etWriteComment.setEnabled(true);
                        Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void settingTags() {
        flTags.removeAllViews();
        for (String tagName : postInfoDetail.getPostInfoSimple().getPostTag()){
            TextView tvTag = new TextView(PostActivity.this);
            tvTag.setText("#".concat(tagName));
            tvTag.setTypeface(null, Typeface.NORMAL);
            tvTag.setTextColor(Color.parseColor("#1b73d8"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTag.setLayoutParams(layoutParams);
            flTags.addView(tvTag);

            tvTag.setOnClickListener(v -> {
                String searchTag = tvTag.getText().toString().substring(1);

                Intent showContainTagPost = new Intent(PostActivity.this, TagPostActivity.class);
                Bundle sctBundle = new Bundle();
                    sctBundle.putString("tagName", searchTag);
                    sctBundle.putDouble("locationX", postInfoDetail.getPostInfoSimple().getWriterInfo().getLocationX());
                    sctBundle.putDouble("locationY", postInfoDetail.getPostInfoSimple().getWriterInfo().getLocationY());
                showContainTagPost.putExtras(sctBundle);
                startActivity(showContainTagPost);
            });
        }
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        if (isDeleted || isEdited)
            setResult(RESULT_OK, returnIntent);
        else
            setResult(RESULT_CANCELED, returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (isWriter) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.post_menu, menu);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.postEdit: {
                Intent editPost = new Intent(PostActivity.this, PostingActivity.class);
                Bundle epBundle = new Bundle();
                    epBundle.putBoolean("isEditingPost", true);
                    epBundle.putParcelable("postInfo", postInfoDetail);
                editPost.putExtras(epBundle);
                startActivityForResult(editPost, POST_EDIT_REQUEST);

                return true;
            }
            case R.id.postDelete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("글 삭제").setMessage("현재 글을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", (dialog, which) ->
                    RetrofitClient.getApiService().deleteIDPost(postID).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.code() == 200) {
                                isDeleted = true;
                                finish();
                            }
                            else
                                Toast.makeText(PostActivity.this, "해당 글 삭제에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                        }
                    }));
                builder.setNegativeButton("취소", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(dialogInterface -> {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.yellow2));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.yellow2));
                });
                alertDialog.show();

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == POST_EDIT_REQUEST && resultCode == RESULT_OK) {
            isEdited = true;
            RetrofitClient.getApiService().getIDPost(postID).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200)
                        showPost(false);
                    else
                        Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
