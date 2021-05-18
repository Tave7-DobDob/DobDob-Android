package com.tave7.dobdob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PostActivity extends AppCompatActivity {
    boolean isEdit = false;     //현재 글 수정중인지
    EditText etTitle, etContent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.post_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      //뒤로가기 버튼

        //TODO: 글 작성자일 때만 해당되게 해야 함
        View customView = LayoutInflater.from(this).inflate(R.layout.other_actionbar, null);
        actionBar.setCustomView(customView);
            ImageView ivCancel = toolbar.findViewById(R.id.toolbar_cancel);
            ivCancel.setVisibility(View.INVISIBLE);
            TextView tvComplete = toolbar.findViewById(R.id.toolbar_complete);
            tvComplete.setVisibility(View.INVISIBLE);
        toolbarListener(toolbar);

        etTitle = findViewById(R.id.post_title);
        etContent = findViewById(R.id.post_content);
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivCancel = toolbar.findViewById(R.id.toolbar_cancel);
        TextView tvDelete = toolbar.findViewById(R.id.toolbar_delete);
        ImageView ivEdit = toolbar.findViewById(R.id.toolbar_edit);

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //수정 취소
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                ivCancel.setVisibility(View.INVISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
                ivEdit.setImageResource(R.drawable.edit);

                //TODO: 수정가능하지않도록 et(제목과 콘텐츠)를 setenabled(false)로 풀어줘야 함
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

                if (isEdit) {   //수정할 수 있음
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);    //뒤로가기 버튼 안보이게 함
                    ivCancel.setVisibility(View.VISIBLE);
                    tvDelete.setVisibility(View.INVISIBLE);
                    ivEdit.setImageResource(R.drawable.ok);

                    //TODO: 수정가능하도록 et(제목과 콘텐츠)를 setenabled(true)로 풀어줘야 함
                    etTitle.setEnabled(true);
                    etContent.setEnabled(true);
                }
                else{           //수정완료함(수정한 것 반영)-> TODO: DB에 바뀐 내용을 저장해야 함
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //뒤로가기 버튼 보이게 함
                    ivCancel.setVisibility(View.INVISIBLE);
                    tvDelete.setVisibility(View.VISIBLE);
                    ivEdit.setImageResource(R.drawable.edit);

                    //TODO: 수정가능하지않도록 et(제목과 콘텐츠)를 setenabled(false)로 풀어줘야 함
                    etTitle.setEnabled(false);
                    etContent.setEnabled(false);
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
