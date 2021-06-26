package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tave7.dobdob.data.PhotoInfo;
import com.tave7.dobdob.data.PostInfoDetail;
import com.tave7.dobdob.data.UserInfo;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.InitialSettingActivity.DAUMADDRESS_REQUEST;

public class PostingActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;
    private JsonObject location = null;
    private String tmpFullAddress = "";

    private UserInfo userInfo = null;
    private PostInfoDetail editPostInfo = null;
    private ArrayList<String> tmpTag = null;        //글 작성 페이지일 때
    private ArrayList<PhotoInfo> tmpPhotos = null;  //(boolean, File, Bitmap)
    private boolean isEditingPost = false, isCompleted = false;

    private com.nex3z.flowlayout.FlowLayout flTags;
    private EditText etTitle, etContent, etTag;
    private LayoutInflater lInflater;
    private LinearLayout llShowPhotos, llTown, llPhotos;
    private TextView tvPhotos;
    private TextView tvTown;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        tmpTag = new ArrayList<>();
        tmpPhotos = new ArrayList<>();
        lInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        etTitle = findViewById(R.id.posting_title);                 //글 제목
        llShowPhotos = findViewById(R.id.posting_showPhotos);   //업로드한 사진들
            llShowPhotos.setVisibility(View.GONE);
        etContent = findViewById(R.id.posting_content);             //글 내용
        etTag = findViewById(R.id.posting_etTag);                   //글의 태그 입력칸
        flTags = findViewById(R.id.posting_flTags);   //글의 태그들 추가할 위치
        tvPhotos = findViewById(R.id.posting_photo);                //글에 첨부할 사진 개수

        llTown = findViewById(R.id.posting_llTown);
        tvTown = findViewById(R.id.posting_town);                  //위치 지정하기 위해 클릭 가능 and 동이름 출력됨
        llPhotos = findViewById(R.id.posting_llPhotos);

        Toolbar toolbar = findViewById(R.id.posting_toolbar);        //툴바 설정
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
            for (String photo : editPostInfo.getPostPhotos()) {
                Bitmap bmp = null;
                try {
                    bmp = new DownloadFileTask(photo).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }

                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_photo, null);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                params.rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                view.setLayoutParams(params);
                ImageView ivPhoto = view.findViewById(R.id.photo_iv);
                ivPhoto.setImageBitmap(bmp);
                ImageView ivCancel = view.findViewById(R.id.photo_cancel);
                    ivCancel.setVisibility(View.GONE);
                llShowPhotos.addView(view);
            }
            etContent.setText(editPostInfo.getPostContent());
            for (String tag : editPostInfo.getPostInfoSimple().getPostTag()) {      //태그가 있다면 태그 표시
                tmpTag.add(tag);
                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_tag, null);
                TextView tvTag = view.findViewById(R.id.tag_tagName);
                tvTag.setText(tag);
                ImageView ivCancel = view.findViewById(R.id.tag_cancel);
                ivCancel.setOnClickListener(v -> {
                    flTags.removeView((View) v.getParent());
                    tmpTag.remove(tag);
                });
                flTags.addView(view);
            }
            tvTown.setText(editPostInfo.getPostInfoSimple().getWriterTown());
            tvPhotos.setText("사진("+editPostInfo.getPostPhotos().size()+"/5)");
        }
        else
            userInfo = getIntent().getExtras().getParcelable("userInfo");

        postingClickListener();
        postingTextChangedListener();
    }

    public void toolbarListener(Toolbar toolbar){
        ImageView ivCancel = toolbar.findViewById(R.id.toolbar_cancel);
        ivCancel.setOnClickListener(v -> finish());
        
        TextView ivComplete = toolbar.findViewById(R.id.toolbar_complete);
        ivComplete.setOnClickListener(v -> {
            if (etTitle.getText().toString().trim().length() == 0) {
                Toast.makeText(getApplicationContext(), "글 제목을 입력해 주세요:)", Toast.LENGTH_SHORT).show();
            }
            else if (etContent.getText().toString().trim().length() == 0) {
                Toast.makeText(getApplicationContext(), "글의 내용을 입력해 주세요:)", Toast.LENGTH_SHORT).show();
            }
            else if (tvTown.getText().toString().equals("동네 설정")) {
                Toast.makeText(getApplicationContext(), "동네를 설정해 주세요:)", Toast.LENGTH_SHORT).show();
            }
            else {
                if (isEditingPost) {    //글 수정 완료
                    boolean isChgTitle = true, isChgContent = true, isChgTown = true, isChgTag = true;
                    if (editPostInfo.getPostInfoSimple().getPostTitle().equals(etTitle.getText().toString().trim()))
                        isChgTitle = false;
                    if (editPostInfo.getPostContent().equals(etContent.getText().toString().trim()))
                        isChgContent = false;
                    if (tmpFullAddress.length()==0 || tmpFullAddress.equals(editPostInfo.getPostInfoSimple().getWriterAddress()))
                        isChgTown = false;
                    if (editPostInfo.getPostInfoSimple().getPostTag().containsAll(tmpTag) && tmpTag.containsAll(editPostInfo.getPostInfoSimple().getPostTag()))
                        isChgTag = false;

                    //글 제목 혹은 위치 혹은 내용 혹은 태그 변경 시
                    if (isChgTitle || isChgContent || isChgTown || isChgTag) {
                        isCompleted = true;

                        JsonObject postData = new JsonObject();
                        postData.addProperty("userId", editPostInfo.getPostInfoSimple().getWriterID());
                        postData.add("location", location);
                        postData.addProperty("title", etTitle.getText().toString().trim());
                        postData.addProperty("content", etContent.getText().toString().trim());
                        postData.addProperty("editedAt", String.valueOf(new Date(System.currentTimeMillis())));     //TODO: 확인해야 함!!!
                        postData.addProperty("tags", new Gson().toJson(tmpTag));
                        RetrofitClient.getApiService().patchIDPost(editPostInfo.getPostInfoSimple().getPostID(), postData).enqueue(new Callback<String>() {       //DB전달
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                Log.i("Posting 수정성공1", response.toString());
                                Log.i("Posting 수정성공1", response.body());
                                if (response.code() == 200) {
                                    finish();
                                }
                                else {
                                    Toast.makeText(PostingActivity.this, "다시 수정 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Log.i("Posting 수정실패", t.getMessage());
                                Toast.makeText(PostingActivity.this, "다시 수정 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //TODO: 삭제해야하는 부분!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        editPostInfo.getPostInfoSimple().setPostTitle(etTitle.getText().toString().trim());    //제목 변경
                        editPostInfo.setPostContent(etContent.getText().toString());                    //내용 변경
                        editPostInfo.getPostInfoSimple().setWriterTown(tvTown.getText().toString());    //동네 변경
                        editPostInfo.getPostInfoSimple().setPostTag(tmpTag);        //태그 추가
                        //삭제해야하는 부분 끝!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    }
                    else    //수정 사항이 없음
                        finish();
                }
                else {      //글쓰기 완료
                    isCompleted = true;

                    //TODO: 사진 확인해봐야함!!!(해상도를 1024*1024로 맞춰야함!) -> 서버에서 해줌!!
                    ArrayList<MultipartBody.Part> postImage = new ArrayList<>();
                    for (PhotoInfo photo : tmpPhotos) {
                        postImage.add(MultipartBody.Part.createFormData("postImage", photo.getPhotoFile().getName(), RequestBody.create(MediaType.parse("multipart/form-data"), photo.getPhotoFile())));
                    }
                    Map<String, RequestBody> dataMap = new HashMap<>();
                    dataMap.put("userId", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userInfo.getUserID())));
                    dataMap.put("location", RequestBody.create(MediaType.parse("application/json"), String.valueOf(location)));
                    dataMap.put("title", RequestBody.create(MediaType.parse("text/plain"), etTitle.getText().toString().trim()));
                    dataMap.put("content", RequestBody.create(MediaType.parse("text/plain"), etContent.getText().toString().trim()));
                    dataMap.put("tags", RequestBody.create(MediaType.parse("multipart/form-data"), new Gson().toJson(tmpTag)));

                    RetrofitClient.getApiService().postNewPost(postImage, dataMap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            Log.i("Posting 연결성공1", response.toString());
                            Log.i("Posting 연결성공1", response.body());
                            if (response.code() == 201) {
                                //TODO: DB에 글 업로드 완료:)라는 의미임
                                //postId를 받아 저장해야 하며, 이미지의 url로 바꿔야 함!!!
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.i("Posting 연결실패", t.getMessage());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void finish() {
        if (isCompleted) {      //글 추가를 한 것이므로 새로고침 해야 함!
            Intent intentComplete = new Intent();
            if (isEditingPost) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("postInfo", editPostInfo);     //TODO: 삭제해야하는 부분!!!
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
        ConstraintLayout clWhole = findViewById(R.id.posting_wholeCL);
        clWhole.setOnTouchListener((v, event) -> {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  //키보드 안보이게 하기 위한 InputMethodManager객체
            getCurrentFocus().clearFocus();

            return false;
        });

        llTown.setOnClickListener(v -> {
            Intent itAddress = new Intent(PostingActivity.this, DaumAddressActivity.class);  //도로명주소 API 실행
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        llPhotos.setOnClickListener(v -> {
            if (isEditingPost) {
                Toast.makeText(PostingActivity.this, "글 수정 시에는 사진을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                //사진 불러오기
                if (tmpPhotos.size() < 5) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_FROM_GALLERY);
                }
                else
                    Toast.makeText(PostingActivity.this, "사진은 최대 5장 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
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
                        if (!tmpTag.contains(tag)) {
                            canAddTag = true;
                            tmpTag.add(tag);
                        }

                        if (canAddTag) {
                            @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_tag, null);
                            TextView tvTag = view.findViewById(R.id.tag_tagName);
                            tvTag.setText(tag);
                            ImageView ivCancel = view.findViewById(R.id.tag_cancel);
                            ivCancel.setOnClickListener(v -> {
                                flTags.removeView((View) v.getParent());
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK){
            try {
                Uri uri = Objects.requireNonNull(data).getData();
                String photoPath = getRealPathFromURI(uri);

                File file = new File(Objects.requireNonNull(photoPath));     //갤러리에서 선택한 파일
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data).getData());
                Bitmap photoBM = BitmapFactory.decodeStream(is);
                is.close();

                PhotoInfo photo = new PhotoInfo(file, photoBM);
                tmpPhotos.add(photo);
                tvPhotos.setText("사진("+tmpPhotos.size()+"/5)");

                if (tmpPhotos.size() == 1)
                    llShowPhotos.setVisibility(View.VISIBLE);

                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_photo, null);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                params.rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                view.setLayoutParams(params);
                ImageView ivPhoto = view.findViewById(R.id.photo_iv);
                    ivPhoto.setImageBitmap(photoBM);
                ImageView ivCancel = view.findViewById(R.id.photo_cancel);
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

        else if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            new GetGEOTask(this, "posting", Objects.requireNonNull(data).getExtras().getString("address")).execute();
        }
    }

    //갤러리에서 선택한 사진의 절대경로를 반환함
    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        try (Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null)) {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    //동네 설정 후 화면 설정
    public void postingSettingTown(JsonObject loc) {
        location = loc;
        tmpFullAddress = loc.get("fullAddress").getAsString();
        tvTown.setText(loc.get("dong").getAsString());
    }
}
