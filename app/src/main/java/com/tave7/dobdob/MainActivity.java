package com.tave7.dobdob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tave7.dobdob.adapter.PostRecyclerAdapter;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final int POSTING_REQUEST = 5000;
    public static final int POST_REQUEST = 6000;
    public static final int MYPAGE_REQUEST = 7000;

    UserInfo userInfo = null;
    ArrayList<PostInfoSimple> postList = null;        //메인에서 보여줄 postList
    ArrayList<PostInfoSimple> totalPostList = null;   //메인에서 보여줄 postList의 복사본

    TextView tvTown;
    RecyclerView rvPost;
    PostRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: UserInfo를 받은 값을 넘겨받아야 함!!!
        String userProfileUrl = PreferenceManager.getString(MainActivity.this, "userProfileUrl");
        byte[] userProfile = Base64.decode(userProfileUrl.getBytes(), Base64.DEFAULT);
        String userName = PreferenceManager.getString(MainActivity.this, "userName");
        String userTown = PreferenceManager.getString(MainActivity.this, "userTown");
        userInfo = new UserInfo(userProfile, userName, userTown);

        Toolbar toolbar = findViewById(R.id.main_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);    //기본 제목을 없앰
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_main, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        postList = new ArrayList<>();
        totalPostList = new ArrayList<>();
        RetrofitClient.getApiService().getAllPost().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("MainA 전체글 성공", response.toString());
                Log.i("MainA 전체글 성공2", response.body());
                if (response.code() == 200) {
                    //TODO: 글 모두 저장함(totalPostList에 넣고 postList에 addAll함!)
                }
                else {
                    Toast.makeText(MainActivity.this, "전체 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("Main 전체글 연결실패", t.getMessage());
                Toast.makeText(MainActivity.this, "서버에 연결이 되지 않았습니다.\n 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        //TODO: 임시 postList 생성
            ArrayList<String> tmpTag = new ArrayList<>();
                tmpTag.add("산책");
                tmpTag.add("동네산책");
                tmpTag.add("4명모집");
            ArrayList<String> tmpHeartUsers = new ArrayList<>();
                tmpHeartUsers.add("생귤");
                tmpHeartUsers.add("테이비");     tmpHeartUsers.add("테이비2");
                tmpHeartUsers.add("테이비3");     tmpHeartUsers.add("테이비4");
            totalPostList.add(new PostInfoSimple(new UserInfo(null, "테이비", "신사동"), "2021.05.16 20:00", "오늘 저녁에 산책할 사람 구해요!", tmpHeartUsers, 4, tmpTag));
            ArrayList<String> tmpTag2 = new ArrayList<>();
                tmpTag2.add("자전거타기");
            ArrayList<String> tmpHeartUsers2 = new ArrayList<>();
                tmpHeartUsers2.add("테이브");      tmpHeartUsers2.add("테이비7");
                tmpHeartUsers2.add("테이비");      tmpHeartUsers2.add("테이비2");
                tmpHeartUsers2.add("테이비3");     tmpHeartUsers2.add("테이비4");
            ArrayList<String> tmpHeartUsers3 = new ArrayList<>();
                tmpHeartUsers3.add("테이비");      tmpHeartUsers3.add("테이비5");
                tmpHeartUsers3.add("테이비3");     tmpHeartUsers3.add("테이비7");
            ArrayList<String> tmpHeartUsers4 = new ArrayList<>();
                tmpHeartUsers4.add("생귤");
                tmpHeartUsers4.add("테이비3");     tmpHeartUsers4.add("테이비7");
            totalPostList.add(new PostInfoSimple(new UserInfo(null, "자전거탄풍경", "개포동"), "2021.05.21 21:00", "오늘 저녁에 같이 자전거 탈 사람 구해요!", tmpHeartUsers2, 0, tmpTag2));
            totalPostList.add(new PostInfoSimple(new UserInfo(null, "테이비1", "인사동"), "2021.05.18 15:00", "개별 포장 빨대 200개 공구하실 분 구합니다!", tmpHeartUsers3, 2, tmpTag2));
            totalPostList.add(new PostInfoSimple(new UserInfo(null, "테이비2", "청파동"), "2021.05.20 11:30", "맥모닝 같이 먹을 사람 구해요!", null, 0, null));
            totalPostList.add(new PostInfoSimple(new UserInfo(null, "테이비", "한남동"), "2021.05.21 13:10", "동네에 맛있는 반찬 가게 알려주세요!", tmpHeartUsers4, 39, null));
            postList.addAll(totalPostList);     //TODO: 삭제했을 때 영향 미치는 지 확인해야 함

        rvPost = findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(postList, totalPostList, userInfo);
        rvPost.setAdapter(adapter);      //어댑터 등록
        rvPost.addItemDecoration(new DividerItemDecoration(MainActivity.this, 1)); //리스트 사이의 구분선 설정

        FloatingActionButton fabAddPost = findViewById(R.id.mainFabAddPost);
        fabAddPost.setOnClickListener(v -> {
            Intent postingPage = new Intent(MainActivity.this, PostingActivity.class);
            Bundle bundle = new Bundle();
                bundle.putParcelable("userInfo", userInfo);
            postingPage.putExtras(bundle);
            startActivityForResult(postingPage, POSTING_REQUEST);    //글쓰기 창으로 화면이 넘어감
        });
    }

    public void toolbarListener(Toolbar toolbar){
        tvTown = toolbar.findViewById(R.id.toolbar_town);
        tvTown.setText(userInfo.getUserTown());     //초기에 user가 설정한 동네로 보여줌

        tvTown.setOnClickListener(v -> {
            //TODO: 동네를 클릭했을 때, 동네 변경이 가능해야 함 + DB에 저장
            //user.getUserTown() = "설정한 동네"
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.search);
        mSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                searchTitleTag("");    //초기로 돌려놓음

                return true;
            }
        });
        SearchView sv = (SearchView) mSearch.getActionView();
        sv.setQueryHint("제목 및 태그 검색");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {    //SearchView의 검색 이벤트
            @Override
            public boolean onQueryTextSubmit(String query) {        //검색버튼을 눌렀을 경우
                searchTitleTag(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {      //텍스트가 바뀔때마다 호출
                if (newText.length() == 0)
                    searchTitleTag("");  //초기로 돌려놓음
                else
                    searchTitleTag(newText);

                return true;
            }
        });

        MenuItem mProfile = menu.findItem(R.id.profile);
        //mProfile.setIcon()        //TODO 마이페이지 아이콘 설정 보이게 하기!!!!
        mProfile.setOnMenuItemClickListener(item -> {
            Intent showMyPage = new Intent(MainActivity.this, MyPageActivity.class);
            Bundle smpBundle = new Bundle();
                smpBundle.putBoolean("isMyPage", true);
                smpBundle.putParcelableArrayList("userPosts", adapter.searchUserPosts(userInfo.getUserName()));
                smpBundle.putParcelable("userInfo", userInfo);
            showMyPage.putExtras(smpBundle);
            startActivityForResult(showMyPage, MYPAGE_REQUEST);

            return true;
        });
        MenuItem mLogout = menu.findItem(R.id.logout);
        mLogout.setOnMenuItemClickListener(item -> {     //로그아웃  TODO: Main에서 바로 Login으로 갈 수 있는 지??! 중간에 쌓인 스택들은 없는 지 확인!
            PreferenceManager.removeKey(MainActivity.this, "access_token");     //어세스 토크 삭제
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

            finish();

            return true;
        });
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == MYPAGE_REQUEST){        //MyPage에서 User 정보 변경 시 적용 위함
                /*TODO: 확인 요망!
                if (data.getExtras().getString("userProfile").length()>0)
                    userInfo.setUserProfileUrl(data.getExtras().getString("userProfile"));
                 */
                if (data != null && data.hasExtra("userName")) {
                    userInfo.setUserName(data.getExtras().getString("userName"));
                    //TODO: 동네에 대한 post가 갱신되어야 함(혹은 자기 이름의 post를 찾아 이름 변경!)
                }
                if (data != null && data.hasExtra("userTown")) {
                    userInfo.setUserTown(data.getExtras().getString("userTown"));
                    tvTown.setText(userInfo.getUserTown());

                    //TODO: 동네에 대한 post가 갱신되어야 함
                }
            }

            else if (requestCode == POST_REQUEST) {
                Log.i("확인용", "post_request");
            }

            else if (requestCode == POSTING_REQUEST) {
                Log.i("확인용", "posting_request");
            }

            //변경사항이 있으므로 다시 받아옴
            RetrofitClient.getApiService().getAllPost().enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.i("MainA 전체글 새로고침 성공", response.toString());
                    Log.i("MainA 전체글 새로고침 성공2", response.body());
                    if (response.code() == 200) {
                        //totalPostList.clear();
                        //postList.clear();
                        //TODO: 글 모두 저장함(totalPostList에 넣고 postList에 addAll함!
                        //adapter.notifyDataSetChanged(); 해줘야 함!!
                    }
                    else {
                        Toast.makeText(MainActivity.this, "전체 글 로드에 문제가 생겼습니다. 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("Main 전체글 연결실패", t.getMessage());
                    Toast.makeText(MainActivity.this, "서버에 연결이 되지 않았습니다.\n 새로 고침을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //TODO: 이후에 post글 추가를 한다면 동네에 대한 post를 최신으로 새로고침해야 함
        //마이페이지를 봤다면 그대로 내버려둠
        //글의 세부 내용을 보는 거라면 그대로 내버려둠
    }
}
