package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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

import com.tave7.dobdob.data.PostInfoDetail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class PostingActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;

    private ArrayList<String> tmpTag = null;    //글 작성 페이지일 때
    private ArrayList<Bitmap> tmpPhotos = null; //수정하는 페이지인 경우에도 사용
    private boolean isEditingPost = false, isCompleted = false, isChangedPhoto = false;
    private PostInfoDetail editPostInfo = null;

    private com.nex3z.flowlayout.FlowLayout flTags;
    private EditText etTitle, etContent, etTag;
    private LayoutInflater lInflater;
    private LinearLayout llShowPhotos, llTown, llPhotos;
    private TextView tvPhotos, tvTown;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        tmpTag = new ArrayList<>();
        tmpPhotos = new ArrayList<>();
        lInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        etTitle = (EditText) findViewById(R.id.posting_title);                 //글 제목
        llShowPhotos = (LinearLayout) findViewById(R.id.posting_showPhotos);   //업로드한 사진들
            llShowPhotos.setVisibility(View.GONE);
        etContent = (EditText) findViewById(R.id.posting_content);             //글 내용
        etTag = (EditText) findViewById(R.id.posting_etTag);                   //글의 태그 입력칸(TODO: 드롭다운 가능해야 함)
        flTags = (com.nex3z.flowlayout.FlowLayout) findViewById(R.id.posting_flTags);   //글의 태그들 추가할 위치
        tvPhotos = (TextView) findViewById(R.id.posting_photo);                //글에 첨부할 사진 개수

        llTown = (LinearLayout) findViewById(R.id.posting_llTown);
        tvTown = (TextView) findViewById(R.id.posting_town);                  //위치 지정하기 위해 클릭 가능 and 동이름 출력됨
        llPhotos = (LinearLayout) findViewById(R.id.posting_llPhotos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.posting_toolbar);        //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_posting, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        if (getIntent().hasExtra("isEditingPost")) {        //현재 글 수정 페이지임
            isEditingPost = true;
            editPostInfo = getIntent().getExtras().getParcelable("postInfo");

            etTitle.setText(editPostInfo.getPostInfoSimple().getPostTitle());
            if (editPostInfo.getPostPhotos().size() > 0)
                llShowPhotos.setVisibility(View.VISIBLE);
            for (byte[] photo : editPostInfo.getPostPhotos()) {
                //TODO: DB 사진 서버로 갔다 와야함!!!!(서버 경로로 저장해야 함!!!!)
                Bitmap bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);     //byte[]를 bitmap으로 변환
                tmpPhotos.add(bmp);
                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_photo, null);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                params.rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                view.setLayoutParams(params);
                ImageView ivPhoto = (ImageView) view.findViewById(R.id.photo_iv);
                ivPhoto.setImageBitmap(bmp);
                ImageView ivCancel = (ImageView) view.findViewById(R.id.photo_cancel);
                ivCancel.setOnClickListener(v -> {
                    isChangedPhoto = true;

                    llShowPhotos.removeView((View) v.getParent());
                    tmpPhotos.remove(bmp);
                    tvPhotos.setText("사진("+tmpPhotos.size()+"/5)");

                    if (tmpPhotos.size() == 0)
                        llShowPhotos.setVisibility(View.GONE);
                });
                llShowPhotos.addView(view);
            }
            etContent.setText(editPostInfo.getPostContent());
            for (String tag : editPostInfo.getPostInfoSimple().getPostTag()) {      //태그가 있다면 태그 표시
                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_tag, null);
                TextView tvTag = (TextView) view.findViewById(R.id.tag_tagName);
                tvTag.setText(tag);
                ImageView ivCancel = (ImageView) view.findViewById(R.id.tag_cancel);
                ivCancel.setOnClickListener(v -> {
                    flTags.removeView((View) v.getParent());
                    editPostInfo.getPostInfoSimple().getPostTag().remove(tag);
                });
                flTags.addView(view);
            }
            tvTown.setText(editPostInfo.getPostInfoSimple().getWriterTown());
            tvPhotos.setText("사진("+tmpPhotos.size()+"/5)");
        }

        postingClickListener();
        postingTextChangedListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivCancel = (ImageView) toolbar.findViewById(R.id.toolbar_cancel);
        ivCancel.setOnClickListener(v -> finish());
        
        TextView ivComplete = (TextView) toolbar.findViewById(R.id.toolbar_complete);
        ivComplete.setOnClickListener(v -> {
            //TODO: 글쓰기 완료버튼과 같은 기능을 해야 함(제목과 content가 있는지 확인, 위치를 저장했는지 확인) -> DB에 전달
            //사진, 태그는 없어도 됨
            if (isEditingPost) {    //글 수정 완료
                isCompleted = true;

                editPostInfo.getPostInfoSimple().setPostTitle(etTitle.getText().toString());    //제목 변경
                if (isChangedPhoto) {                                                           //사진 변경 -> 서버로 저장된 Uri를 저장해야 함!
                    editPostInfo.getPostPhotos().clear();

                    for (Bitmap bitmap : tmpPhotos) {
                        editPostInfo.getPostPhotos().add(bitmapToByte(bitmap));
                    }
                }
                editPostInfo.setPostContent(etContent.getText().toString());                    //내용 변경
                editPostInfo.getPostInfoSimple().setWriterTown(tvTown.getText().toString());    //동네 변경
                //TODO: DB에 바뀐 내용을 저장해야 함 -> 제목,내용,사진추가 등
                //TODO: DB에 전달함!!!

                finish();
            }
            else {
                isCompleted = true;

                //TODO: 글쓰기 완료버튼과 같음 -> DB에 값을 전달함 -> 저장되어야 함!(MainActivity에 DB로부터 최신 정보를 받아옴)
                finish();
            }
        });
    }

    @Override
    public void finish() {
        if (isCompleted) {
            Intent intentComplete = new Intent();
            if (isEditingPost) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("postInfo", editPostInfo);
                intentComplete.putExtras(bundle);
            }
            setResult(RESULT_OK, intentComplete);
        }
        else {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
        }

        super.finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void postingClickListener(){
        ConstraintLayout clWhole = (ConstraintLayout) findViewById(R.id.posting_wholeCL);
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        //TODO: 동 내용(tvTown) 클릭
        llTown.setOnClickListener(v -> {
            //TODO: 위치 지정 혹은 선택 위치 지정
        });

        llPhotos.setOnClickListener(v -> {
            //사진 불러오기
            if (tmpPhotos.size() < 5) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_FROM_GALLERY);
            }
            else
                Toast.makeText(PostingActivity.this, "사진은 최대 5장 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    public void postingTextChangedListener(){
        etTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //스페이스바를 누를 시에 태그가 추가됨
                if (s.length()>0 && s.charAt(s.length()-1)==' ') {
                    String tag = s.toString().trim();   //앞뒤 공백 제거
                    if (tag.length() > 0) {
                        boolean canAddTag = false;
                        if (isEditingPost) {
                            if (!editPostInfo.getPostInfoSimple().getPostTag().contains(tag)) {
                                canAddTag = true;
                                editPostInfo.getPostInfoSimple().getPostTag().add(tag);
                            }
                        }
                        else {
                            if (!tmpTag.contains(tag)) {
                                canAddTag = true;
                                tmpTag.add(tag);
                            }
                        }

                        if (canAddTag) {
                            @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_tag, null);
                            TextView tvTag = (TextView) view.findViewById(R.id.tag_tagName);
                            tvTag.setText(tag);
                            ImageView ivCancel = (ImageView) view.findViewById(R.id.tag_cancel);
                            ivCancel.setOnClickListener(v -> {
                                flTags.removeView((View) v.getParent());

                                if (isEditingPost)
                                    editPostInfo.getPostInfoSimple().getPostTag().remove(tag);
                                else
                                    tmpTag.remove(tag);
                            });
                            flTags.addView(view);
                        }

                        etTag.setText("");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data).getData());
                Bitmap photo = BitmapFactory.decodeStream(is);
                is.close();

                isChangedPhoto = true;

                tmpPhotos.add(photo);
                tvPhotos.setText("사진("+tmpPhotos.size()+"/5)");

                if (tmpPhotos.size() == 1)
                    llShowPhotos.setVisibility(View.VISIBLE);

                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_photo, null);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                params.rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                view.setLayoutParams(params);
                ImageView ivPhoto = (ImageView) view.findViewById(R.id.photo_iv);
                    ivPhoto.setImageBitmap(photo);
                ImageView ivCancel = (ImageView) view.findViewById(R.id.photo_cancel);
                ivCancel.setOnClickListener(v -> {
                    llShowPhotos.removeView((View) v.getParent());
                    tmpPhotos.remove(photo);
                    tvPhotos.setText("사진("+tmpPhotos.size()+"/5)");

                    if (tmpPhotos.size() == 0)
                        llShowPhotos.setVisibility(View.GONE);
                });
                llShowPhotos.addView(view);

            } catch (Exception e){ e.printStackTrace(); }
        } else if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_CANCELED){
            Toast.makeText(this,"사진 선택 취소", Toast.LENGTH_SHORT).show();
        }
    }
}
