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
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class PostActivity extends AppCompatActivity {
    public static final int POST_EDIT_REQUEST = 7500;   //requestCode로 사용될 상수(글 수정)

    int postID = -1;
    PostInfoDetail postInfoDetail;
    Menu menu;
    boolean isWriter = false;
    boolean isClickedHeart = false;
    boolean isDeleted = false, isEdited = false;          //MainActivity에 전달해야 함(글을 삭제했는 지)

    private CommentRecyclerAdapter commentAdapter;
    private FrameLayout flImages;
    private ImageView ivHeart;
    private NestedScrollView svEntirePost;
    private FlowLayout flTags;
    private PostPhotosPagerAdapter photoAdapter;
    private SwipeRefreshLayout srlPost;
    private TextView tvTitle, tvContent, tvHeartNums, tvCommentNums, tvNoComment;
    
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostInfoSimple postInfo = getIntent().getParcelableExtra("postInfo");
        postID = postInfo.getPostID();
        postInfoDetail = new PostInfoDetail(postInfo);
        isWriter = myInfo.getUserName().equals(postInfoDetail.getPostInfoSimple().getWriterName());

        Toolbar toolbar = findViewById(R.id.post_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

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
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            try {
                Date date = sdf.parse(postInfoDetail.getPostInfoSimple().getPostTime());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                String dateString = dateFormat.format(Objects.requireNonNull(date));
                tvPostTime.setText(dateString);
            } catch (ParseException e) { e.printStackTrace(); }
        tvContent = findViewById(R.id.post_content);

        flImages = findViewById(R.id.post_flImages);
        CircleIndicator3 indicator = findViewById(R.id.indicator);
        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        photoAdapter = new PostPhotosPagerAdapter(postInfoDetail.getPostImages());
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
                Log.i("PostA 글 성공", response.body());
                if (response.code() == 200) {
                    postInfoDetail.getPostImages().clear();
                    postInfoDetail.getComments().clear();
                    try {
                        JSONObject postInfo = new JSONObject(Objects.requireNonNull(response.body())).getJSONObject("post");
                        if (!postInfoDetail.getPostInfoSimple().getPostTitle().equals(postInfo.getString("title"))){
                            postInfoDetail.getPostInfoSimple().setPostTitle(postInfo.getString("title"));
                            tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                        }
                        postInfoDetail.setPostContent(postInfo.getString("content"));

                        JSONArray postImages = postInfo.getJSONArray("PostImages");
                        for (int i=0; i<postImages.length(); i++) {
                            postInfoDetail.getPostImages().add(postImages.getJSONObject(i).getString("url"));
                        }

                        JSONArray tagsArray = postInfo.getJSONArray("Tags");
                        postInfoDetail.getPostInfoSimple().getPostTag().clear();
                        for (int i=0; i<tagsArray.length(); i++) {
                            JSONObject tagObject = tagsArray.getJSONObject(i);
                            postInfoDetail.getPostInfoSimple().getPostTag().add(tagObject.getString("name"));
                        }

                        JSONArray likesArray = postInfo.getJSONArray("Likes");
                        postInfoDetail.getLikes().clear();
                        for (int i=0; i<likesArray.length(); i++) {
                            JSONObject likeObject = likesArray.getJSONObject(i);
                            JSONObject userObject = likeObject.getJSONObject("User");
                            UserInfo likeUser = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"));
                            postInfoDetail.getLikes().add(likeUser);

                            if (likeUser.getUserName().equals(myInfo.getUserName()))
                                isClickedHeart = true;
                        }
                        if (postInfoDetail.getPostInfoSimple().getLikeNum() != postInfoDetail.getLikes().size())
                            postInfoDetail.getPostInfoSimple().setLikeNum(postInfoDetail.getLikes().size());

                        JSONArray commentArray = postInfo.getJSONArray("Comments");
                        postInfoDetail.getComments().clear();
                        for (int i=0; i<commentArray.length(); i++) {
                            JSONObject commentObject = commentArray.getJSONObject(i);
                            JSONObject commenterObject = commentObject.getJSONObject("User");
                            UserInfo commenterInfo;
                            if (commenterObject.isNull("profileUrl"))
                                commenterInfo = new UserInfo(commenterObject.getInt("id"), null, commenterObject.getString("nickName"), commenterObject.getJSONObject("Location").getString("dong"));
                            else {
                                commenterInfo = new UserInfo(commenterObject.getInt("id"), commenterObject.getString("profileUrl"), commenterObject.getString("nickName"), commenterObject.getJSONObject("Location").getString("dong"));
                                Bitmap commenterProfile;
                                try {
                                    commenterProfile = new DownloadFileTask(commenterObject.getString("profileUrl")).execute().get();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    commenterProfile.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    commenterInfo.setUserProfile(stream.toByteArray());
                                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                            }

                            CommentInfo comment = new CommentInfo(commentObject.getInt("id"), commenterInfo, commentObject.getString("createdAt"), commentObject.getString("content"));
                            postInfoDetail.getComments().add(comment);
                        }
                        if (postInfoDetail.getPostInfoSimple().getCommentNum() != postInfoDetail.getComments().size())
                            postInfoDetail.getPostInfoSimple().setCommentNum(postInfoDetail.getComments().size());
                    } catch (JSONException e) { e.printStackTrace(); }

                    tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                    tvContent.setText(postInfoDetail.getPostContent());

                    if (postInfoDetail.getPostImages().size() == 0)
                        flImages.setVisibility(View.GONE);
                    else {
                        flImages.setVisibility(View.VISIBLE);
                        photoAdapter.notifyDataSetChanged();
                    }

                    if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우
                        ivHeart.setImageResource(R.drawable.heart_click);
                    else
                        ivHeart.setImageResource(R.drawable.heart);
                    tvHeartNums.setText(String.valueOf(postInfoDetail.getLikes().size()));
                    //tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getLikeNum()));       //TODO: 수정 요망!
                    //tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));
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
            if (myInfo.getUserID() != postInfoDetail.getPostInfoSimple().getWriterID())
                sppBundle.putInt("userID", postInfoDetail.getPostInfoSimple().getWriterID());
            showProfilePage.putExtras(sppBundle);
            startActivity(showProfilePage);
        });

        //하트 클릭 시
        ivHeart.setOnClickListener(v -> {
            if (isClickedHeart) {
                Log.i("확인용", "이전에 클릭된 하트였음");

                RetrofitClient.getApiService().deleteIDLike(myInfo.getUserID(), postID).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 좋아요취소 성공", response.toString());
                        Log.i("PostA 좋아요취소 성공", response.body());

                        if (response.code() == 200) {
                            showPost(false);
                        }
                        else
                            Toast.makeText(PostActivity.this, "죄송합니다. 다시 시도해 주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Log.i("확인용", "이전에 클릭되지 않은 하트였음");

                //TODO: MainActivity에서도 변경된 값을 갖고 있도록 해야함??!!
                JsonObject likeInfo = new JsonObject();
                likeInfo.addProperty("userId", myInfo.getUserID());
                likeInfo.addProperty("postId", postID);
                RetrofitClient.getApiService().postLike(likeInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 좋아요 성공", response.toString());
                        Log.i("PostA 좋아요 성공", response.body());

                        if (response.code() == 201) {
                            showPost(false);
                        }
                        else
                            Toast.makeText(PostActivity.this, "죄송합니다. 다시 시도해 주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(PostActivity.this, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

        TextView tvAddComment = findViewById(R.id.post_postComment);    //댓글 추가
        tvAddComment.setOnClickListener(v -> {
            etWriteComment.setEnabled(false);

            String writeComment = etWriteComment.getText().toString().trim();
            writeComment = writeComment.concat(" ");
            if (writeComment.length() != 0) {
                JsonObject commentInfo = new JsonObject();
                commentInfo.addProperty("postId", postID);
                commentInfo.addProperty("userId", myInfo.getUserID());
                commentInfo.addProperty("content", writeComment);
                RetrofitClient.getApiService().postComment(commentInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 댓글 post 성공", response.body());
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
                showContainTagPost.putExtras(sctBundle);
                startActivity(showContainTagPost);
                finish();
            });
        }
    }

    @Override
    public void finish() {
        //변경 내용이 있다면 보내줌(있으면 true 전달, 없으면 false 전달 -> Main에서 처리하게 함!, MyPage에서 처리!)
        Intent returnIntent = new Intent();
        if (isDeleted || isEdited)       //글 삭제 or 글 수정인 경우
            setResult(RESULT_OK, returnIntent);
        else
            setResult(RESULT_CANCELED, returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.menu = menu;
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
                            Log.i("PostA 글 삭제성공", response.toString());
                            Log.i("PostA 글 삭제성공2", response.body());
                            if (response.code() == 200) {
                                isDeleted = true;
                                finish();
                            }
                            else {
                                Toast.makeText(PostActivity.this, "해당 글 삭제에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
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
                    Log.i("PostA 글 성공", response.body());
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
