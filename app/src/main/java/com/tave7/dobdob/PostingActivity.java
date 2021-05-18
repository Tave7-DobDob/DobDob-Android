package com.tave7.dobdob;

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

public class PostingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        Toolbar toolbar = findViewById(R.id.posting_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼
            //actionBar.setHomeAsUpIndicator(R.drawable.);  //뒤로가기 버튼 아이콘 변경

        View customView = LayoutInflater.from(this).inflate(R.layout.other_actionbar, null);
        actionBar.setCustomView(customView);
            ImageView ivCancel = toolbar.findViewById(R.id.toolbar_cancel);
            ivCancel.setVisibility(View.INVISIBLE);
            TextView tvDelete = toolbar.findViewById(R.id.toolbar_delete);
            tvDelete.setVisibility(View.INVISIBLE);
            ImageView ivEdit = toolbar.findViewById(R.id.toolbar_edit);
            ivEdit.setVisibility(View.INVISIBLE);
        toolbarListener(toolbar);
        
        EditText etTitle = findViewById(R.id.posting_title);                //글 제목
        LinearLayout llShowPhotos = findViewById(R.id.posting_showPhotos);  //업로드한 사진들
        EditText etContent = findViewById(R.id.posting_content);            //글 내용
        LinearLayout llTags = findViewById(R.id.posting_llTags);            //글의 태그들 추가할 위치
        EditText etTag = findViewById(R.id.posting_etTag);                  //글의 태그 입력칸(TODO: 드롭다운 가능해야 함)
        TextView tvTown = findViewById(R.id.posting_town);                  //위치 지정하기 위해 클릭 가능 and 동이름 출력됨
        TextView tvPhoto = findViewById(R.id.posting_photo);                //사진 업로드하기 위해 클릭 가능 and 사진개수 출력됨

        postingClickListener();
    }

    public void toolbarListener(Toolbar toolbar){
        TextView ivComplete = toolbar.findViewById(R.id.toolbar_complete);
        ivComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 글쓰기 완료버튼과 같은 기능을 해야 함(제목과 content가 있는지 확인, 위치를 저장했는지 확인) -> DB에 전달
                //사진, 태그은 없어도 됨
            }
        });
    }

    public void postingClickListener(){
        //TODO: 사진모음(llShowPhotos)에서 각각의 사진에 대해 클릭 리스너 제공, 동 내용(tvTown) 클릭, 사진 업로드(tvPhoto) 위해 클릭
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
