package com.example.utrun.Activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.utrun.Adapter.MessageAdapter;
import com.example.utrun.R;
import com.example.utrun.databinding.ActivityChatActBinding;
import com.example.utrun.models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
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
    private Handler handler;
    List<MessageModel> messageList = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatActBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileName = findViewById(R.id.contactNameTextView);
        profilePicture = findViewById(R.id.contactProfileImageView);
        status = findViewById(R.id.contactStatusTextView);

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
        binding.recycler.setAdapter(messageAdapter);
        // Add the message to the adapter


        binding.recycler.setLayoutManager(new LinearLayoutManager(this));


        databaseReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        databaseReferenceReciever = FirebaseDatabase.getInstance().getReference("chats").child(recieverRoom);
        //databaseReferenceReciever = FirebaseDatabase.getInstance().getReference("chats").child(recieverRoom);
        userStateReference = FirebaseDatabase.getInstance().getReference("login").child("email").child(recieverId);
        currentUserTyping = FirebaseDatabase.getInstance().getReference("login").child("email").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        // Schedule the task to check for changes every second



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
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the adapter and add the sorted messages
                messageList.clear(); // Clear the list before adding new messages

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messageList.add(messageModel);
                }

                // Sort the messages by timestamp
                Collections.sort(messageList, new Comparator<MessageModel>() {
                    @Override
                    public int compare(MessageModel o1, MessageModel o2) {
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                });

                messageAdapter.clear(); // Clear the adapter before adding new messages

                for (MessageModel messageModel : messageList) {
                    messageAdapter.add(messageModel);
                }

                // Notify the RecyclerView of data changes
                messageAdapter.notifyDataSetChanged();

                // Scroll to the bottom
                binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
       // binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
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


}