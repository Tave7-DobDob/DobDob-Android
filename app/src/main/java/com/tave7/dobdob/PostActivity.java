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
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator3;

public class PostActivity extends AppCompatActivity {
    UserInfo seeUserInfo;
    PostInfoSimple postInfo;        //TODO: PostInfoDetail로 변경해야 함!
    Menu menu;
    boolean isWriter = false;
    boolean isEdit = false, isClickedHeart = false;     //현재 글 수정중인지 || 현재 글에 대해 하트를 눌렀는 지
    boolean isDeleted = false;      //MainActivity에 전달해야 함(글을 삭제했는 지)

    NestedScrollView svEntirePost;
    EditText etTitle, etContent, etTag;
    TextView tvAddPhotos, tvHeartNums, tvCommentNums, tvTag;
    CircleIndicator3 indicator;
    PostPhotosPagerAdapter photoAdapter;
    ImageView ivHeart;
    RecyclerView rvComments;
    CommentRecyclerAdapter commentAdapter;
    LinearLayout llTag, llWriteComment;

    ArrayList<CommentInfo> commentList = null;
    
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        seeUserInfo = (UserInfo) getIntent().getSerializableExtra("seeUserInfo");     //지금 화면을 보고 있는 사용자의 정보
        postInfo = (PostInfoSimple) getIntent().getSerializableExtra("postInfo");     //TODO: DB에서 가져올 것인가(Extra로 안받아도됨)? 아니면 저장되어 있는 것을 보여줄 것인가?

        isWriter = seeUserInfo.getUserName().equals(postInfo.getWriterName());

