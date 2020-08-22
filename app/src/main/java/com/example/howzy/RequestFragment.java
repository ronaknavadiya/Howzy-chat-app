package com.example.howzy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.howzy.holder.RequestViewHolder;
import com.example.howzy.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Ref;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public RequestFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView = inflater.inflate(R.layout.fragment_request, container, false);

        myRequestList = requestView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");



        return requestView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserId),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                final String requestUserId = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received"))
                            {
                                usersRef.child(requestUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            String retrieveUserImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(retrieveUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }

                                        String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                        String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(retrieveUserName);
                                        holder.userStatus.setText("Wants to Connect with you..");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                Intent intent = new Intent(getContext(),ProfileActivity.class);
                                                intent.putExtra("visited_user_id",requestUserId);
                                                startActivity(intent);

                                            }
                                        });

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                contactsRef.child(currentUserId).child(requestUserId).child("Contact").setValue("saved")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    contactsRef.child(requestUserId).child(currentUserId).child("Contact").setValue("saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatRequestRef.child(currentUserId).child(requestUserId).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            chatRequestRef.child(requestUserId).child(currentUserId).removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                                        {
                                                                                                                            if(task.isSuccessful())
                                                                                                                            {
                                                                                                                                Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

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
                                        });

                                        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                chatRequestRef.child(currentUserId).child(requestUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(requestUserId).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(getContext(), "Request Canceled", Toast.LENGTH_SHORT).show();

                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });


                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");

                                Button request_cancel_btn = holder.itemView.findViewById(R.id.request_cancel_btn);
                                request_cancel_btn.setVisibility(View.INVISIBLE);

                                usersRef.child(requestUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            String retrieveUserImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(retrieveUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }

                                        String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                        String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(retrieveUserName);
                                        holder.userStatus.setText("You have send a request to "+ retrieveUserName);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                Intent intent = new Intent(getContext(),ProfileActivity.class);
                                                intent.putExtra("visited_user_id",requestUserId);
                                                startActivity(intent);

                                            }
                                        });

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                chatRequestRef.child(currentUserId).child(requestUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(requestUserId).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(getContext(), "Request Canceled", Toast.LENGTH_SHORT).show();

                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });


                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new RequestViewHolder(view);
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }
}
