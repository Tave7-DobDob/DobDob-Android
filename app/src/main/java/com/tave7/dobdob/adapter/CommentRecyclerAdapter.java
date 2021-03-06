package com.tave7.dobdob.adapter;

import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.LoginActivity;
import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.PostActivity;
import com.tave7.dobdob.PreferenceManager;
import com.tave7.dobdob.R;
import com.tave7.dobdob.RetrofitClient;
import com.tave7.dobdob.data.CommentInfo;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave7.dobdob.MainActivity.myInfo;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentViewHolder> {
    private Context context;
    private ArrayList<CommentInfo> commentList;

    public CommentRecyclerAdapter(ArrayList<CommentInfo> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentRecyclerAdapter.CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_comment, parent, false);
        CommentRecyclerAdapter.CommentViewHolder viewHolder = new CommentRecyclerAdapter.CommentViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentRecyclerAdapter.CommentViewHolder holder, int position) {
        if (commentList.get(position).getCommenterInfo().getUserProfileBM() == null)
            holder.commenterProfile.setImageResource(R.drawable.user);
        else
            holder.commenterProfile.setImageBitmap(commentList.get(position).getCommenterInfo().getUserProfileBM());
        holder.commenterName.setText(commentList.get(position).getCommenterInfo().getUserName());
        holder.commenterName.setOnClickListener(v -> {
            Intent showProfilePage = new Intent(context, MyPageActivity.class);
            Bundle sppBundle = new Bundle();
            if (myInfo.getUserID() != commentList.get(position).getCommenterInfo().getUserID()) {
                sppBundle.putInt("userID", commentList.get(position).getCommenterInfo().getUserID());
                sppBundle.putString("userName", commentList.get(position).getCommenterInfo().getUserName());
            }
            showProfilePage.putExtras(sppBundle);
            context.startActivity(showProfilePage);
        });
        holder.commenterTown.setText(commentList.get(position).getCommenterInfo().getUserTown());
        holder.commentTime.setText(commentList.get(position).getCommentTime());
        holder.comment.setText(commentList.get(position).getContent().replace(" ", "\u00A0"));
        //holder.comment.setText(commentList.get(position).getContent());
            Spannable span = (Spannable) holder.comment.getText();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //TODO: ?????? ???????????? ????????? ??? ????????? ?????????(????????? ??????????????? ??????!!!!)
                    /*
                    Intent showProfilePage = new Intent(context, MyPageActivity.class);
                    Bundle sppBundle = new Bundle();
                    if (myInfo.getUserID() != ) {
                        sppBundle.putInt("userID", );
                        sppBundle.putString("userName", );
                    }
                    showProfilePage.putExtras(sppBundle);
                    context.startActivity(showProfilePage);
                     */
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
                    int indexMentionEnd = content.indexOf("\u00A0", indexMentionStart);

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

        if (myInfo.getUserID() == commentList.get(position).getCommenterInfo().getUserID()) {
            holder.commentDelete.setVisibility(View.VISIBLE);
            holder.commentDelete.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("?????? ??????").setMessage("??? ????????? ?????????????????????????");
                builder.setPositiveButton("??????", (dialog, which) -> {
                    RetrofitClient.getApiService().deleteIDComment(PreferenceManager.getString(context, "jwt"), commentList.get(position).getCommentID()).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.code() == 200)
                                ((PostActivity) context).showPost(false);
                            else if (response.code() == 419) {
                                Toast.makeText(context, "????????? ????????? ????????????\n ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                PreferenceManager.removeKey(context, "jwt");
                                Intent reLogin = new Intent(context, LoginActivity.class);
                                reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(reLogin);
                                ((PostActivity) context).finish();
                            }
                            else
                                Toast.makeText(context, "?????? ?????? ????????? ??????????????????. ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(context, "?????? ?????? ????????? ??????????????????. ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                builder.setNegativeButton("??????", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(dialogInterface -> {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.yellow2));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.yellow2));
                });
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
