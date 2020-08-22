package com.example.howzy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int galleryPic = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadingBar =new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                updateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);

            }
        });
    }

    private void RetrieveUserInfo()
    {
        rootRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")))
                {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    userName.setText(retrieveUserName);

                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    userStatus.setText(retrieveStatus);

                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                }
                else if(dataSnapshot.exists() && (dataSnapshot.hasChild("name")))
                {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    userName.setText(retrieveUserName);

                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    userStatus.setText(retrieveStatus);
                }
                else
                {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "please set and update your profile info..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPic && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).
                    setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if(resultCode == RESULT_OK)
                {
                    loadingBar.setTitle("Set Profile Image");
                    loadingBar.setMessage("please wait , your profile picture is updating");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    Uri resultUri = result.getUri();

                    StorageReference filepath = userProfileImageRef.child(currentUserId + ".jpg");
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                                final String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();

                                rootRef.child(currentUserId).child("image")
                                        .setValue(downloadUri)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Image Save In database Successfully ", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Error:"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            else
                            {
                                loadingBar.dismiss();
                                String msg = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:"+msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }


    }

    private void initializeFields()
    {
        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        updateAccountSettings = findViewById(R.id.update_settings_btn);
        userName = findViewById(R.id.set_profile_username);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
    }

    private void updateSettings()
    {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "please enter the User name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setUserStatus))
        {
            Toast.makeText(this, "please enter your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);

            rootRef.child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    }
                    else
                    {
                        String msg = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error:"+msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void SendUserToMainActivity()
    {
            Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
    }



}
