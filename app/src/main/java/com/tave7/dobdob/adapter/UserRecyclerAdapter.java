package com.tave7.dobdob.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tave7.dobdob.MyPageActivity;
import com.tave7.dobdob.R;
import com.tave7.dobdob.data.UserInfo;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tave7.dobdob.MainActivity.myInfo;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UsersViewHolder> {
    private Context context;
    private ArrayList<UserInfo> userList;

    public UserRecyclerAdapter(ArrayList<UserInfo> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserRecyclerAdapter.UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_likeuser, parent, false);
        UserRecyclerAdapter.UsersViewHolder viewHolder = new UserRecyclerAdapter.UsersViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerAdapter.UsersViewHolder holder, int position) {
        if (userList.get(position).getUserProfileBM() == null)
            holder.civProfile.setImageResource(R.drawable.user);
        else
            holder.civProfile.setImageBitmap(userList.get(position).getUserProfileBM());
        holder.tvName.setText(userList.get(position).getUserName());
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civProfile;
        TextView tvName;

        UsersViewHolder(final View itemView) {
            super(itemView);

            civProfile = itemView.findViewById(R.id.likeUserRow_profile);
            tvName = itemView.findViewById(R.id.likeUserRow_name);
            tvName.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Intent showProfilePage = new Intent(context, MyPageActivity.class);
                    Bundle sppBundle = new Bundle();
                    if (myInfo.getUserID() != userList.get(pos).getUserID()) {
                        sppBundle.putInt("userID", userList.get(pos).getUserID());
                        sppBundle.putString("userName", userList.get(pos).getUserName());
                    }
                    showProfilePage.putExtras(sppBundle);
                    context.startActivity(showProfilePage);
                }
            });
        }
    }
}
