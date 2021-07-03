package com.tave7.dobdob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.InitialSettingActivity.DAUMADDRESS_REQUEST;

public class MainActivity extends AppCompatActivity {
    public static final int POSTING_REQUEST = 6000;
    public static final int POST_REQUEST = 7000;
    public static final int MYPAGE_REQUEST = 8000;

    public static UserInfo myInfo = null;       //로그인한 사용자의 정보
    private JsonObject location;        //TODO: 현재 표시하고 있는 지역이 어디인지를 보여줌!
    private ArrayList<PostInfoSimple> postList = null;        //메인에서 보여줄 postList
    private ArrayList<PostInfoSimple> totalPostList = null;   //메인에서 보여줄 postList의 복사본
    private boolean isSearchFocus = false;

    private ActionBar actionBar;
    private CircleImageView civSubMenuUser;
    private LinearLayout llTown;
    private SearchView svSearch;
    private TextView tvTown;
    private SwipeRefreshLayout srlPost;
    private RecyclerView rvPost;
    private PostRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //location = new JsonObject();    //TODO: location을 저장해서 들고 있어야 함!!!

        Toolbar toolbar = findViewById(R.id.main_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);    //기본 제목을 없앰
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_main, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        postList = new ArrayList<>();
        totalPostList = new ArrayList<>();

