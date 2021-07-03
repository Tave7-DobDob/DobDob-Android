package com.tave7.dobdob.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.DownloadFileTask;
import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.R;
import com.tave7.dobdob.data.CommentInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tave7.dobdob.MainActivity.myInfo;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentViewHolder> {
    private Context context;
    private ArrayList<CommentInfo> commentList;

    public CommentRecyclerAdapter(ArrayList<CommentInfo> commentList) {
        this.commentList = commentList;
    }

    public void changeCommentList(ArrayList<CommentInfo> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentRecyclerAdapter.CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.commentrow, parent, false);
        CommentRecyclerAdapter.CommentViewHolder viewHolder = new CommentRecyclerAdapter.CommentViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentRecyclerAdapter.CommentViewHolder holder, int position) {
        Bitmap commenterProfile = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        if (commentList.get(position).getCommenterInfo().getUserProfileUrl() != null) {
            try {
                commenterProfile = new DownloadFileTask(commentList.get(position).getCommenterInfo().getUserProfileUrl()).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        }
        holder.commenterProfile.setImageBitmap(commenterProfile);
        holder.commenterName.setText(commentList.get(position).getCommenterInfo().getUserName());
        //상대 닉네임을 클릭했을 때
        holder.commenterName.setOnClickListener(v -> {
            Intent showProfilePage = new Intent(context, MyPageActivity.class);
            Bundle sppBundle = new Bundle();
                sppBundle.putInt("userID", commentList.get(position).getCommenterInfo().getUserID());
            showProfilePage.putExtras(sppBundle);
            context.startActivity(showProfilePage);
        });
        holder.commenterTown.setText(commentList.get(position).getCommenterInfo().getUserTown());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        try {
            Date date = sdf.parse(commentList.get(position).getCommentTime());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String dateString = dateFormat.format(Objects.requireNonNull(date));
            holder.commentTime.setText(dateString);
        } catch (ParseException e) { e.printStackTrace(); }
        holder.comment.setText(commentList.get(position).getContent());
            Spannable span = (Spannable) holder.comment.getText();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //멘션 닉네임을 눌렀을 때 가능한 이벤트
                    Intent showProfilePage = new Intent(context, MyPageActivity.class);
                    Bundle sppBundle = new Bundle();
                        sppBundle.putInt("userID", commentList.get(position).getCommenterInfo().getUserID());
                    showProfilePage.putExtras(sppBundle);
                    context.startActivity(showProfilePage);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#1b73d8"));
                    ds.setUnderlineText(false);
                }
            };
            int i = 0;
            String content = holder.comment.getText().toString();
            while (i < content.length()) {
                int indexMentionStart = content.indexOf("@", i);

                if (indexMentionStart != -1) {
                    int indexMentionEnd = content.indexOf(" ", indexMentionStart);

                    if (indexMentionEnd != -1) {
                        span.setSpan(clickableSpan, indexMentionStart, indexMentionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        i = indexMentionEnd+1;
                    }
                    else
                        break;
                }
                else
                    break;
            }
            holder.comment.setMovementMethod(LinkMovementMethod.getInstance());

        if (myInfo.getUserName().equals(commentList.get(position).getCommenterInfo().getUserName())){
            holder.commentDelete.setVisibility(View.VISIBLE);
            holder.commentDelete.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("댓글 삭제").setMessage("이 댓글을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", (dialog, which) -> {
                    //TODO: DB에서 댓글을 삭제
                    commentList.remove(position);
                    notifyDataSetChanged();
                });
                builder.setNegativeButton("취소", (dialog, id) -> {
                    dialog.cancel();    //삭제가 되지 않음
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });
        }
        else
            holder.commentDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commenterProfile;
        TextView commenterName, commenterTown, commentTime, comment, commentDelete;

        CommentViewHolder(final View itemView) {
            super(itemView);
            commenterProfile = itemView.findViewById(R.id.commentrow_profile);
            commenterName = itemView.findViewById(R.id.commentrow_name);
            commenterTown = itemView.findViewById(R.id.commentrow_town);
            commentTime = itemView.findViewById(R.id.commentrow_time);
            comment = itemView.findViewById(R.id.commentrow_comment);
            commentDelete = itemView.findViewById(R.id.commentrow_delete);
        }
    }
}
