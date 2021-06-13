package com.tave7.dobdob.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.R;

import java.util.ArrayList;

public class PostPhotosPagerAdapter extends RecyclerView.Adapter<PostPhotosPagerAdapter.PhotosViewHolder> {
    private Context context;
    private ArrayList<byte[]> photoList;     //photo의 url이 변환된 byte[]형태로 저장돼있음 -> TODO: 추후에 Uri를 받도록 변경해야 함!

    public PostPhotosPagerAdapter(ArrayList<byte[]> photoList) { this.photoList = photoList; }

    public void changePhotoList(ArrayList<byte[]> photoList) {
        this.photoList = photoList;
    }
    @NonNull
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
        //TODO: Uri를 Bitmap으로 변경해서 setImageBitmap을 해야 함!
        holder.ivPhoto.setImageBitmap(BitmapFactory.decodeByteArray(photoList.get(position), 0, photoList.get(position).length));
    }

    @Override
    public int getItemCount() { return photoList.size(); }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        PhotosViewHolder(final View itemView) {
            super(itemView);

            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPostPhoto);
            ivPhoto.setOnClickListener(v -> {   //사진을 클릭했을 시
                //다이얼로그로 원본 사진을 보여줌
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_photo_pager);
                ImageView photo = (ImageView) dialog.findViewById(R.id.ivPostPhoto);
                    FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    photo.setLayoutParams(param);
                    photo.setImageDrawable(ivPhoto.getDrawable());
                    photo.setOnClickListener(v1 -> dialog.dismiss());
                dialog.show();
            });
        }
    }
}
