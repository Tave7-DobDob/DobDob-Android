package com.tave7.dobdob;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentViewHolder> {
    private Context context;
    private ArrayList<CommentInfo> commentList = null;

    public CommentRecyclerAdapter(ArrayList<CommentInfo> commentList) { this.commentList = commentList; }

    @Override
    public CommentRecyclerAdapter.CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.commentrow, parent, false);
        CommentRecyclerAdapter.CommentViewHolder viewHolder = new CommentRecyclerAdapter.CommentViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentRecyclerAdapter.CommentViewHolder holder, int position) {
        //holder.commenterProfile.setImageURI("");     //TODO: 이미지 URL을 보이게 함
        holder.commenterName.setText(commentList.get(position).getCommenterName());
        holder.commenterTown.setText(commentList.get(position).getCommenterTown());
        holder.commentTime.setText(commentList.get(position).getCommentTime());
        holder.comment.setText(commentList.get(position).getMention()+"  "+commentList.get(position).getContent());
            //TODO: 멘션에 사람이 여러명이라면 for문을 통해 각각 다른 onClick의 이벤트를 수행해야 함
            Spannable span = (Spannable) holder.comment.getText();
            span.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //TODO: 멘션 닉네임을 눌렀을 때 가능한 이벤트
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#5C1DB5"));
                    ds.setUnderlineText(false);
                }
            }, 0, commentList.get(position).getMention().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //mention 길이까지만 클릭가능
            holder.comment.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commenterProfile;
        TextView commenterName, commenterTown, commentTime, comment;

        CommentViewHolder(final View itemView) {
            super(itemView);
            commenterProfile = itemView.findViewById(R.id.commentrow_profile);
            commenterName = itemView.findViewById(R.id.commentrow_name);
            commenterTown = itemView.findViewById(R.id.commentrow_town);
            commentTime = itemView.findViewById(R.id.commentrow_time);
            comment = itemView.findViewById(R.id.commentrow_comment);

            commenterName.setOnClickListener(new View.OnClickListener() {        //상대 닉네임을 클릭했을 때
                @Override
                public void onClick(View v) {
                    //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함
                }
            });
        }
    }
}
