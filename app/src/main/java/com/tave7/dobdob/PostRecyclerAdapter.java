package com.tave7.dobdob;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tave7.dobdob.MainActivity.POST_REQUEST;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    private Context context;
    private boolean isMain = true;
    private ArrayList<PostInfoSimple> postList = null;
    private ArrayList<PostInfoSimple> totalPostList = null;    //메인에서 보여줄 postList의 복사본
    private UserInfo userInfo;

    public PostRecyclerAdapter(ArrayList<PostInfoSimple> postList, UserInfo userInfo) {      //MyPageActivity에서 호출
        isMain = false;        //태그 클릭 시 태그에 대한 게시물 검색이 안됨
        this.postList = postList;
        this.userInfo = userInfo;
    }

    public PostRecyclerAdapter(ArrayList<PostInfoSimple> postList, ArrayList<PostInfoSimple> totalPostList, UserInfo userInfo) {   //MainActivity에서 호출
        this.postList = postList;
        this.totalPostList = totalPostList;
        this.userInfo = userInfo;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
        View view = inflater.inflate(R.layout.postrow, parent, false);
        PostViewHolder viewHolder = new PostViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        //holder.writerProfile.setImageURI("");     //TODO: 이미지 URL을 보이게 함
        holder.writerName.setText(postList.get(position).getWriterName());
        holder.postTime.setText(postList.get(position).getPostTime());
        holder.postTitle.setText(postList.get(position).getPostTitle());

        boolean isClickedHeart = false;
        for (String user: postList.get(position).getHeartUsers()) {
            if (user.equals(userInfo.getUserName())) {
                isClickedHeart = true;
                break;
            }
        }
        if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우
            holder.ivHeart.setImageResource(R.drawable.heart_full);
        else
            holder.ivHeart.setImageResource(R.drawable.heart_empty);

        boolean IsHeartFull = isClickedHeart;
        holder.ivHeart.setOnClickListener(v -> {
            if (IsHeartFull) {       //기존에는 하트가 눌렸었지만 하트를 취소함
                postList.get(position).getHeartUsers().remove(userInfo.getUserName());
                //TODO: DB에 하트를 취소했다고 추가해야 함!!
            }
            else {
                postList.get(position).getHeartUsers().add(userInfo.getUserName());
                //TODO: DB에 하트를 클릭했다고 추가해야 함!!
            }

            notifyDataSetChanged();
        });
        holder.heartNum.setText(String.valueOf(postList.get(position).getHeartUsers().size()));

        holder.commentNum.setText(String.valueOf(postList.get(position).getCommentNum()));

        holder.tags.removeAllViews();       //기존에 있는 태그들 초기화
        if (postList.get(position).getPostTag() != null && postList.get(position).getPostTag().size() != 0) {
            for (String tagName : postList.get(position).getPostTag()){
                TextView tvTag = new TextView(context);
                tvTag.setText("#"+tagName+" ");
                tvTag.setTypeface(null, Typeface.BOLD);
                tvTag.setTextColor(Color.parseColor("#5AAEFF"));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvTag.setLayoutParams(layoutParams);
                holder.tags.addView(tvTag);

                tvTag.setOnClickListener(v -> {
                    if (isMain) {       //TODO: 마이페이지에서 태그 검색이 가능하게 할 것인가?
                        String searchTag = tvTag.getText().toString().substring(1, tvTag.getText().length()-1);

                        Intent showContainTagPost = new Intent(context, TagPostActivity.class);
                        Bundle sctBundle = new Bundle();
                        sctBundle.putString("tagName", searchTag);
                        sctBundle.putSerializable("tagPostLists", searchTagPosts(searchTag));
                        sctBundle.putSerializable("userInfo", userInfo);
                        showContainTagPost.putExtras(sctBundle);
                        context.startActivity(showContainTagPost);
                    }
                });
            }
        }
        else
            holder.tags.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void changeWriterName(String beforeName, String afterName) {     //MyPage에서 이름 변경 적용 시
        for (PostInfoSimple pi : postList){
            pi.setWriterName(afterName);

            for (int i=0; i<pi.getHeartUsers().size(); i++) {
                if (pi.getHeartUsers().get(i).equals(beforeName))
                    pi.getHeartUsers().set(i, afterName);
            }
        }
        //TODO: 댓글에 writer가 존재하는 경우에는 post를 서버에서 받아오는 것으로 하자!(댓글은 PostDetail이기 때문에 Post를 클릭 시 DB에서 댓글 내용을 받아옴으로 괜찮음)
        notifyDataSetChanged();
    }

    public ArrayList<PostInfoSimple> searchTagPosts(String searchText) {     //태그가 포함된 검색
        ArrayList<PostInfoSimple> tmpTagPosts = new ArrayList<>();

        for (PostInfoSimple pi : totalPostList) {
            for (String tag : pi.getPostTag()) {
                if (tag.equals(searchText))
                    tmpTagPosts.add(pi);
            }
        }

        return tmpTagPosts;
    }

    public ArrayList<PostInfoSimple> searchUserPosts(String userName) {
        ArrayList<PostInfoSimple> tmpUserPosts = new ArrayList<>();

        for (PostInfoSimple pi : totalPostList) {
            if (pi.getWriterName().equals(userName))
                tmpUserPosts.add(pi);
        }

        return tmpUserPosts;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView writerProfile;      //TODO: 사진 받아와서 이 사진으로 설정함
        TextView writerName, postTime, postTitle, heartNum, commentNum;
        ImageView ivHeart;
        LinearLayout tags;
        
        PostViewHolder(final View itemView) {
            super(itemView);
            writerProfile = (CircleImageView) itemView.findViewById(R.id.postrow_profile);
            writerName = (TextView) itemView.findViewById(R.id.postrow_name);
            postTime = (TextView) itemView.findViewById(R.id.postrow_time);
            postTitle = (TextView) itemView.findViewById(R.id.postrow_title);
            ivHeart = (ImageView) itemView.findViewById(R.id.postrow_ivHeart);
            heartNum = (TextView) itemView.findViewById(R.id.postrow_heartNum);
            commentNum = (TextView) itemView.findViewById(R.id.postrow_commentNum);
            tags = (LinearLayout) itemView.findViewById(R.id.postrow_LinearTag);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION) {
                    //선택한 post의 세부 내용을 다른 화면에 보여줌
                    //TODO: DB에 post 작성자의 이름과 시간을 전달한 후에, 해당 내용을 받아옴
                    Intent showPostPage = new Intent(context, PostActivity.class);
                    Bundle sppBundle = new Bundle();
                    sppBundle.putSerializable("seeUserInfo", userInfo);
                    //sppBundle.putSerializable("postInfoDetail", postInfoDetail);        //PostInfoDetail postInfoDetail;
                    sppBundle.putSerializable("postInfo", postList.get(pos));       //TODO: 변경 필요!!
                    showPostPage.putExtras(sppBundle);
                    ((MainActivity)context).startActivityForResult(showPostPage, POST_REQUEST);     //해당 글 창으로 넘어감
                }
            });
        }
    }
}
