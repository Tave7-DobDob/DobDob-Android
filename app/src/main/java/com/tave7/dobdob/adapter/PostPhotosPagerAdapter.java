package com.tave7.dobdob.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.R;

import java.util.ArrayList;

public class PostPhotosPagerAdapter extends RecyclerView.Adapter<PostPhotosPagerAdapter.PhotosViewHolder> {
    private Context context;
    private ArrayList<Bitmap> photoList;

    public PostPhotosPagerAdapter(ArrayList<Bitmap> photoList) {
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
    public void onBindViewHolder(@NonNull PostPhotosPagerAdapter.PhotosViewHolder holder, int position) {
        if (photoList.get(position) == null) {
            holder.tvLoadFail.setVisibility(View.VISIBLE);
            holder.ivPhoto.setVisibility(View.GONE);
        }
        else {
            holder.tvLoadFail.setVisibility(View.GONE);
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivPhoto.setImageBitmap(photoList.get(position));
        }
    }

    @Override
    public int getItemCount() { return photoList.size(); }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoadFail;
        ImageView ivPhoto;

        PhotosViewHolder(final View itemView) {
            super(itemView);

            tvLoadFail = itemView.findViewById(R.id.tvLoadFail);
            ivPhoto = itemView.findViewById(R.id.ivPostPhoto);
            ivPhoto.setOnClickListener(v -> {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_photo_pager);
                ImageView photo = dialog.findViewById(R.id.ivPostPhoto);
                    FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    photo.setLayoutParams(param);
                    photo.setImageDrawable(ivPhoto.getDrawable());
                    photo.setOnClickListener(v1 -> dialog.dismiss());
                dialog.show();
            });
        }
    }
}
