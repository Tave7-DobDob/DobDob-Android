package com.tave7.dobdob;

import android.content.Context;
import android.content.Intent;
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
        holder.comment.setText(commentList.get(position).getContent());
            Spannable span = (Spannable) holder.comment.getText();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //멘션 닉네임을 눌렀을 때 가능한 이벤트
                    //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함(해당 사용자의 UserInfo를 주어야 함) -> 만약 현재 닉네임을 클릭한 사람이 작성자라면 true로 Extra 전달
                    Intent showProfilePage = new Intent(context, MyPageActivity.class);
                    Bundle sppBundle = new Bundle();
                        sppBundle.putBoolean("isMyPage", false);
                    showProfilePage.putExtras(sppBundle);
                    //TODO: user의 닉네임을 DB에 전달해서 DB로부터 해당 userInfo와 user가 쓴 글을 받아와야 함
                    //context.startActivity(showProfilePage);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#5C1DB5"));
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
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commenterProfile;
        TextView commenterName, commenterTown, commentTime, comment;

        CommentViewHolder(final View itemView) {
            super(itemView);
            commenterProfile = (CircleImageView) itemView.findViewById(R.id.commentrow_profile);
            commenterName = (TextView) itemView.findViewById(R.id.commentrow_name);
            commenterTown = (TextView) itemView.findViewById(R.id.commentrow_town);
            commentTime = (TextView) itemView.findViewById(R.id.commentrow_time);
            comment = (TextView) itemView.findViewById(R.id.commentrow_comment);

            commenterName.setOnClickListener(new View.OnClickListener() {        //상대 닉네임을 클릭했을 때
                @Override
                public void onClick(View v) {
                    //TODO: 추후에 이 사람이 쓴 글을 볼 수 있게 함(해당 사용자의 UserInfo를 주어야 함) -> 만약 현재 닉네임을 클릭한 사람이 작성자라면 true로 Extra 전달
                    Intent showProfilePage = new Intent(context, MyPageActivity.class);
                    Bundle sppBundle = new Bundle();
                        sppBundle.putBoolean("isMyPage", false);
                    showProfilePage.putExtras(sppBundle);
                    //TODO: user의 닉네임을 DB에 전달해서 DB로부터 해당 userInfo와 user가 쓴 글을 받아와야 함
                    //context.startActivity(showProfilePage);
                }
            });
        }
    }
}
