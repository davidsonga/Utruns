package com.example.utrun.Activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.utrun.Adapter.MessageAdapter;
import com.example.utrun.R;
import com.example.utrun.databinding.ActivityChatActBinding;
import com.example.utrun.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;


import android.Manifest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ChatAct extends AppCompatActivity {
    ActivityChatActBinding binding;
    String recieverId = "";
    private String pictureBase64 = "";
    String fullName = "";
    String senderRoom, recieverRoom = "";
    DatabaseReference databaseReferenceReciever;
    DatabaseReference databaseReferenceSender;
    DatabaseReference currentUserTyping;
    DatabaseReference userStateReference;
    MessageAdapter messageAdapter;
    private TextView profileName;
    private TextView status;

    private ImageView profilePicture;
    private boolean isTyping = false;
    private ImageView videoCallBtns;
    private Handler handler;



    private List<MessageModel> messageList = new ArrayList<>();





    @SuppressLint("WrongViewCast")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatActBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileName = findViewById(R.id.contactNameTextView);
        profilePicture = findViewById(R.id.contactProfileImageView);
        status = findViewById(R.id.contactStatusTextView);
        videoCallBtns = findViewById(R.id.videoCallBtn);

        //use putextra
        recieverId = getIntent().getStringExtra("id");
        pictureBase64 = getIntent().getStringExtra("pictureUrl");
        fullName = getIntent().getStringExtra("fullName");

        handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
        profileName.setText(fullName);
        //converting the string into a bitmap to allow our picture to show in imageview
        Bitmap bitmap = decodeBase64(pictureBase64);
        if (bitmap != null) {
            profilePicture.setImageBitmap(bitmap);

        } else {
            // Handle the case where the Base64 string couldn't be decoded
            Toast.makeText(this, "Error decoding the image", Toast.LENGTH_LONG).show();
        }
        //do not modify
        senderRoom = FirebaseAuth.getInstance().getUid() + recieverId;
        recieverRoom = recieverId + FirebaseAuth.getInstance().getUid();

        //messageAdapter to communicate with recyclerview
        messageAdapter = new MessageAdapter(this);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(messageAdapter);
        // Add the message to the adapter


        binding.recycler.setLayoutManager(new LinearLayoutManager(this));


        databaseReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        databaseReferenceReciever = FirebaseDatabase.getInstance().getReference("chats").child(recieverRoom);
        //databaseReferenceReciever = FirebaseDatabase.getInstance().getReference("chats").child(recieverRoom);
        userStateReference = FirebaseDatabase.getInstance().getReference("login").child("email").child(recieverId);
        currentUserTyping = FirebaseDatabase.getInstance().getReference("login").child("email").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        // Schedule the task to check for changes every second



        videoCallBtns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        binding.messageEd.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
                if (!isTyping) {
                    // User has started typing
                    isTyping = true;
                    currentUserTyping.child("typing").setValue(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
                if (s.length() == 0 && isTyping) {
                    // User has finished typing
                    isTyping = false;
                    currentUserTyping.child("typing").setValue(false);
                }
            }
        });








        databaseReferenceSender.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                messageAdapter.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messageList.add(messageModel);
                }


                messageList.sort(Comparator.comparingLong(MessageModel::getTimestamp));

                messageAdapter.clear(); // Clear the adapter before adding new messages

                for (MessageModel message : messageList) {
                    messageAdapter.add(message);
                }

                // Notify the RecyclerView of data changes
                messageAdapter.notifyDataSetChanged();

                // Scroll to the bottom
                binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error
            }
        });




        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.messageEd.getText().toString();

                if (message.trim().length() > 0) {
                    binding.messageEd.setText("");
                    senMessage(message);

                }
            }
        });

    }



    private void senMessage(String message) {
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis(); // Get current timestamp in milliseconds

        MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, timestamp );
        messageAdapter.add(messageModel);

        databaseReferenceSender
                .child(messageId)
                .setValue(messageModel);

        databaseReferenceReciever
                .child(messageId)
                .setValue(messageModel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE},100);
        }




        // Notify the receiver via FCM
       // sendFCMNotifications(message);

        // Clear the input field
        binding.messageEd.setText("");
        binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            userStateReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String userRole = snapshot.child("state").getValue(String.class);
                    boolean userTyping = Boolean.TRUE.equals(snapshot.child("typing").getValue(Boolean.class));

                    if (userTyping) {
                        status.setText("typing....");
                    } else {
                        status.setText("");

                        if ("Online".equalsIgnoreCase(userRole) && !"Online".equalsIgnoreCase(status.getText().toString())) {
                            status.setText("");
                            status.setText(userRole);
                        }
                        if (!"Online".equalsIgnoreCase(userRole)) {
                            status.setText("");
                            status.setText("Last seen " + userRole);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle onCancelled
                }
            });

            handler.postDelayed(this, 100);
        }
    };


    // Function to decode Base64 string to Bitmap
    private Bitmap decodeBase64(String base64String) {
        try {
            byte[] imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            // Handle decoding error, e.g., invalid Base64 string
            return null;
        }
    }
    private void sendFCMNotifications(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userReference = databaseReference.child("login").child("email").child(recieverId);


        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String  fcmToken =  dataSnapshot.child("FCMToken").getValue().toString() ;


                    // Create a data payload for the FCM message
                    RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder(fcmToken);
                    messageBuilder.addData("title", "New Message from " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    messageBuilder.addData("body", message);
                    messageBuilder.addData("click_action", "OPEN_CHAT_ACTIVITY"); // Specify an action to open the chat activity

                    // Send the FCM message
                    FirebaseMessaging.getInstance().send(messageBuilder.build());

                } else {
                    System.out.println("FCM Token does not exist for this user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error occurred while retrieving FCM Token: " + databaseError.getMessage());
            }
        });

    }





}