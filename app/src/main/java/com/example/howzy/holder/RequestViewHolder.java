package com.example.howzy.holder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howzy.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestViewHolder extends RecyclerView.ViewHolder
{
    public TextView userName, userStatus;
    public CircleImageView profileImage;
    public Button acceptBtn, cancelBtn;

    public RequestViewHolder(@NonNull View itemView)
    {
        super(itemView);

        userName =itemView.findViewById(R.id.users_profile_name);
        profileImage =itemView.findViewById(R.id.users_profile_image);
        userStatus =itemView.findViewById(R.id.user_profile_status);

        acceptBtn = itemView.findViewById(R.id.request_accept_btn);
        cancelBtn = itemView.findViewById(R.id.request_cancel_btn);
    }
}
