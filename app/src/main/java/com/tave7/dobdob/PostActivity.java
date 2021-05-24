package com.tave7.dobdob;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class PostActivity extends AppCompatActivity {
    boolean isEdit = false;     //현재 글 수정중인지
    EditText etTitle, etContent, etWriteComment;
    TextView tvAddPhotos;
    CircleIndicator3 indicator;
    RecyclerView rvComments;
    PostPhotosPagerAdapter photoAdapter;
    LinearLayout llWriteComment;

    ArrayList<CommentInfo> commentList = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        etTitle = findViewById(R.id.post_title);
        etContent = findViewById(R.id.post_content);
        indicator = findViewById(R.id.indicator);
        llWriteComment = findViewById(R.id.post_writeCommentL);
        etWriteComment = findViewById(R.id.post_etComment);

        Toolbar toolbar = findViewById(R.id.post_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        //TODO: 글 작성자일 때만 해당되게 해야 함(cf. 글 작성자가 아니라면 뒤로가기 버튼만 보이게 됨)
        View customView = LayoutInflater.from(this).inflate(R.layout.other_actionbar, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        tvAddPhotos = findViewById(R.id.post_addPhotos);        //사진 추가 버튼
        tvAddPhotos.setVisibility(View.GONE);

        //TODO: 임시 photoList생성
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


        //TODO: 임시 commentList 생성(commentList는 Post와 연결되어 있어야 함)
            commentList = new ArrayList<>();
            commentList.add(new CommentInfo("", "테이비1", "XXX동", "2021.05.16 20:00", "@tave1 첫 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비2", "XX동", "2021.05.17 13:00", "두 번째 댓글@tave2 입니다!"));
            commentList.add(new CommentInfo("", "테이비3", "XXX동", "2021.05.18 15:00", "@tave3 세 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비4", "XX동", "2021.05.19 17:00", "네 번째 댓글입니다! @tave4 "));

        rvComments = findViewById(R.id.postComments);
        LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvComments.setLayoutManager(manager);
        CommentRecyclerAdapter cAdapter = new CommentRecyclerAdapter(commentList);
        rvComments.setAdapter(cAdapter);      //어댑터 등록
        rvComments.addItemDecoration(new DividerItemDecoration(PostActivity.this, 1));

        postClickListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivEditCancel = toolbar.findViewById(R.id.toolbar_editCancel);
            ivEditCancel.setVisibility(View.GONE);     //수정버튼이 눌릴 때만 수정취소버튼이 보이게 되어야 함
        TextView tvPostDelete = toolbar.findViewById(R.id.toolbar_delete);
        ImageView ivEdit = toolbar.findViewById(R.id.toolbar_edit);

        ivEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //수정 취소
                isEdit = false;     //지금부터 수정 안됨
                
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                ivEditCancel.setVisibility(View.GONE);
                tvPostDelete.setVisibility(View.VISIBLE);
                ivEdit.setImageResource(R.drawable.edit);

                etTitle.setEnabled(false);
                etContent.setEnabled(false);
                tvAddPhotos.setVisibility(View.GONE);
                rvComments.setVisibility(View.VISIBLE);
                llWriteComment.setVisibility(View.VISIBLE);

                photoAdapter.changeIsEditable();
                photoAdapter.notifyDataSetChanged();
            }
        });

        tvPostDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("글 삭제").setMessage("현재 글을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: DB에서 포스트를 삭제하고, 안드로이드 스튜디오 내의 postList에서 post를 삭제해야 함
                        finish();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();    //삭제가 되지 않음
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;   //지금부터 수정가능여부

                if (isEdit) {   //작성한 글(post)을 수정할 수 있음
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);    //뒤로가기 버튼 안보이게 함
                    ivEditCancel.setVisibility(View.VISIBLE);
                    tvPostDelete.setVisibility(View.GONE);
                    ivEdit.setImageResource(R.drawable.ok);

                    etTitle.setEnabled(true);
                    etContent.setEnabled(true);
                    tvAddPhotos.setVisibility(View.VISIBLE);
                    rvComments.setVisibility(View.GONE);        //댓글 영역 안보임
                    llWriteComment.setVisibility(View.GONE);    //댓글 작성 영역 안보임

                    photoAdapter.changeIsEditable();
                    photoAdapter.notifyDataSetChanged();
                }
                else {          //수정완료함(수정한 것 반영)-> TODO: DB에 바뀐 내용을 저장해야 함 -> 제목,내용,사진추가 등
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                    ivEditCancel.setVisibility(View.GONE);
                    tvPostDelete.setVisibility(View.VISIBLE);
                    ivEdit.setImageResource(R.drawable.edit);

                    etTitle.setEnabled(false);
                    etContent.setEnabled(false);
                    tvAddPhotos.setVisibility(View.GONE);
                    rvComments.setVisibility(View.VISIBLE);
                    llWriteComment.setVisibility(View.VISIBLE);

                    photoAdapter.changeIsEditable();
                    photoAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void postClickListener(){
        TextView tvWriterNick = findViewById(R.id.post_writerNick);
        tvWriterNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit) {
                    //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함(해당 사용자의 UserInfo를 주어야 함) -> 만약 현재 닉네임을 클릭한 사람이 작성자라면 true로 Extra 전달
                    Intent showProfilePage = new Intent(PostActivity.this, MyPageActivity.class);
                    showProfilePage.putExtra("isMyPage", false);
                    startActivity(showProfilePage);
                }
            }
        });

        tvAddPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 사진 갤러리에서 불러와서 photoList에 추가함
            }
        });

        TextView tvAddComment = findViewById(R.id.post_postComment);
        tvAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etCommentContent = findViewById(R.id.post_etComment);
                //TODO: commentList에 댓글을 추가함(Post에도 연결되어 추가 됨) + DB에 댓글 저장
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{    //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
