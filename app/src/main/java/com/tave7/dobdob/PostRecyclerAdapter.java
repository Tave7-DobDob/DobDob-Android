package com.tave7.dobdob;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {
    private Context context;
    private ArrayList<PostInfo> postList = null;
    private int selected_pos = -1;

    public PostRecyclerAdapter(ArrayList<PostInfo> postList) {
        this.postList = postList;
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
        holder.heartNum.setText(String.valueOf(postList.get(position).getHeartNum()));
        holder.commentNum.setText(String.valueOf(postList.get(position).getCommentNum()));

        //리니어레이아웃에 태그 추가함
        for (String tagName : postList.get(position).getPostTag()){
            TextView tvTag = new TextView(context);
            tvTag.setText("#"+tagName+" ");
            tvTag.setTypeface(null, Typeface.BOLD);
            tvTag.setTextColor(Color.parseColor("#5AAEFF"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTag.setLayoutParams(layoutParams);
            holder.tags.addView(tvTag);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView writerProfile;      //TODO: 사진 받아와서 이 사진으로 설정함
        TextView writerName, postTime, postTitle, heartNum, commentNum;
        LinearLayout tags;
        
        PostViewHolder(final View itemView) {
            super(itemView);
            writerProfile = itemView.findViewById(R.id.postrow_profile);
            writerName = itemView.findViewById(R.id.postrow_name);
            postTime = itemView.findViewById(R.id.postrow_time);
            postTitle = itemView.findViewById(R.id.postrow_title);
            heartNum = itemView.findViewById(R.id.postrow_heartNum);
            commentNum = itemView.findViewById(R.id.postrow_commentNum);
            tags = itemView.findViewById(R.id.postrow_LinearTag);

            itemView.setOnClickListener(new View.OnClickListener() {        //항목을 클릭했을 때
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        selected_pos = pos;

                        //TODO: 선택한 post의 세부 내용을 다른 화면에 보여줌(Bundle로 position이랑 어떤 글인지 넘겨줘야 함)
                        Intent intent = new Intent(context, PostActivity.class);
                        context.startActivity(intent);    //해당 글 창으로 넘어감
                    }
                }
            });
        }
    }
}
