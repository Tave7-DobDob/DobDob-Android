package com.tave7.dobdob;

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

import me.relex.circleindicator.CircleIndicator;
import me.relex.circleindicator.CircleIndicator3;

public class PostActivity extends AppCompatActivity {
    boolean isEdit = false;     //현재 글 수정중인지
    EditText etTitle, etContent, etWriteComment;
    CircleIndicator3 indicator;
    RecyclerView rvComments;
    LinearLayout llWriteComment;
    
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


        //TODO: 임시 photoList생성
        ArrayList<Bitmap> photoList = new ArrayList<>();
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
            photoList.add(icon);
            Bitmap icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
            photoList.add(icon2);
        ViewPager2 viewpager2 = findViewById(R.id.vpPostPhotos);
        PostPhotosPagerAdapter photoAdapter = new PostPhotosPagerAdapter(photoList);
        viewpager2.setAdapter(photoAdapter);
        indicator.setViewPager(viewpager2);
        photoAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());


        //TODO: 임시 commentList 생성
            ArrayList<CommentInfo> commentList = new ArrayList<>();
            commentList.add(new CommentInfo("", "테이비1", "XXX동", "2021.05.16 20:00", "@tave7", "첫 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비2", "XX동", "2021.05.17 13:00", "@tave7", "두 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비3", "XXX동", "2021.05.18 15:00", "@tave7", "세 번째 댓글입니다!"));
            commentList.add(new CommentInfo("", "테이비4", "XX동", "2021.05.19 17:00", "@tave7", "네 번째 댓글입니다!"));

        rvComments = findViewById(R.id.postComments);
        LinearLayoutManager manager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.VERTICAL,false);
        rvComments.setLayoutManager(manager);
        CommentRecyclerAdapter cAdapter = new CommentRecyclerAdapter(commentList);
        rvComments.setAdapter(cAdapter);      //어댑터 등록
        rvComments.addItemDecoration(new DividerItemDecoration(PostActivity.this, 1));
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivEditCancel = toolbar.findViewById(R.id.toolbar_editCancel);
            ivEditCancel.setVisibility(View.GONE);     //수정버튼이 눌릴 때만 수정취소버튼이 보이게 되어야 함
        TextView tvDelete = toolbar.findViewById(R.id.toolbar_delete);
        ImageView ivEdit = toolbar.findViewById(R.id.toolbar_edit);

        ivEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //수정 취소
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                ivEditCancel.setVisibility(View.GONE);
                tvDelete.setVisibility(View.VISIBLE);
                ivEdit.setImageResource(R.drawable.edit);

                //TODO: 수정가능하지않도록 사진을 원래대로 복귀
                etTitle.setEnabled(false);
                etContent.setEnabled(false);
            }
        });

        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 다이얼로그로 실제로 삭제할 것인지 물어봄(DB에서 삭제함)
            }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;   //지금부터 수정가능여부

                if (isEdit) {   //작성한 글(post)을 수정할 수 있음
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);    //뒤로가기 버튼 안보이게 함
                    ivEditCancel.setVisibility(View.VISIBLE);
                    tvDelete.setVisibility(View.GONE);
                    ivEdit.setImageResource(R.drawable.ok);

                    //TODO: 수정가능하도록 -> 사진도 삭제가능하게!
                    etTitle.setEnabled(true);
                    etContent.setEnabled(true);
                    rvComments.setVisibility(View.GONE);        //댓글 영역 안보임
                    llWriteComment.setVisibility(View.GONE);    //댓글 작성 영역 안보임

                }
                else {          //수정완료함(수정한 것 반영)-> TODO: DB에 바뀐 내용을 저장해야 함
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                    ivEditCancel.setVisibility(View.GONE);
                    tvDelete.setVisibility(View.VISIBLE);
                    ivEdit.setImageResource(R.drawable.edit);

                    //TODO: 수정가능하지 않도록
                    etTitle.setEnabled(false);
                    etContent.setEnabled(false);
                    rvComments.setVisibility(View.VISIBLE);
                    llWriteComment.setVisibility(View.VISIBLE);
                }
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
