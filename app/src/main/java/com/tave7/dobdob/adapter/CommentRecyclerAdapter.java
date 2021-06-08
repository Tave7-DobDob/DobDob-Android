package com.tave7.dobdob.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.R;
import com.tave7.dobdob.data.CommentInfo;
import com.tave7.dobdob.data.UserInfo;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentViewHolder> {
    private Context context;
    private UserInfo seeUserInfo;
    private ArrayList<CommentInfo> commentList;

    public CommentRecyclerAdapter(ArrayList<CommentInfo> commentList, UserInfo seeUserInfo) {
        this.commentList = commentList;
        this.seeUserInfo = seeUserInfo;
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

        if (seeUserInfo.getUserName().equals(commentList.get(position).getCommenterName())){
            holder.commentDelete.setVisibility(View.VISIBLE);
            holder.commentDelete.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("댓글 삭제").setMessage("이 댓글을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: DB에서 댓글을 삭제
                        commentList.remove(position);
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();    //삭제가 되지 않음
                    }
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
            commenterProfile = (CircleImageView) itemView.findViewById(R.id.commentrow_profile);
            commenterName = (TextView) itemView.findViewById(R.id.commentrow_name);
            commenterTown = (TextView) itemView.findViewById(R.id.commentrow_town);
            commentTime = (TextView) itemView.findViewById(R.id.commentrow_time);
            comment = (TextView) itemView.findViewById(R.id.commentrow_comment);
            commentDelete = (TextView) itemView.findViewById(R.id.commentrow_delete);

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