        svEntirePost = findViewById(R.id.post_scrollView);
        CircleImageView civWriterProfile = findViewById(R.id.post_writerProfile);
            //TODO: postInfo.getWriterProfile()를 통해 사진 설정
        TextView tvWriterName = findViewById(R.id.post_writerName);
            tvWriterName.setText(postInfo.getWriterName());
        TextView tvWriterTown = findViewById(R.id.post_writerTown);
            tvWriterTown.setText(postInfo.getWriterTown());
        etTitle = findViewById(R.id.post_title);
            etTitle.setText(postInfo.getPostTitle());
        etContent = findViewById(R.id.post_content);
            //TODO: etContent에 대해서 저장해야 함
        tvAddPhotos = findViewById(R.id.post_addPhotos);        //사진 추가 버튼
            tvAddPhotos.setVisibility(View.GONE);
        indicator = findViewById(R.id.indicator);
        tvHeartNums = findViewById(R.id.post_heartNum);
            tvHeartNums.setText(String.valueOf(postInfo.getHeartUsers().size()));
        tvCommentNums = findViewById(R.id.post_commentNum);
            tvCommentNums.setText(String.valueOf(postInfo.getCommentNum()));        //TODO: 수정 요망!
        tvTag = findViewById(R.id.post_tag);
            tvTag.setVisibility(View.GONE);
        etTag = findViewById(R.id.post_etTag);
            etTag.setVisibility(View.GONE);
        llTag = findViewById(R.id.post_LinearTag);
            if (postInfo.getPostTag().size() != 0) {
                for (String tagName : postInfo.getPostTag()){
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
                            sctBundle.putSerializable("tagPostLists", searchTagPosts(searchTag));       //어떻게?
                            sctBundle.putSerializable("userInfo", seeUserInfo);
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

        //TODO: 임시 photoList생성      ->   postInfo.get()을 통해 사진을 받아와서 Bitmap으로 adapter에 연결함
        ArrayList<Bitmap> photoList = new ArrayList<>();
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
            photoList.add(icon);
            Bitmap icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.user_image);
            photoList.add(icon2);
        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        photoAdapter = new PostPhotosPagerAdapter(photoList);
        viewpager2.setAdapter(photoAdapter);
        indicator.setViewPager(viewpager2);
        photoAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        ivHeart = findViewById(R.id.post_ivHeart);
        for (String user: postInfo.getHeartUsers()) {
            if (user.equals(seeUserInfo.getUserName())) {
                isClickedHeart = true;
                break;
            }
        }
        if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우(TODO: 사용자 설정해야함)
            ivHeart.setImageResource(R.drawable.heart_full);
        else
            ivHeart.setImageResource(R.drawable.heart_empty);
        

        //TODO: 임시 commentList 생성(commentList는 Post와 연결되어 있어야 함)
            commentList = new ArrayList<>();
            commentList.add(new CommentInfo("", "테이비1", "XXX동", "2021.05.16 20:00", "@tave1 첫 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비2", "XX동", "2021.05.17 13:00", "두 번째 댓글@tave2 입니다!"));
            commentList.add(new CommentInfo("", "테이비3", "XXX동", "2021.05.18 15:00", "@tave3 세 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비4", "XX동", "2021.05.19 17:00", "네 번째 댓글입니다! @tave4 "));

        rvComments = findViewById(R.id.postComments);
        LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvComments.setLayoutManager(manager);
        commentAdapter = new CommentRecyclerAdapter(commentList, seeUserInfo);
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
            if (!isEdit) {
                //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함(해당 사용자의 UserInfo를 주어야 함) -> 만약 현재 닉네임을 클릭한 사람이 작성자라면 true로 Extra 전달
                Intent showProfilePage = new Intent(PostActivity.this, MyPageActivity.class);
                Bundle sppBundle = new Bundle();
                    sppBundle.putBoolean("isMyPage", false);
                showProfilePage.putExtras(sppBundle);
                //TODO: user의 닉네임을 DB에 전달해서 DB로부터 해당 userInfo와 user가 쓴 글을 받아와야 함
                //startActivity(showProfilePage);
            }
        });

        tvAddPhotos.setOnClickListener(v -> {
            //TODO: 사진 갤러리에서 불러와서 photoList에 추가함
        });

        //하트 클릭 시
        ivHeart.setOnClickListener(v -> {
            isClickedHeart = !isClickedHeart;

            if (isClickedHeart) {
                ivHeart.setImageResource(R.drawable.heart_full);
                postInfo.getHeartUsers().add(seeUserInfo.getUserName());
                tvHeartNums.setText(String.valueOf(postInfo.getHeartUsers().size()));

                //TODO: DB에 저장하고 수정 + MainActivity에서도 변경된 값을 갖고 있도록 해야함!
            }
            else {
                ivHeart.setImageResource(R.drawable.heart_empty);
                postInfo.getHeartUsers().remove(seeUserInfo.getUserName());
                tvHeartNums.setText(String.valueOf(postInfo.getHeartUsers().size()));

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

                commentList.add(new CommentInfo(seeUserInfo.getUserProfileUrl(), seeUserInfo.getUserName(), seeUserInfo.getUserTown(),
                        date, writeComment));
                commentAdapter.notifyDataSetChanged();

                tvCommentNums.setText(String.valueOf(commentList.size()));      //TODO: 추후에 postInfo와 CommentInfo를 연동해야 함!!
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

            menu.findItem(R.id.postEditComplete).setVisible(false);     //수정 시에만 보여야 함
            menu.findItem(R.id.postEditCancel).setVisible(false);
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
                SetEditing(true);

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
            case R.id.postEditComplete: {
                //TODO: DB에 바뀐 내용을 저장해야 함 -> 제목,내용,사진추가 등
                SetEditing(false);

                return true;
            }
            case R.id.postEditCancel: {
                //TODO: 기존 값으로 돌려놓아야 함!
                SetEditing(false);

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void SetEditing(boolean isEdit) {   //수정 상태일 때와 아닐 때에 대한 화면 설정
        this.isEdit = isEdit;   //수정중인 상태인 가를 저장

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!isEdit);    //뒤로가기 버튼 보이게 함
        menu.findItem(R.id.postEdit).setVisible(!isEdit);
        menu.findItem(R.id.postDelete).setVisible(!isEdit);
        menu.findItem(R.id.postEditComplete).setVisible(isEdit);
        menu.findItem(R.id.postEditCancel).setVisible(isEdit);

        etTitle.setEnabled(!isEdit);
        etContent.setEnabled(!isEdit);

        if (isEdit) {
            tvAddPhotos.setVisibility(View.VISIBLE);
            tvTag.setVisibility(View.VISIBLE);
            etTag.setVisibility(View.VISIBLE);
            rvComments.setVisibility(View.GONE);
            llWriteComment.setVisibility(View.GONE);
        }
        else {
            tvAddPhotos.setVisibility(View.GONE);
            tvTag.setVisibility(View.GONE);
            etTag.setVisibility(View.GONE);
            rvComments.setVisibility(View.VISIBLE);
            llWriteComment.setVisibility(View.VISIBLE);
        }

        photoAdapter.changeIsEditable();
        photoAdapter.notifyDataSetChanged();
    }
}
