package com.example.howzy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView myChatList;
    private String friendUserId, friendUserName, friendImage, currentUserId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMsgBrn, sendFilesBtn;
    private EditText inputMsgText;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private messageAdapter messageAdapter;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUri="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(currentUserId).child(friendUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                // new message first
                myChatList.smoothScrollToPosition(myChatList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendUserId = getIntent().getExtras().get("friend_user_id").toString();
        friendUserName = getIntent().getExtras().get("friend_name").toString();
        friendImage = getIntent().getExtras().get("friend_image").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        userName.setText(friendUserName);
        Picasso.get().load(friendImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMsgBrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();

            }
        });

        sendFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[] = new CharSequence[]
                        {

                                "Image",
                                "PDF Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which == 0)
                        {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 430);
                        }
                        if(which == 1)
                        {
                            checker = "pfd";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf*");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 430);
                        }
                        if(which == 2)
                        {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword*");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 430);
                        }
                    }
                });
                builder.show();
            }
        });

    }



    private void initializeFields()
    {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        inputMsgText = findViewById(R.id.input_private_chat_msg);
        sendMsgBrn = findViewById(R.id.private_chat_send_msg_btn);
        sendFilesBtn = findViewById(R.id.send_files_btn);

        messageAdapter = new messageAdapter(messagesList);
        myChatList = findViewById(R.id.private_chat_list);
        linearLayoutManager = new LinearLayoutManager(this);
        myChatList.setLayoutManager(linearLayoutManager);
        myChatList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(ChatActivity.this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy" );
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a" );
        saveCurrentTime = currentTime.format(calendar.getTime());

        displayLastSeen();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 430 && resultCode==RESULT_OK && data.getData()!=null)
        {
            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if(!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + currentUserId + "/" + friendUserId;
                final String messageReceiverRef = "Messages/" + friendUserId + "/" + currentUserId;

                final DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(currentUserId).child(friendUserId).push();

                final String msgPushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushId + "."+ checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Map<String, Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("message", task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from", currentUserId);
                            messageTextBody.put("to",  friendUserId);
                            messageTextBody.put("messageID", msgPushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushId , messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId , messageTextBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p +" % Uploading..." );

                    }
                });


            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + currentUserId + "/" + friendUserId;
                final String messageReceiverRef = "Messages/" + friendUserId + "/" + currentUserId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(currentUserId).child(friendUserId).push();

                final String msgPushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushId + "."+ "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if(task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            myUri  =downloadUri.toString();


                            Map<String, Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("message",myUri);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from", currentUserId);
                            messageTextBody.put("to",  friendUserId);
                            messageTextBody.put("messageID", msgPushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushId , messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId , messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error:"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    inputMsgText.setText("");

                                }
                            });

                        }

                    }
                });
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "please, select an item", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void displayLastSeen()
    {
        rootRef.child("Users").child(friendUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("userState").hasChild("state"))
                {
                    final String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    final String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online"))
                    {
                        userLastSeen.setText("Online");

                    }
                    else if(state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen: "+ date + " "+time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String msgText = inputMsgText.getText().toString();

        if(TextUtils.isEmpty(msgText))
        {
            Toast.makeText(this, "Please, write the msg", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + currentUserId + "/" + friendUserId;
            String messageReceiverRef = "Messages/" + friendUserId + "/" + currentUserId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(currentUserId).child(friendUserId).push();

            String msgPushId = userMessageKeyRef.getKey();

            Map<String, Object> messageTextBody = new HashMap<>();
            messageTextBody.put("message",msgText);
            messageTextBody.put("type","text");
            messageTextBody.put("from", currentUserId);
            messageTextBody.put("to",  friendUserId);
            messageTextBody.put("messageID", msgPushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderRef + "/" + msgPushId , messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error:"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                    inputMsgText.setText("");

                }
            });

        }
    }
}

//Jp9573@gmail.com