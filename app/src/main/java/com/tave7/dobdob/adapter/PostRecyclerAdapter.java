package com.tave7.dobdob.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.nex3z.flowlayout.FlowLayout;
import com.tave7.dobdob.DownloadFileTask;
import com.tave7.dobdob.MainActivity;
import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.PostActivity;
import com.tave7.dobdob.R;
import com.tave7.dobdob.RetrofitClient;
import com.tave7.dobdob.TagPostActivity;
import com.tave7.dobdob.data.PostInfoSimple;
import com.tave7.dobdob.data.UserInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.POST_REQUEST;
import static com.tave7.dobdob.MainActivity.myInfo;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    private Context context;
    private char whereActivity;     //m: MainActivity, p: MyPageActivity, t: TagPostActivity
    private ArrayList<PostInfoSimple> postList;

    public PostRecyclerAdapter(char whereActivity, ArrayList<PostInfoSimple> postList) {
        this.whereActivity = whereActivity;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_post, parent, false);
        PostViewHolder viewHolder = new PostViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Bitmap writerProfile = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        if (postList.get(position).getWriterInfo().getUserProfile() != null)
            writerProfile = BitmapFactory.decodeByteArray(postList.get(position).getWriterInfo().getUserProfile(), 0, postList.get(position).getWriterInfo().getUserProfile().length);
        else if (postList.get(position).getWriterInfo().getUserProfile() == null && postList.get(position).getWriterProfileUrl() != null) {
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

        if (postList.get(position).getMyLikePos() != -1)
            holder.ivHeart.setImageResource(R.drawable.heart_click);
        else
            holder.ivHeart.setImageResource(R.drawable.heart);
        holder.ivHeart.setOnClickListener(v -> {
            if (postList.get(position).getMyLikePos() != -1) {
                RetrofitClient.getApiService().deleteIDLike(myInfo.getUserID(), postList.get(position).getPostID()).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 좋아요취소 성공", response.body());

                        if (response.code() == 200) {
                            postList.get(position).getLikes().remove(postList.get(position).getMyLikePos());
                            postList.get(position).setMyLikePos(-1);
                            notifyItemChanged(position);
                        }
                        else
                            Toast.makeText(context, "죄송합니다. 다시 시도해 주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                JsonObject likeInfo = new JsonObject();
                likeInfo.addProperty("userId", myInfo.getUserID());
                likeInfo.addProperty("postId", postList.get(position).getPostID());
                RetrofitClient.getApiService().postLike(likeInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.i("PostA 좋아요 성공", response.body());

                        if (response.code() == 201) {
                            postList.get(position).getLikes().add(new UserInfo(myInfo.getUserID(), myInfo.getUserProfileUrl(), myInfo.getUserName()));
                            postList.get(position).setMyLikePos(postList.get(position).getLikes().size()-1);
                            notifyItemChanged(position);
                        }
                        else
                            Toast.makeText(context, "죄송합니다. 다시 시도해 주세요:)", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        holder.heartNum.setText(String.valueOf(postList.get(position).getLikes().size()));

        holder.commentNum.setText(String.valueOf(postList.get(position).getCommentNum()));

        holder.tags.removeAllViews();
        if (postList.get(position).getPostTag() != null && postList.get(position).getPostTag().size() != 0) {
            holder.tagDivider.setVisibility(View.VISIBLE);
            holder.tags.setVisibility(View.VISIBLE);

            for (String tagName : postList.get(position).getPostTag()){
                TextView tvTag = new TextView(context);
                tvTag.setText("#".concat(tagName));
                tvTag.setTypeface(null, Typeface.NORMAL);
                tvTag.setTextColor(Color.parseColor("#1b73d8"));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvTag.setLayoutParams(layoutParams);
                holder.tags.addView(tvTag);

                tvTag.setOnClickListener(v -> {
                    String searchTag = tvTag.getText().toString().substring(1);

                    Intent showContainTagPost = new Intent(context, TagPostActivity.class);
                    Bundle sctBundle = new Bundle();
                        sctBundle.putString("tagName", searchTag);
                    showContainTagPost.putExtras(sctBundle);
                    context.startActivity(showContainTagPost);
                });
            }
        }
        else {
            holder.tagDivider.setVisibility(View.GONE);
            holder.tags.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView writerProfile;
        TextView writerName, writerTown, postTime, postTitle, heartNum, commentNum;
        View tagDivider;
        ImageView ivHeart;
        FlowLayout tags;
        
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
            tagDivider = itemView.findViewById(R.id.postrow_Divider);
            tags = itemView.findViewById(R.id.postrow_flTags);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Intent showPostPage = new Intent(context, PostActivity.class);
                    Bundle sppBundle = new Bundle();
                        sppBundle.putParcelable("postInfo", postList.get(pos));
                    showPostPage.putExtras(sppBundle);
                    if (whereActivity == 'm')
                        ((MainActivity) context).startActivityForResult(showPostPage, POST_REQUEST);
                    else if (whereActivity == 'p')
                        ((MyPageActivity) context).startActivityForResult(showPostPage, POST_REQUEST);
                    else
                        ((TagPostActivity) context).startActivityForResult(showPostPage, POST_REQUEST);
                }
            });
        }
    }
}
