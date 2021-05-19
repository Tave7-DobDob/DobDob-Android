package com.tave7.dobdob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PostingActivity extends AppCompatActivity {
    TextView tvTown, tvPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        EditText etTitle = findViewById(R.id.posting_title);                //글 제목
        LinearLayout llShowPhotos = findViewById(R.id.posting_showPhotos);  //업로드한 사진들
        EditText etContent = findViewById(R.id.posting_content);            //글 내용
        LinearLayout llTags = findViewById(R.id.posting_llTags);            //글의 태그들 추가할 위치
        EditText etTag = findViewById(R.id.posting_etTag);                  //글의 태그 입력칸(TODO: 드롭다운 가능해야 함)

        Toolbar toolbar = findViewById(R.id.posting_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

        View customView = LayoutInflater.from(this).inflate(R.layout.posting_actionbar, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        postingClickListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivCancel = toolbar.findViewById(R.id.toolbar_cancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();       //다시 메인화면으로 돌아감
            }
        });
        
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
        LinearLayout llTown = findViewById(R.id.posting_llTown);
        LinearLayout llPhotos = findViewById(R.id.posting_llPhotos);
        tvTown = findViewById(R.id.posting_town);                  //위치 지정하기 위해 클릭 가능 and 동이름 출력됨
        tvPhoto = findViewById(R.id.posting_photo);                //사진 업로드하기 위해 클릭 가능 and 사진개수 출력됨

        llTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 위치 지정 혹은 선택 위치 지정
            }
        });

        llPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 사진 불러오기

            }
        });
    }
}
