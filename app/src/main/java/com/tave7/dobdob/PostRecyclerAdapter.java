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
    private boolean isMain = true;
    private ArrayList<PostInfo> postList = null;
    private ArrayList<PostInfo> totalPostList = null;    //메인에서 보여줄 postList의 복사본

    public PostRecyclerAdapter(ArrayList<PostInfo> postList) {      //MyPageActivity에서 호출
        isMain = false;        //태그 클릭 시 태그에 대한 게시물 검색이 안됨
        this.postList = postList; 
    }

    public PostRecyclerAdapter(ArrayList<PostInfo> postList, ArrayList<PostInfo> totalPostList) {   //MainActivity에서 호출
        this.postList = postList;
        this.totalPostList = totalPostList;
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

                tvTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMain) {
                            String searchTag = tvTag.getText().toString().substring(1, tvTag.getText().length()-1);

                            Intent showContainTagPost = new Intent(context, TagPostActivity.class);
                            showContainTagPost.putExtra("tagName", searchTag);
                            showContainTagPost.putExtra("tagPostList", searchTagPost(searchTag));
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

    public ArrayList<PostInfo> searchTagPost(String searchText) {     //태그가 포함된 검색
        ArrayList<PostInfo> tmpTagPost = new ArrayList<>();

        for (PostInfo pi : totalPostList) {
            for (String tag : pi.getPostTag()) {
                if (tag.equals(searchText))
                    tmpTagPost.add(pi);
            }
        }

        return tmpTagPost;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView writerProfile;      //TODO: 사진 받아와서 이 사진으로 설정함
        TextView writerName, postTime, postTitle, heartNum, commentNum;
        LinearLayout tags;
        
        PostViewHolder(final View itemView) {
            super(itemView);
            writerProfile = (CircleImageView) itemView.findViewById(R.id.postrow_profile);
            writerName = (TextView) itemView.findViewById(R.id.postrow_name);
            postTime = (TextView) itemView.findViewById(R.id.postrow_time);
            postTitle = (TextView) itemView.findViewById(R.id.postrow_title);
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
                        context.startActivity(intent);    //해당 글 창으로 넘어감
                    }
                }
            });
        }
    }
}
