package com.tave7.dobdob.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.DownloadFileTask;
import com.tave7.dobdob.MainActivity;
import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.PostActivity;
import com.tave7.dobdob.R;
import com.tave7.dobdob.TagPostActivity;
import com.tave7.dobdob.data.PostInfoSimple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tave7.dobdob.MainActivity.POST_REQUEST;
import static com.tave7.dobdob.MainActivity.myInfo;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    private Context context;
    private boolean isMain = true;
    private ArrayList<PostInfoSimple> postList;
    private ArrayList<PostInfoSimple> totalPostList = null;    //메인에서 보여줄 postList의 복사본

    public PostRecyclerAdapter(ArrayList<PostInfoSimple> postList) {      //MyPageActivity에서 호출
        isMain = false;        //태그 클릭 시 태그에 대한 게시물 검색이 안됨
        this.postList = postList;
    }

    public PostRecyclerAdapter(ArrayList<PostInfoSimple> postList, ArrayList<PostInfoSimple> totalPostList) {   //MainActivity에서 호출
        this.postList = postList;
        this.totalPostList = totalPostList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
        View view = inflater.inflate(R.layout.postrow, parent, false);
        PostViewHolder viewHolder = new PostViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Bitmap writerProfile = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        if (postList.get(position).getWriterProfileUrl() != null) {
            try {
                writerProfile = new DownloadFileTask(postList.get(position).getWriterProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        }
        holder.writerProfile.setImageBitmap(writerProfile);
        holder.writerName.setText(postList.get(position).getWriterName());
        holder.writerName.setOnClickListener(v -> {
            Intent showProfilePage = new Intent(context, MyPageActivity.class);
            Bundle sppBundle = new Bundle();
            if (myInfo.getUserID() != postList.get(position).getWriterID())
                sppBundle.putInt("userID", postList.get(position).getWriterID());
            showProfilePage.putExtras(sppBundle);
            context.startActivity(showProfilePage);
        });
        holder.writerTown.setText(postList.get(position).getWriterTown());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        try {
            Date date = sdf.parse(postList.get(position).getPostTime());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String dateString = dateFormat.format(Objects.requireNonNull(date));
            holder.postTime.setText(dateString);
        } catch (ParseException e) { e.printStackTrace(); }
        holder.postTitle.setText(postList.get(position).getPostTitle());

        boolean isClickedHeart = false;

        /*      TODO: 바꿔야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        for (String user: postList.get(position).getHeartUsers()) {
            if (user.equals(myInfo.getUserName())) {
                isClickedHeart = true;
                break;
            }
        }
         */

        if (isClickedHeart)   //사용자가 하트를 누른 사람 중 한명인 경우
            holder.ivHeart.setImageResource(R.drawable.heart_click);
        else
            holder.ivHeart.setImageResource(R.drawable.heart);

        boolean IsHeartFull = isClickedHeart;
        /*  TODO: 바꿔야 함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        holder.ivHeart.setOnClickListener(v -> {
            if (IsHeartFull) {       //기존에는 하트가 눌렸었지만 하트를 취소함
                postList.get(position).getHeartUsers().remove(myInfo.getUserName());
                //TODO: DB에 하트를 취소했다고 추가해야 함!!
            }
            else {
                postList.get(position).getHeartUsers().add(myInfo.getUserName());
                //TODO: DB에 하트를 클릭했다고 추가해야 함!!
            }

            notifyDataSetChanged();
        });
         */
        //holder.heartNum.setText(String.valueOf(postList.get(position).getHeartUsers().size()));
        holder.heartNum.setText(String.valueOf(postList.get(position).getLikeNum()));
        holder.commentNum.setText(String.valueOf(postList.get(position).getCommentNum()));

        holder.tags.removeAllViews();       //기존에 있는 태그들 초기화
        if (postList.get(position).getPostTag() != null && postList.get(position).getPostTag().size() != 0) {
            for (String tagName : postList.get(position).getPostTag()){
                TextView tvTag = new TextView(context);
                tvTag.setText("#"+tagName+" ");
                tvTag.setTypeface(null, Typeface.NORMAL);
                tvTag.setTextColor(Color.parseColor("#1b73d8"));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvTag.setLayoutParams(layoutParams);
                holder.tags.addView(tvTag);

                tvTag.setOnClickListener(v -> {
                    if (isMain) {       //TODO: 마이페이지에서 태그 검색이 가능하게 할 것인가?
                        String searchTag = tvTag.getText().toString().substring(1, tvTag.getText().length()-1);

                        Intent showContainTagPost = new Intent(context, TagPostActivity.class);
                        Bundle sctBundle = new Bundle();
                            sctBundle.putString("tagName", searchTag);
                            sctBundle.putParcelableArrayList("tagPostLists", searchTagPosts(searchTag));
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

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView writerProfile;
        TextView writerName, writerTown, postTime, postTitle, heartNum, commentNum;
        ImageView ivHeart;
        LinearLayout tags;
        
        PostViewHolder(final View itemView) {
            super(itemView);
            writerProfile = itemView.findViewById(R.id.postrow_profile);
            writerName = itemView.findViewById(R.id.postrow_name);
            writerTown = itemView.findViewById(R.id.postrow_town);
            postTime = itemView.findViewById(R.id.postrow_time);
            postTitle = itemView.findViewById(R.id.postrow_title);
            ivHeart = itemView.findViewById(R.id.postrow_ivHeart);
            heartNum = itemView.findViewById(R.id.postrow_heartNum);
            commentNum = itemView.findViewById(R.id.postrow_commentNum);
            tags = itemView.findViewById(R.id.postrow_LinearTag);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    //선택한 post의 세부 내용을 다른 화면에 보여줌
                    Intent showPostPage = new Intent(context, PostActivity.class);
                    Bundle sppBundle = new Bundle();
                        sppBundle.putParcelable("postInfo", postList.get(pos));
                    showPostPage.putExtras(sppBundle);
                    ((MainActivity)context).startActivityForResult(showPostPage, POST_REQUEST);     //해당 글 창으로 넘어감  -> 안됨!!!!!!!!!!
                }
            });
        }
    }
}
