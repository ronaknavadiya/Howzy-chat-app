package com.example.howzy.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howzy.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsViewHolder extends RecyclerView.ViewHolder
{
    public TextView userName, userStatus;
    public CircleImageView profileImage;

    public FindFriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        userName =itemView.findViewById(R.id.users_profile_name);
        profileImage =itemView.findViewById(R.id.users_profile_image);
        userStatus =itemView.findViewById(R.id.user_profile_status);

    }
}
