package com.tave7.dobdob;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final int POST_REQUEST = 6000;  //requestCode로 사용될 상수(마이페이지)
    static final int MYPAGE_REQUEST = 7000;  //requestCode로 사용될 상수(마이페이지)

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

        String userProfileUrl = PreferenceManager.getString(MainActivity.this, "userProfileUrl");
        String userName = PreferenceManager.getString(MainActivity.this, "userName");
        String userTown = PreferenceManager.getString(MainActivity.this, "userTown");
        userInfo = new UserInfo(userProfileUrl, userName, userTown);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);      //툴바 설정
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);    //기본 제목을 없앰
        View customView = LayoutInflater.from(this).inflate(R.layout.main_actionbar, null);
        actionBar.setCustomView(customView);
        toolbarListener(toolbar);

        //TODO: 임시 postList 생성
            postList = new ArrayList<>();       //이후에 DB에서 내용 가져와서 =를 사용해야함
            totalPostList = new ArrayList<>();
            ArrayList<String> tmpTag = new ArrayList<>();
                tmpTag.add("산책");
                tmpTag.add("동네산책");
                tmpTag.add("4명모집");
            ArrayList<String> tmpHeartUsers = new ArrayList<>();
                tmpHeartUsers.add("생귤");
                tmpHeartUsers.add("테이비");     tmpHeartUsers.add("테이비2");
                tmpHeartUsers.add("테이비3");     tmpHeartUsers.add("테이비4");
            totalPostList.add(new PostInfoSimple("", "테이비", "신사동", "2021.05.16 20:00", "오늘 저녁에 산책할 사람 구해요!", tmpHeartUsers, 4, tmpTag));
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
            totalPostList.add(new PostInfoSimple("", "자전거탄풍경", "개포동", "2021.05.21 21:00", "오늘 저녁에 같이 자전거 탈 사람 구해요!", tmpHeartUsers2, 0, tmpTag2));
            totalPostList.add(new PostInfoSimple("", "테이비1", "인사동", "2021.05.18 15:00", "개별 포장 빨대 200개 공구하실 분 구합니다!", tmpHeartUsers3, 2, tmpTag2));
            totalPostList.add(new PostInfoSimple("", "테이비2", "개포동", "2021.05.20 11:30", "맥모닝 같이 먹을 사람 구해요!", null, 0, null));
            totalPostList.add(new PostInfoSimple("", "테이비", "한남동", "2021.05.21 13:10", "동네에 맛있는 반찬 가게 알려주세요!", tmpHeartUsers4, 39, null));
            postList.addAll(totalPostList);     //TODO: 삭제했을 때 영향 미치는 지 확인해야 함

        rvPost = (RecyclerView) findViewById(R.id.mainPost);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL,false);
        rvPost.setLayoutManager(manager);
        adapter = new PostRecyclerAdapter(postList, totalPostList, userInfo);
        rvPost.setAdapter(adapter);      //어댑터 등록
        rvPost.addItemDecoration(new DividerItemDecoration(MainActivity.this, 1)); //리스트 사이의 구분선 설정

        FloatingActionButton fabAddPost = (FloatingActionButton) findViewById(R.id.mainFabAddPost);
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostingActivity.class));    //글쓰기 창으로 화면이 넘어감
            }
        });
    }

    public void toolbarListener(Toolbar toolbar){
        tvTown = (TextView) toolbar.findViewById(R.id.toolbar_town);
        tvTown.setText(userInfo.getUserTown());     //초기에 user가 설정한 동네로 보여줌

        tvTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 동네를 클릭했을 때, 동네 변경이 가능해야 함 + DB에 저장
                //user.getUserTown() = "설정한 동네"
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);

        MenuItem mSearch = menu.findItem(R.id.search);
        mSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchTitleTag("");    //초기로 돌려놓음
                return true;
            }
        });
        SearchView sv = (SearchView) mSearch.getActionView();
        //sv.setSubmitButtonEnabled(true);      TODO: 필요한가?
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
                    searchTitleTag("");    //초기로 돌려놓음

                return true;
            }
        });

        MenuItem mProfile = menu.findItem(R.id.profile);
        mProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent showMyPage = new Intent(MainActivity.this, MyPageActivity.class);
                Bundle smpBundle = new Bundle();
                    smpBundle.putBoolean("isMyPage", true);
                    smpBundle.putSerializable("userPosts", adapter.searchUserPosts(userInfo.getUserName()));
                    smpBundle.putSerializable("userInfo", userInfo);
                showMyPage.putExtras(smpBundle);
                startActivityForResult(showMyPage, MYPAGE_REQUEST);

                return true;
            }
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
                if (data.getExtras().getString("userName").length()>0)
                    userInfo.setUserName(data.getExtras().getString("userName"));
                if (data.getExtras().getString("userTown").length()>0) {
                    tvTown.setText(data.getExtras().getString("userTown"));
                        //TODO: 동네에 대한 post가 바뀌어야 함
                    userInfo.setUserTown(data.getExtras().getString("userTown"));
                }
            }

            else if (requestCode == POST_REQUEST){
                Log.i("확인용", "post_request");
            }
        }

        //TODO: 이후에 post글 추가를 한다면 동네에 대한 post를 최신으로 새로고침해야 함
        //마이페이지를 봤다면 그대로 내버려둠
        //글의 세부 내용을 보는 거라면 그대로 내버려둠
    }
}