        srlPost = findViewById(R.id.main_swipeRL);
        srlPost.setDistanceToTriggerSync(400);
        srlPost.setOnRefreshListener(() -> {
            //TODO: allPosts로 들고 옴!!!(지역 전달해야함!!)
            updatePostList(true);
        });
        rvPost = findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(postList, totalPostList);
        rvPost.setAdapter(adapter);      //어댑터 등록
        DividerItemDecoration devider=new DividerItemDecoration(MainActivity.this, 1);
        devider.setDrawable(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.list_dvide_bar, null)));
        rvPost.addItemDecoration(devider); //리스트 사이의 구분선 설정

        FloatingActionButton fabAddPost = findViewById(R.id.mainFabAddPost);
        fabAddPost.setOnClickListener(v -> {    //글쓰기 창으로 화면이 넘어감
            startActivityForResult(new Intent(MainActivity.this, PostingActivity.class), POSTING_REQUEST);
        });

        updatePostList(false);   //추후에 지역을 전달해서 allPost를 받아와야 함!
    }

    public void toolbarListener(Toolbar toolbar){
        tvTown = toolbar.findViewById(R.id.toolbar_town);
        tvTown.setText(myInfo.getUserTown());     //초기에 user가 설정한 동네로 보여줌

        llTown = toolbar.findViewById(R.id.toolbar_main_town);
        llTown.setOnClickListener(v -> {
            Intent itAddress = new Intent(MainActivity.this, DaumAddressActivity.class);  //도로명주소 API 실행
            startActivityForResult(itAddress, DAUMADDRESS_REQUEST);
        });

        svSearch = findViewById(R.id.toolbar_search);
        svSearch.setQueryHint("제목 및 태그 검색");
        svSearch.setOnSearchClickListener(v -> {
            isSearchFocus = true;
            llTown.setVisibility(View.GONE);
        });
        svSearch.setOnCloseListener(() -> {
            isSearchFocus = false;
            searchTitleTag("");    //초기로 돌려놓음(삭제요망)
            //TODO: DB에 키워드를 전달해 값을 반환받음!!(totalPostList 초기화 해야 함!!)
            svSearch.onActionViewCollapsed();
            llTown.setVisibility(View.VISIBLE);

            return true;
        });
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {    //SearchView의 검색 이벤트
            @Override
            public boolean onQueryTextSubmit(String query) {        //검색버튼을 눌렀을 경우
                searchTitleTag(query);      //TODO: DB에 키워드를 전달해 값을 반환받음!!

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {      //텍스트가 바뀔때마다 호출
                if (newText.length() == 0)
                    searchTitleTag("");  //초기로 돌려놓음

                return true;
            }
        });

        civSubMenuUser = findViewById(R.id.toolbar_subMenuUser);
        if (myInfo.getUserProfileUrl() == null)
            civSubMenuUser.setImageResource(R.drawable.user);
        else {
            Bitmap userProfile = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.user, null))).getBitmap();
            try {
                userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            civSubMenuUser.setImageBitmap(userProfile);
        }
        civSubMenuUser.setOnClickListener(view -> {
            final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
            getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.mypage)
                    startActivityForResult(new Intent(MainActivity.this, MyPageActivity.class), MYPAGE_REQUEST);
                else {  //로그아웃  TODO: Main에서 바로 Login으로 갈 수 있는 지??! 중간에 쌓인 스택들은 없는 지 확인!
                    PreferenceManager.removeKey(MainActivity.this, "access_token");         //어세스 토크 삭제 TODO: 수정 요망!!!!!!!!!!
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                    finish();
                }
                return false;
            });
            popupMenu.show();
        });
    }

    public void searchTitleTag(String searchText) {     //제목 검색
        postList.clear();   //리스트를 초기화 함

        if (searchText.length() == 0)     //검색어 입력이 없을 경우
            postList.addAll(totalPostList);
        else {      //입력한 검색어가 있을 경우
            for (PostInfoSimple pi : totalPostList) {
                if (pi.getPostTitle().toLowerCase().contains(searchText)) {
                    postList.add(pi);
                }
                else {
                    for (String tag : pi.getPostTag()) {
                        if (tag.contains(searchText))
                            postList.add(pi);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (isSearchFocus) {
            isSearchFocus = false;
            searchTitleTag("");     //TODO: 변경할 것 인지 고민해보자!
            svSearch.onActionViewCollapsed();
            Log.i("확인용", "1");
            llTown.setVisibility(View.VISIBLE);
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == MYPAGE_REQUEST) {        //MyPage에서 User 정보 변경 시 적용 위함
                if (Objects.requireNonNull(data).hasExtra("isChanged")) {
                    if (myInfo.getUserProfileUrl() == null)
                        civSubMenuUser.setImageResource(R.drawable.user);
                    else {
                        Bitmap userProfile = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.user, null))).getBitmap();
                        try {
                            userProfile = new DownloadFileTask(myInfo.getUserProfileUrl()).execute().get();
                        } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                        civSubMenuUser.setImageBitmap(userProfile);
                    }
                    tvTown.setText(myInfo.getUserTown());
                    updatePostList(false);       //리팩토링 해야 함!!
                }
            }

            else if (requestCode == POST_REQUEST) {
                Log.i("확인용", "post_request");
                updatePostList(false);
            }

            else if (requestCode == POSTING_REQUEST) {
                Log.i("확인용", "posting_request");
                updatePostList(false);
            }
            else if (requestCode == DAUMADDRESS_REQUEST) {
                new GetGEOTask(this, "main", Objects.requireNonNull(data).getExtras().getString("address")).execute();
            }
        }
        //TODO: 이후에 post글 추가를 한다면 동네에 대한 post를 최신으로 새로고침해야 함
        //마이페이지를 봤다면 그대로 내버려둠
        //글의 세부 내용을 보는 거라면 그대로 내버려둠
    }

    public void mainSettingTown(JsonObject loc) {
        location = loc;
        tvTown.setText(loc.get("dong").getAsString());     //초기에 user가 설정한 동네로 보여줌

        updatePostList(false);
        //updatePostList(false, location);     //TODO: 지역을 전달해야 함(서버로부터 해당 지역의 동네를 받아야 함!)
    }

    public void updatePostList(boolean isSwipe) {      //TODO: 지역을 받아야 전달해야 함!!!!!!!!!!
        //변경사항이 있으므로 다시 받아옴
        RetrofitClient.getApiService().getAllPost().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("MainA 전체글 새로고침 성공", response.toString());
                Log.i("MainA 전체글 새로고침 성공2", response.body());
                if (isSwipe)
                    srlPost.setRefreshing(false);

                if (response.code() == 200) {
                    totalPostList.clear();
                    postList.clear();
                    try {
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                        JSONArray jsonPosts = result.getJSONArray("posts");
                        for (int i=0; i < jsonPosts.length(); i++) {
                            JSONObject postObject = jsonPosts.getJSONObject(i);

                            int postID = postObject.getInt("id");
                            JSONObject userObject = postObject.getJSONObject("User");
                            //TODO: 동네도 넣어야 함!!!!!!!!!!!!!!!!!!!!!!!!
                            UserInfo writerInfo;
                            if (userObject.isNull("profileUrl"))
                                writerInfo = new UserInfo(userObject.getInt("id"), null, userObject.getString("nickName"), "");
                            else
                                writerInfo = new UserInfo(userObject.getInt("id"), userObject.getString("profileUrl"), userObject.getString("nickName"), "");
                            String postTime = postObject.getString("createdAt");
                            String title = postObject.getString("title");
                            //하트 수
                            int commentNum = 3;     //TODO: 서버로부터 받아와야 함!!
                            //태그 받아옴!
                            PostInfoSimple post = new PostInfoSimple(postID, writerInfo, postTime, title, null, commentNum, null);
                            totalPostList.add(post);
                        }
                        postList.addAll(totalPostList);
                    } catch (JSONException e) { e.printStackTrace(); }
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(MainActivity.this, "전체 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("Main 전체글 연결실패", t.getMessage());
                if (isSwipe)
                    srlPost.setRefreshing(false);
                Toast.makeText(MainActivity.this, "서버에 연결이 되지 않았습니다.\n 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
