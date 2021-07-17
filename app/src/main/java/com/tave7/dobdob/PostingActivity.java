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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.tave7.dobdob.data.PostInfoDetail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
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
import static com.tave7.dobdob.MainActivity.myInfo;

public class PostingActivity extends AppCompatActivity {
    private static final int PICK_FROM_GALLERY = 100;
    private JsonObject location = null;
    private String tmpFullAddress = "";

    private PostInfoDetail editPostInfo = null;
    private ArrayList<String> tmpTag = null;
    private ArrayList<File> tmpPhotos = null;
    private boolean isEditingPost = false, isCompleted = false;

    private com.nex3z.flowlayout.FlowLayout flTags;
    private EditText etTitle, etContent, etTag;
    private LayoutInflater lInflater;
    private LinearLayout llShowPhotos, llTown, llPhotos;
    private ScrollView svTags;
    private TextView tvPhotos, tvTown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        tmpTag = new ArrayList<>();
        tmpPhotos = new ArrayList<>();
        lInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        etTitle = findViewById(R.id.posting_title);
        llShowPhotos = findViewById(R.id.posting_showPhotos);
            llShowPhotos.setVisibility(View.GONE);
        etContent = findViewById(R.id.posting_content);
        etTag = findViewById(R.id.posting_etTag);
        svTags = findViewById(R.id.posting_svTags);
        flTags = findViewById(R.id.posting_flTags);
        tvPhotos = findViewById(R.id.posting_photo);

        llTown = findViewById(R.id.posting_llTown);
        tvTown = findViewById(R.id.posting_town);
        llPhotos = findViewById(R.id.posting_llPhotos);

        Toolbar toolbar = findViewById(R.id.posting_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_posting, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        if (getIntent().hasExtra("isEditingPost")) {
            isEditingPost = true;
            editPostInfo = getIntent().getExtras().getParcelable("postInfo");

            etTitle.setText(editPostInfo.getPostInfoSimple().getPostTitle());
            if (editPostInfo.getPostImages().size() > 0)
                llShowPhotos.setVisibility(View.VISIBLE);
            for (String photo : editPostInfo.getPostImages()) {
                Bitmap bmp = null;
                try {
                    bmp = new DownloadFileTask(photo).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }

                @SuppressLint("InflateParams") View view = lInflater.inflate(R.layout.item_photo, null);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                view.setLayoutParams(params);
                ImageView ivPhoto = view.findViewById(R.id.photo_iv);
                    ivPhoto.setImageBitmap(bmp);
                ImageView ivCancel = view.findViewById(R.id.photo_cancel);
                    ivCancel.setVisibility(View.GONE);
                llShowPhotos.addView(view);
            }
            etContent.setText(editPostInfo.getPostContent());
            for (String tag : editPostInfo.getPostInfoSimple().getPostTag()) {
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
            tvPhotos.setText("사진(".concat(String.valueOf(editPostInfo.getPostImages().size())).concat("/5)"));
        }

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

                    if (isChgTitle || isChgContent || isChgTown || isChgTag) {
                        isCompleted = true;

                        JsonObject postData = new JsonObject();
                        postData.addProperty("userId", editPostInfo.getPostInfoSimple().getWriterID());
                        postData.add("location", location);
                        postData.addProperty("title", etTitle.getText().toString().trim());
                        postData.addProperty("content", etContent.getText().toString().trim());
                        postData.addProperty("tags", new Gson().toJson(tmpTag));
                        RetrofitClient.getApiService().patchIDPost(PreferenceManager.getString(this, "jwt"), editPostInfo.getPostInfoSimple().getPostID(), postData).enqueue(new Callback<String>() {       //DB전달
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.code() == 200)
                                    finish();
                                else if (response.code() == 419) {
                                    Toast.makeText(PostingActivity.this, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                    PreferenceManager.removeKey(PostingActivity.this, "jwt");
                                    Intent reLogin = new Intent(PostingActivity.this, LoginActivity.class);
                                    reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(reLogin);
                                    finish();
                                }
                                else
                                    Toast.makeText(PostingActivity.this, "다시 수정 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(PostingActivity.this, "다시 수정 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                        finish();
                }
                else {
                    isCompleted = true;

                    ArrayList<MultipartBody.Part> postImage = new ArrayList<>();
                    for (File photo : tmpPhotos) {
                        postImage.add(MultipartBody.Part.createFormData("postImage", photo.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), photo)));
                    }
                    Map<String, RequestBody> dataMap = new HashMap<>();
                    dataMap.put("userId", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(myInfo.getUserID())));
                    dataMap.put("location", RequestBody.create(MediaType.parse("application/json"), String.valueOf(location)));
                    dataMap.put("title", RequestBody.create(MediaType.parse("text/plain"), etTitle.getText().toString().trim()));
                    dataMap.put("content", RequestBody.create(MediaType.parse("text/plain"), etContent.getText().toString().trim()));
                    dataMap.put("tags", RequestBody.create(MediaType.parse("multipart/form-data"), new Gson().toJson(tmpTag)));

                    RetrofitClient.getApiService().postNewPost(PreferenceManager.getString(this, "jwt"), postImage, dataMap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.code() == 201) {
                                finish();
                            }
                            else if (response.code() == 419) {
                                Toast.makeText(PostingActivity.this, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                PreferenceManager.removeKey(PostingActivity.this, "jwt");
                                Intent reLogin = new Intent(PostingActivity.this, LoginActivity.class);
                                reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(reLogin);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(PostingActivity.this, "다시 완료 버튼을 눌러주세요:)", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void finish() {
        if (isCompleted) {
            Intent intentComplete = new Intent();
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
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();

            return false;
        });

        llTown.setOnClickListener(v -> {
            Intent itAddress = new Intent(PostingActivity.this, DaumAddressActivity.class);
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        llPhotos.setOnClickListener(v -> {
            if (isEditingPost) {
                Toast.makeText(PostingActivity.this, "글 수정 시에는 사진을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
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

    public void postingTextChangedListener() {
        etTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0 && s.charAt(s.length()-1)==' ') {      //스페이스바를 누를 시에 태그가 추가됨
                    String tag = s.toString().trim();
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
                            svTags.post(() -> svTags.fullScroll(ScrollView.FOCUS_DOWN));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                Uri uri = Objects.requireNonNull(data).getData();
                String photoPath = getRealPathFromURI(uri);

                File file = new File(Objects.requireNonNull(photoPath));
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data).getData());
                Bitmap photoBM = BitmapFactory.decodeStream(is);
                is.close();

                tmpPhotos.add(file);
                tvPhotos.setText("사진(".concat(String.valueOf(tmpPhotos.size())).concat("/5)"));

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
                    tmpPhotos.remove(file);
                    tvPhotos.setText("사진(".concat(String.valueOf(tmpPhotos.size())).concat("/5)"));

                    if (tmpPhotos.size() == 0)
                        llShowPhotos.setVisibility(View.GONE);
                });
                llShowPhotos.addView(view);
            } catch (Exception e){ e.printStackTrace(); }
        }
        else if (requestCode == DAUMADDRESS_REQUEST && resultCode == RESULT_OK) {
            new GetGEOTask(this, "posting", Objects.requireNonNull(data).getExtras().getString("address")).execute();
        }
    }

    //사진의 절대경로 반환
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
        tmpFullAddress = loc.get("detail").getAsString();
        tvTown.setText(loc.get("dong").getAsString());
    }
}
