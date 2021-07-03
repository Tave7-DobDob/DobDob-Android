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
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.JsonObject;
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
import java.util.Calendar;
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
    boolean isClickedHeart = false;     //현재 글 수정중인지 || 현재 글에 대해 하트를 눌렀는 지
    boolean isDeleted = false;          //MainActivity에 전달해야 함(글을 삭제했는 지)

    CircleIndicator3 indicator;
    CommentRecyclerAdapter commentAdapter;
    ImageView ivHeart;
    LinearLayout llTag, llWriteComment;
    NestedScrollView svEntirePost;
    PostPhotosPagerAdapter photoAdapter;
    RecyclerView rvComments;
    TextView tvWriterTown, tvTitle, tvContent, tvHeartNums, tvCommentNums;
    
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
        tvWriterTown = findViewById(R.id.post_writerTown);
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
        indicator = findViewById(R.id.indicator);
        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        ivHeart = findViewById(R.id.post_ivHeart);
        tvHeartNums = findViewById(R.id.post_heartNum);
        tvCommentNums = findViewById(R.id.post_commentNum);
        llTag = findViewById(R.id.post_LinearTag);
        rvComments = findViewById(R.id.postComments);
        llWriteComment = findViewById(R.id.post_writeCommentL);

        RetrofitClient.getApiService().getIDPost(postID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("PostA 글 성공", response.toString());
                Log.i("PostA 글 성공2", response.body());
                if (response.code() == 200) {
                    postInfoDetail.getPostImages().clear();
                    //postInfoDetail.getComments().clear();
                    try {
                        JSONObject postInfo = new JSONObject(Objects.requireNonNull(response.body())).getJSONObject("post");
                        if (!postInfoDetail.getPostInfoSimple().getPostTitle().equals(postInfo.getString("title"))){
                            postInfoDetail.getPostInfoSimple().setPostTitle(postInfo.getString("title"));
                            tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                        }
                        //if (!postInfoDetail.getPostInfoSimple().getHeartUsers())        TODO: 하트 수가 다를 시!
                        //      postInfoDetail.getPostInfoSimple().getHeartUsers =
                        //if (!postInfoDetail.getPostInfoSimple().getPostTag())           TODO: 태그 전체를 비교 시, 다를 시에는 추가함!
                        postInfoDetail.setPostContent(postInfo.getString("content"));
                        JSONArray postImages = postInfo.getJSONArray("PostImages");
                        for (int i=0; i<postImages.length(); i++) {
                            postInfoDetail.getPostImages().add(postImages.getJSONObject(i).getString("url"));
                        }
                        //if (!postInfoDetail.getPostInfoSimple().getCommentNum())        TODO: 댓글 저장해야 함!
                    } catch (JSONException e) { e.printStackTrace(); }

                    tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
                    tvContent.setText(postInfoDetail.getPostContent());

                    if (postInfoDetail.getPostImages().size() == 0) {
                        FrameLayout flImages = findViewById(R.id.post_flImages);
                        flImages.setVisibility(View.GONE);
                    }
                    else {
                        photoAdapter = new PostPhotosPagerAdapter(postInfoDetail.getPostImages());
                        viewpager2.setAdapter(photoAdapter);
                        indicator.setViewPager(viewpager2);
                        photoAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
                    }

                    for (String user: postInfoDetail.getPostInfoSimple().getHeartUsers()) {     //TODO: 하트 확인!!!!!!!!!!!!!!!!
                        if (user.equals(myInfo.getUserName())) {
                            isClickedHeart = true;
                            break;
                        }
                    }
                    if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우
                        ivHeart.setImageResource(R.drawable.heart_click);
                    else
                        ivHeart.setImageResource(R.drawable.heart);
                    tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));
                    tvCommentNums.setText(String.valueOf(postInfoDetail.getComments().size()));

                    if (postInfoDetail.getPostInfoSimple().getPostTag().size() > 0)
                        settingTags();
                    else
                        llTag.setVisibility(View.GONE);

                    LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
                    rvComments.setLayoutManager(manager);
                    commentAdapter = new CommentRecyclerAdapter(postInfoDetail.getComments());
                    rvComments.setAdapter(commentAdapter);      //어댑터 등록
                    rvComments.addItemDecoration(new DividerItemDecoration(PostActivity.this, 1));
                }
                else {
                    Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("PostA 서버 연결실패", t.getMessage());
                Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        //TODO: 임시 comment들 생성
            postInfoDetail.getComments().add(new CommentInfo(1, new UserInfo(1, null, "테이비1", "한남동", ""), "2021-07-03T07:13:23.000Z", "@tave1 첫 번째 댓글입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(2, new UserInfo(1, null, "테이비2", "신사동", ""), "2021-07-03T07:13:23.000Z", "두 번째 댓글@tave2 입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(3, new UserInfo(1, null, "테이비", "XXXX동", ""), "2021-07-03T07:13:23.000Z", "@tave3 세 번째 댓글입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(4, new UserInfo(1, null, "테이비3", "XXX동", ""), "2021-07-03T07:13:23.000Z", "네 번째 댓글입니다! @tave4 "));

        postClickListener();
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
            isClickedHeart = !isClickedHeart;

            if (isClickedHeart) {
                ivHeart.setImageResource(R.drawable.heart_click);
                postInfoDetail.getPostInfoSimple().getHeartUsers().add(myInfo.getUserName());
                tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));

                //TODO: DB에 저장하고 수정 + MainActivity에서도 변경된 값을 갖고 있도록 해야함!
            }
            else {
                ivHeart.setImageResource(R.drawable.heart);
                postInfoDetail.getPostInfoSimple().getHeartUsers().remove(myInfo.getUserName());
                tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));

                //TODO: DB에 저장하고 수정
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
            writeComment = writeComment.concat(" ");   //마지막에 멘션있을 시를 대비해 하나의 공백은 남겨둠
            if (writeComment.length() != 0) {
                JsonObject commentInfo = new JsonObject();
                commentInfo.addProperty("postId", postID);
                commentInfo.addProperty("userId", myInfo.getUserID());
                commentInfo.addProperty("content", writeComment);
                RetrofitClient.getApiService().postComment(commentInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 댓글 post 성공", response.toString());
                        Log.i("PostA 댓글 post 성공2", response.body());
                        if (response.code() == 201) {
                            //TODO: 댓글 및 전체 글 새로고침!!!!!!!!!!!!!!!!!
                        }
                        else
                            Toast.makeText(PostActivity.this, "죄송합니다. 다시 한번 댓글을 전송해주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.i("PostA 댓글 연결실패", t.getMessage());
                        Toast.makeText(PostActivity.this, "서버에 연결이 되지 않았습니다.\n 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
                //TODO: 삭제해야 함!!!!!!!!!!!!!*********************************(빼갈 부분은 빼가자)******************************************
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                String date = sdf.format(calendar.getTime());

                postInfoDetail.getComments().add(new CommentInfo(1, myInfo, date, writeComment));
                commentAdapter.notifyDataSetChanged();

                tvCommentNums.setText(String.valueOf(postInfoDetail.getComments().size()));
                etWriteComment.setText("");

                svEntirePost.post(() -> {
                    svEntirePost.fullScroll(View.FOCUS_DOWN);       //화면 하단이 보이도록 함
                });
                //
            }

            etWriteComment.setEnabled(true);
        });
    }

    @SuppressLint("SetTextI18n")
    public void settingTags() {
        for (String tagName : postInfoDetail.getPostInfoSimple().getPostTag()){
            TextView tvTag = new TextView(PostActivity.this);
            tvTag.setText("#"+tagName+" ");
            tvTag.setTypeface(null, Typeface.NORMAL);
            tvTag.setTextColor(Color.parseColor("#1b73d8"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTag.setLayoutParams(layoutParams);
            llTag.addView(tvTag);

            /*  TODO: 태그 검색이 가능하게 할 것인가?!
            tvTag.setOnClickListener(v -> {
                String searchTag = tvTag.getText().toString().substring(1, tvTag.getText().length()-1);

                Intent showContainTagPost = new Intent(PostActivity.this, TagPostActivity.class);
                Bundle sctBundle = new Bundle();
                    sctBundle.putString("tagName", searchTag);
                    sctBundle.putParcelableArrayList("tagPostLists", searchTagPosts(searchTag));       //어떻게?
                showContainTagPost.putExtras(sctBundle);
                startActivity(showContainTagPost);
                finish();
            });
             */
        }
    }

    @Override
    public void finish() {
        //변경 내용이 있다면 보내줌(있으면 true 전달, 없으면 false 전달 -> Main에서 처리하게 함!, MyPage에서 처리!)
        Intent returnIntent = new Intent();
        if (isDeleted)       //글을 삭제했다고 Main에 전달해야 함!!(메인의 postList를 갱신해야 함)
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
            case android.R.id.home:{    //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.postEdit: {
                Intent editPost = new Intent(PostActivity.this, PostingActivity.class);
                Bundle epBundle = new Bundle();
                    epBundle.putBoolean("isEditingPost", true);
                    epBundle.putParcelable("postInfo", postInfoDetail);
                editPost.putExtras(epBundle);
                startActivityForResult(editPost, POST_EDIT_REQUEST);        //PostingActivity 화면으로 넘어감!

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
                            Log.i("PostA 삭제서버 연결실패", t.getMessage());
                            Toast.makeText(PostActivity.this, "해당 글 삭제에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }));
                builder.setNegativeButton("취소", (dialog, id) -> {
                    dialog.cancel();    //삭제가 되지 않음
                });
                AlertDialog alertDialog = builder.create();
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
            RetrofitClient.getApiService().getIDPost(postID).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("PostA 글 성공", response.toString());
                    Log.i("PostA 글 성공2", response.body());
                    if (response.code() == 200) {
                        //TODO: UI를 변경해야 함!!!
                    }
                    else {
                        Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("PostA 서버 연결실패", t.getMessage());
                    Toast.makeText(PostActivity.this, "해당 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
            //TODO: 위의 코드로 변경되어야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            postInfoDetail = Objects.requireNonNull(data).getExtras().getParcelable("postInfo");
            tvWriterTown.setText(postInfoDetail.getPostInfoSimple().getWriterTown());
            tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
            tvContent.setText(postInfoDetail.getPostContent());
            photoAdapter.changePhotoList(postInfoDetail.getPostImages());    //List의 주소가 변경됐으므로 이 주소로 설정해야 함!
            photoAdapter.notifyDataSetChanged();
            commentAdapter.changeCommentList(postInfoDetail.getComments());
            commentAdapter.notifyDataSetChanged();
            if (postInfoDetail.getPostInfoSimple().getPostTag().size() == 0)
                llTag.setVisibility(View.GONE);
            else {
                llTag.setVisibility(View.VISIBLE);
                llTag.removeAllViews();     //태그들 모두 초기화
                settingTags();
            }
            //삭제되어야 하는 부분 끝!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        }
    }
}
