package com.tave7.dobdob;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostPhotosPagerAdapter extends RecyclerView.Adapter<PostPhotosPagerAdapter.PhotosViewHolder> {
    private Context context;
    private ArrayList<Bitmap> photoList = null;     //photo의 url이 변환된 Bitmap형태로 저장돼있음

    public PostPhotosPagerAdapter(ArrayList<Bitmap> photoList) { this.photoList = photoList; }

    @Override
    public PostPhotosPagerAdapter.PhotosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_photo_pager, parent, false);
        PostPhotosPagerAdapter.PhotosViewHolder viewHolder = new PostPhotosPagerAdapter.PhotosViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PostPhotosPagerAdapter.PhotosViewHolder holder, int position) {
        holder.ivPhoto.setImageBitmap(photoList.get(position));     //TODO: 이미지를 보이게 함
    }

    @Override
    public int getItemCount() { return photoList.size(); }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        PhotosViewHolder(final View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPostPhoto);

            ivPhoto.setOnClickListener(new View.OnClickListener() {        //사진을 클릭했을 시
                @Override
                public void onClick(View v) {
                    //다이얼로그로 원본 사진을 보여줌
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.item_photo_pager);
                    ImageView photo = dialog.findViewById(R.id.ivPostPhoto);
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        photo.setLayoutParams(param);
                        photo.setImageDrawable(ivPhoto.getDrawable());
                        photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    dialog.show();

                    //TODO: 만약에 작성자라면 삭제가 가능하게 해야 함
                }
            });
        }
    }
}
