package com.tave7.dobdob;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostPhotosPagerAdapter extends RecyclerView.Adapter<PostPhotosPagerAdapter.PhotosViewHolder> {
    private Context context;
    private ArrayList<Bitmap> photoList = null;     //photo의 url이 변환된 Bitmap형태로 저장돼있음
    private boolean isEditable = false;

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

        if (isEditable)
            holder.tvPhotoDelete.setVisibility(View.VISIBLE);
        else
            holder.tvPhotoDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return photoList.size(); }

    public void changeIsEditable() {
        isEditable = !isEditable;
    }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhotoDelete;
        ImageView ivPhoto;

        PhotosViewHolder(final View itemView) {
            super(itemView);
            //TODO: 권한이 있고 수정상태여야 삭제 텍스트가 보임
            tvPhotoDelete = itemView.findViewById(R.id.tvPhotoDelete);
            tvPhotoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("사진 삭제").setMessage("선택한 사진을 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO: DB에서 사진을 삭제한 것을 반영할 수 있게 해야 함
                            int pos = getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                photoList.remove(pos);
                                notifyDataSetChanged();
                            }
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
                }
            });

            ivPhoto = itemView.findViewById(R.id.ivPostPhoto);
            ivPhoto.setOnClickListener(new View.OnClickListener() {        //사진을 클릭했을 시
                @Override
                public void onClick(View v) {
                    //다이얼로그로 원본 사진을 보여줌
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.item_photo_pager);
                    TextView photoDelete = dialog.findViewById(R.id.tvPhotoDelete);
                        photoDelete.setVisibility(View.GONE);
                    ImageView photo = dialog.findViewById(R.id.ivPostPhoto);
                        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                        photo.setLayoutParams(param);
                        photo.setImageDrawable(ivPhoto.getDrawable());
                        photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    dialog.show();
                }
            });
        }
    }
}
