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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tave7.dobdob.adapter.CommentRecyclerAdapter;
import com.tave7.dobdob.adapter.PostPhotosPagerAdapter;
import com.tave7.dobdob.data.CommentInfo;
import com.tave7.dobdob.data.PostInfoDetail;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator3;

public class PostActivity extends AppCompatActivity {
    public static final int POST_EDIT_REQUEST = 6500;   //requestCode로 사용될 상수(글 수정)

    UserInfo seeUserInfo;
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
    TextView tvHeartNums, tvCommentNums;
    
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        seeUserInfo = (UserInfo) getIntent().getParcelableExtra("seeUserInfo");     //지금 화면을 보고 있는 사용자의 정보
        PostInfoSimple postInfo = (PostInfoSimple) getIntent().getParcelableExtra("postInfo");     //TODO: DB에서 가져올 것인가(Extra로 안받아도됨)? 아니면 저장되어 있는 것을 보여줄 것인가?
        postInfoDetail = new PostInfoDetail(postInfo, "내용입니다아아앙~~\n...\n...");     //깊은 복사

        //*********************************예시로 쓰는 사진들**************************************************
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap sendBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        postInfoDetail.getPostPhotos().add(stream.toByteArray());
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        Bitmap icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.user_image);
            icon2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
        postInfoDetail.getPostPhotos().add(stream2.toByteArray());

        isWriter = seeUserInfo.getUserName().equals(postInfoDetail.getPostInfoSimple().getWriterName());

        svEntirePost = findViewById(R.id.post_scrollView);
        CircleImageView civWriterProfile = findViewById(R.id.post_writerProfile);
            if (postInfoDetail.getPostInfoSimple().getWriterProfileUrl() != null) {
                //TODO: postInfo.getWriterProfile()를 통해 사진 설정  -> 확인해야 함!!!!!!!!!!!
                Bitmap writerProfile = BitmapFactory.decodeByteArray(postInfoDetail.getPostInfoSimple().getWriterProfileUrl(), 0, postInfoDetail.getPostInfoSimple().getWriterProfileUrl().length);
                civWriterProfile.setImageBitmap(writerProfile);
            }
        TextView tvWriterName = findViewById(R.id.post_writerName);
            tvWriterName.setText(postInfoDetail.getPostInfoSimple().getWriterName());
        TextView tvWriterTown = findViewById(R.id.post_writerTown);
            tvWriterTown.setText(postInfoDetail.getPostInfoSimple().getWriterTown());
        TextView tvTitle = findViewById(R.id.post_title);
            tvTitle.setText(postInfoDetail.getPostInfoSimple().getPostTitle());
        TextView tvContent = findViewById(R.id.post_content);
            tvContent.setText(postInfoDetail.getPostContent());
        indicator = findViewById(R.id.indicator);
        tvHeartNums = findViewById(R.id.post_heartNum);
            tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));
        tvCommentNums = findViewById(R.id.post_commentNum);
            tvCommentNums.setText(String.valueOf(postInfoDetail.getComments().size()));
        llTag = findViewById(R.id.post_LinearTag);
            if (postInfoDetail.getPostInfoSimple().getPostTag().size() != 0) {
                for (String tagName : postInfoDetail.getPostInfoSimple().getPostTag()){
                    TextView tvTag = new TextView(PostActivity.this);
                    tvTag.setText("#"+tagName+" ");
                    tvTag.setTypeface(null, Typeface.BOLD);
                    tvTag.setTextColor(Color.parseColor("#5AAEFF"));
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
                            sctBundle.putParcelable("userInfo", seeUserInfo);
                        showContainTagPost.putExtras(sctBundle);
                        startActivity(showContainTagPost);
                        finish();
                    });
                     */
                }
            }
            else
                llTag.setVisibility(View.GONE);
        llWriteComment = findViewById(R.id.post_writeCommentL);

        Toolbar toolbar = findViewById(R.id.post_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        photoAdapter = new PostPhotosPagerAdapter(postInfoDetail.getPostPhotos());
        viewpager2.setAdapter(photoAdapter);
        indicator.setViewPager(viewpager2);
        photoAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        ivHeart = findViewById(R.id.post_ivHeart);
        for (String user: postInfoDetail.getPostInfoSimple().getHeartUsers()) {
            if (user.equals(seeUserInfo.getUserName())) {
                isClickedHeart = true;
                break;
            }
        }
        if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우(TODO: 사용자 설정해야함)
            ivHeart.setImageResource(R.drawable.heart_full);
        else
            ivHeart.setImageResource(R.drawable.heart_empty);
        

        //TODO: 임시 comment들 생성
            postInfoDetail.getComments().add(new CommentInfo(new UserInfo(null, "테이비1", "한남동"), "2021.05.16 20:00", "@tave1 첫 번째 댓글입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(new UserInfo(null, "테이비2", "신사동"), "2021.05.17 13:00", "두 번째 댓글@tave2 입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(new UserInfo(null, "테이비", "XXXX동"), "2021.05.18 15:00", "@tave3 세 번째 댓글입니다!"));
            postInfoDetail.getComments().add(new CommentInfo(new UserInfo(null, "테이비3", "XXX동"), "2021.05.19 17:00", "네 번째 댓글입니다! @tave4 "));

        rvComments = findViewById(R.id.postComments);
        LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvComments.setLayoutManager(manager);
        commentAdapter = new CommentRecyclerAdapter(postInfoDetail.getComments(), seeUserInfo);
        rvComments.setAdapter(commentAdapter);      //어댑터 등록
        rvComments.addItemDecoration(new DividerItemDecoration(PostActivity.this, 1));

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
            //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함(해당 사용자의 UserInfo를 주어야 함) -> 만약 현재 닉네임을 클릭한 사람이 작성자라면 true로 Extra 전달
            Intent showProfilePage = new Intent(PostActivity.this, MyPageActivity.class);
            Bundle sppBundle = new Bundle();
                sppBundle.putBoolean("isMyPage", false);
            showProfilePage.putExtras(sppBundle);
            //TODO: user의 닉네임을 DB에 전달해서 DB로부터 해당 userInfo와 user가 쓴 글을 받아와야 함
            //startActivity(showProfilePage);
        });

        //하트 클릭 시
        ivHeart.setOnClickListener(v -> {
            isClickedHeart = !isClickedHeart;

            if (isClickedHeart) {
                ivHeart.setImageResource(R.drawable.heart_full);
                postInfoDetail.getPostInfoSimple().getHeartUsers().add(seeUserInfo.getUserName());
                tvHeartNums.setText(String.valueOf(postInfoDetail.getPostInfoSimple().getHeartUsers().size()));

                //TODO: DB에 저장하고 수정 + MainActivity에서도 변경된 값을 갖고 있도록 해야함!
            }
            else {
                ivHeart.setImageResource(R.drawable.heart_empty);
                postInfoDetail.getPostInfoSimple().getHeartUsers().remove(seeUserInfo.getUserName());
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

            String writeComment = etWriteComment.getText().toString();
            writeComment = writeComment.trim();
            if (writeComment.length() != 0) {
                //TODO: DB에 댓글 저장(현재 시간 저장) -> DB에 시간을 형식 맞게 저장하자!
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                String date = sdf.format(calendar.getTime());

                postInfoDetail.getComments().add(new CommentInfo(seeUserInfo, date, writeComment));
                commentAdapter.notifyDataSetChanged();

                tvCommentNums.setText(String.valueOf(postInfoDetail.getComments().size()));
                etWriteComment.setText("");

                svEntirePost.post(() -> {
                    svEntirePost.fullScroll(View.FOCUS_DOWN);       //화면 하단이 보이도록 함
                });
            }

            etWriteComment.setEnabled(true);
        });
    }

    @Override
    public void finish() {
        //변경 내용이 있다면 보내줌(있으면 true 전달, 없으면 false 전달 -> Main에서 처리하게 함!)
        //if (isDeleted)        //글을 삭제했다고 Main에 전달해야 함!!(메인의 postList를 갱신해야 함)

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
                builder.setPositiveButton("삭제", (dialog, which) -> {
                    //TODO: DB에서 포스트를 삭제하고, 안드로이드 스튜디오 내의 postList에서 post를 삭제해야 함(전달! Bundle로)
                    isDeleted = true;
                    finish();
                });
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
    //TODO: 수정을 했다면 Posting으로부터 수정 내용을 받아옴!!!!!
    //TODO: DB에 바뀐 내용을 저장해야 함 -> 제목,내용,사진추가 등
}
