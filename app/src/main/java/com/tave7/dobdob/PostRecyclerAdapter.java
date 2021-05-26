package com.tave7.dobdob;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    private Context context;
    private boolean isMain = true;
    private ArrayList<PostInfo> postList = null;
    private ArrayList<PostInfo> totalPostList = null;    //메인에서 보여줄 postList의 복사본
    private UserInfo userInfo;

    public PostRecyclerAdapter(ArrayList<PostInfo> postList, UserInfo userInfo) {      //MyPageActivity에서 호출
        isMain = false;        //태그 클릭 시 태그에 대한 게시물 검색이 안됨
        this.postList = postList;
        this.userInfo = userInfo;
    }

    public PostRecyclerAdapter(ArrayList<PostInfo> postList, ArrayList<PostInfo> totalPostList, UserInfo userInfo) {   //MainActivity에서 호출
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

        /* TODO: 수정 요망!!!!!! --> heart를 막 누르면 태그가 안보이는 곳이 있음(메인에 태그가 필요한 것인가...?)
        boolean isClickHeart = isClickedHeart;
        holder.ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("하트수", holder.heartNum.getText().toString());
                if (isClickHeart) {       //기존에는 하트가 눌렸었지만 하트를 취소함
                    //holder.ivHeart.setImageResource(R.drawable.heart_empty);
                    postList.get(position).getHeartUsers().remove(userInfo.getUserName());
                    Log.i("하트 취소 위치", String.valueOf(position));
                    notifyItemChanged(position);
                }
                else {
                    //holder.ivHeart.setImageResource(R.drawable.heart_full);
                    postList.get(position).getHeartUsers().add(userInfo.getUserName());
                    Log.i("하트 클릭 위치", String.valueOf(position));
                    notifyItemChanged(position);
                }
            }
        });

         */
        holder.heartNum.setText(String.valueOf(postList.get(position).getHeartUsers().size()));

        holder.commentNum.setText(String.valueOf(postList.get(position).getCommentNum()));

        //리니어레이아웃에 태그 추가함
        holder.tags.removeAllViews();       //기존에 있는 태그들 초기화
        if (postList.get(position).getPostTag() != null && postList.get(position).getPostTag().size() != 0) {
            Log.i("하트 태그", "태그 있음");
            for (String tagName : postList.get(position).getPostTag()){
                TextView tvTag = new TextView(context);
                tvTag.setText("#"+tagName+" ");
                tvTag.setTypeface(null, Typeface.BOLD);
                tvTag.setTextColor(Color.parseColor("#5AAEFF"));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvTag.setLayoutParams(layoutParams);
                holder.tags.addView(tvTag);

                tvTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMain) {
                            String searchTag = tvTag.getText().toString().substring(1, tvTag.getText().length()-1);

                            Intent showContainTagPost = new Intent(context, TagPostActivity.class);
                            showContainTagPost.putExtra("tagName", searchTag);
                            showContainTagPost.putExtra("tagPostLists", searchTagPosts(searchTag));
                            showContainTagPost.putExtra("userInfo", userInfo);
                            context.startActivity(showContainTagPost);
                        }
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

    public ArrayList<PostInfo> searchTagPosts(String searchText) {     //태그가 포함된 검색
        ArrayList<PostInfo> tmpTagPosts = new ArrayList<>();

        for (PostInfo pi : totalPostList) {
            for (String tag : pi.getPostTag()) {
                if (tag.equals(searchText))
                    tmpTagPosts.add(pi);
            }
        }

        return tmpTagPosts;
    }

    public ArrayList<PostInfo> searchUserPosts(String userName) {
        ArrayList<PostInfo> tmpUserPosts = new ArrayList<>();

        for (PostInfo pi : totalPostList) {
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

            itemView.setOnClickListener(new View.OnClickListener() {        //항목을 클릭했을 때
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        //TODO: 선택한 post의 세부 내용을 다른 화면에 보여줌(Bundle로 position이랑 어떤 글인지 넘겨줘야 함)
                        Intent intent = new Intent(context, PostActivity.class);
                        intent.putExtra("userInfo", userInfo);
                        intent.putExtra("postInfo", postList.get(pos));
                        context.startActivity(intent);    //해당 글 창으로 넘어감
                    }
                }
            });
        }
    }
}
