package com.example.howzy.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howzy.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder
{
    public TextView senderMsgText, receiverMsgText;
    public CircleImageView receiverProfileImage;
    public ImageView messageSenderPicture, messageReceiverPicture;

    public MessageViewHolder(@NonNull View itemView)
    {
        super(itemView);

        senderMsgText = itemView.findViewById(R.id.sender_message_text);
        receiverMsgText = itemView.findViewById(R.id.receiver_message_text);
        receiverProfileImage = itemView.findViewById(R.id.message_profile_img);
        messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
    }
}
