package com.example.howzy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receivedUserId,SenderUserId, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestBtn, declineRequestBtn;
    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receivedUserId  = getIntent().getExtras().get("visited_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        SenderUserId = mAuth.getCurrentUser().getUid();

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestBtn = findViewById(R.id.send_msg_request_btn);
        declineRequestBtn = findViewById(R.id.decline_send_msg_request_btn);

        Current_State = "new";
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        userRef.child(receivedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
               else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequest()
    {
        chatRequestRef.child(SenderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(receivedUserId))
                {
                    String request_type = dataSnapshot.child(receivedUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        Current_State = "request_sent";
                        sendMessageRequestBtn.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("received"))
                    {
                        Current_State = "request_received";
                        sendMessageRequestBtn.setText("Accept Chat Request");
                        declineRequestBtn.setVisibility(View.VISIBLE);
                        declineRequestBtn.setEnabled(true);

                        declineRequestBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsRef.child(SenderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(receivedUserId))
                            {
                                Current_State = "friends";
                                sendMessageRequestBtn.setText("Remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!SenderUserId.equals(receivedUserId))
        {
            sendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    sendMessageRequestBtn.setEnabled(false);

                    if(Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if(Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if(Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if(Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });
        }
        else
        {
            sendMessageRequestBtn.setVisibility(View.INVISIBLE);
        }
    }


    private void SendChatRequest()
    {
        chatRequestRef.child(SenderUserId).child(receivedUserId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(receivedUserId).child(SenderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        HashMap<String , String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from", SenderUserId);
                                        chatNotificationMap.put("type", "request");
                                        notificationRef.child(receivedUserId).push()
                                                .setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    sendMessageRequestBtn.setEnabled(true);
                                                    Current_State = "request_sent";
                                                    sendMessageRequestBtn.setText("Cancel Chat Request");
                                                }

                                            }
                                        });
                                    }

                                }
                            });
                }

            }
        });
    }

    private void CancelChatRequest()
    {
        chatRequestRef.child(SenderUserId).child(receivedUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(receivedUserId).child(SenderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestBtn.setEnabled(true);
                                Current_State = "new";
                                sendMessageRequestBtn.setText("Send Chat Request");

                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                declineRequestBtn.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }


    private void AcceptChatRequest()
    {
        contactsRef.child(SenderUserId).child(receivedUserId).child("contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(receivedUserId).child(SenderUserId).child("contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                chatRequestRef.child(SenderUserId).child(receivedUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receivedUserId).child(SenderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        sendMessageRequestBtn.setText("Remove Contact");
                                                                                        sendMessageRequestBtn.setEnabled(true);
                                                                                        Current_State = "friends";

                                                                                        declineRequestBtn.setVisibility(View.INVISIBLE);
                                                                                        declineRequestBtn.setEnabled(false);

                                                                                    }

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void RemoveSpecificContact()
    {
        contactsRef.child(SenderUserId).child(receivedUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsRef.child(receivedUserId).child(SenderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestBtn.setEnabled(true);
                                Current_State = "new";
                                sendMessageRequestBtn.setText("Send Chat Request");

                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                declineRequestBtn.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

}
