package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.InputStream;
import java.util.ArrayList;

public class PostingActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;

    private ArrayList<String> tmpTag = null;
    private ArrayList<Bitmap> tmpPhotos = null;

    private LayoutInflater lInflater;
    private LinearLayout llShowPhotos;
    private EditText etTag;
    private com.nex3z.flowlayout.FlowLayout flTags;
    private TextView tvPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        tmpTag = new ArrayList<>();
        tmpPhotos = new ArrayList<>();
        lInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        EditText etTitle = (EditText) findViewById(R.id.posting_title);                 //글 제목
        llShowPhotos = (LinearLayout) findViewById(R.id.posting_showPhotos);            //업로드한 사진들
        EditText etContent = (EditText) findViewById(R.id.posting_content);             //글 내용
        etTag = (EditText) findViewById(R.id.posting_etTag);                            //글의 태그 입력칸(TODO: 드롭다운 가능해야 함)
        flTags = (com.nex3z.flowlayout.FlowLayout) findViewById(R.id.posting_flTags);                        //글의 태그들 추가할 위치
        tvPhotos = (TextView) findViewById(R.id.posting_photo);                         //글에 첨부할 사진 개수

        Toolbar toolbar = (Toolbar) findViewById(R.id.posting_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

        View customView = LayoutInflater.from(this).inflate(R.layout.posting_actionbar, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        postingClickListener();
        postingTextChangedListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivCancel = (ImageView) toolbar.findViewById(R.id.toolbar_cancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();       //다시 메인화면으로 돌아감
            }
        });
        
        TextView ivComplete = (TextView) toolbar.findViewById(R.id.toolbar_complete);
        ivComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 글쓰기 완료버튼과 같은 기능을 해야 함(제목과 content가 있는지 확인, 위치를 저장했는지 확인) -> DB에 전달
                //사진, 태그은 없어도 됨
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void postingClickListener(){
        ConstraintLayout clWhole = (ConstraintLayout) findViewById(R.id.posting_wholeCL);
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        //TODO: 사진모음(llShowPhotos)에서 각각의 사진에 대해 클릭 리스너 제공, 동 내용(tvTown) 클릭, 사진 업로드(tvPhoto) 위해 클릭
        LinearLayout llTown = (LinearLayout) findViewById(R.id.posting_llTown);
        TextView tvTown = (TextView) findViewById(R.id.posting_town);                  //위치 지정하기 위해 클릭 가능 and 동이름 출력됨
        LinearLayout llPhotos = (LinearLayout) findViewById(R.id.posting_llPhotos);

        llTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 위치 지정 혹은 선택 위치 지정
            }
        });

        llPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //사진 불러오기
                if (tmpPhotos.size() < 7) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    //intent.setType(MediaStore.Images.Media.CONTENT_TYPE);       //기기 기본 갤러리 접근
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_FROM_GALLERY);
                }
                else
                    Toast.makeText(PostingActivity.this, "사진은 최대 7장 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void postingTextChangedListener(){
        etTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //스페이스바를 누를 시에 태그가 추가됨
                if (s.length()>0 && s.charAt(s.length()-1)==' ') {
                    tmpTag.add(etTag.getText().toString().substring(0, s.length()-1));

                    View view = lInflater.inflate(R.layout.item_tag, null);
                    TextView tvTag = (TextView) view.findViewById(R.id.tag_tagName);
                    tvTag.setText(s.toString());
                    ImageView ivCancel = (ImageView) view.findViewById(R.id.tag_cancel);
                        ivCancel.setOnClickListener(v -> flTags.removeView((View) v.getParent()));
                    flTags.addView(view);

                    etTag.setText("");
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap tmpPhoto = BitmapFactory.decodeStream(is);
                tmpPhotos.add(tmpPhoto);
                is.close();

                tvPhotos.setText("사진("+tmpPhotos.size()+"/7)");

                View view = lInflater.inflate(R.layout.item_photo, null);
                ImageView ivPhoto = (ImageView) view.findViewById(R.id.photo_iv);
                    ivPhoto.setImageBitmap(tmpPhoto);
                ImageView ivCancel = (ImageView) view.findViewById(R.id.photo_cancel);
                ivCancel.setOnClickListener(v -> {
                    llShowPhotos.removeView((View) v.getParent());
                    tmpPhotos.remove(tmpPhoto);
                    tvPhotos.setText("사진("+tmpPhotos.size()+"/7)");
                });
                llShowPhotos.addView(view);

            } catch (Exception e){ e.printStackTrace(); }
        } else if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_CANCELED){
            Toast.makeText(this,"사진 선택 취소", Toast.LENGTH_SHORT).show();
        }
    }
}
