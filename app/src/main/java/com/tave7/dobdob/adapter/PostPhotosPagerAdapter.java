package com.tave7.dobdob.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.DownloadFileTask;
import com.tave7.dobdob.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PostPhotosPagerAdapter extends RecyclerView.Adapter<PostPhotosPagerAdapter.PhotosViewHolder> {
    private Context context;
    private ArrayList<String> photoList;

    public PostPhotosPagerAdapter(ArrayList<String> photoList) {
        this.photoList = photoList;
    }

    public void changePhotoList(ArrayList<String> photoList) {
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
        Bitmap photo = null;
        try {
            photo = new DownloadFileTask(photoList.get(position)).execute().get();
        } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        holder.ivPhoto.setImageBitmap(photo);
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
