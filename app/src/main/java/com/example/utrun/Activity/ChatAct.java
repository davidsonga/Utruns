package com.example.utrun.Activity;

import static com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utrun.MyFirebaseMessagingService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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


import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;

import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;



public class ChatAct extends AppCompatActivity  {
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


    private List<MessageModel> messageList = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private List<String> list2 = new ArrayList<>();
    private List<String> listID = new ArrayList<>();
    private List<Boolean> boolList = new ArrayList<>();


    private boolean isOnline = false;
    private boolean isReceiverReadMessage = false;
    private String currentUserUID;
    private   Timer timer ;
    private   Timer timer2 ;
    private   int timerValue = 1000;
    private String userRole ="";
    private boolean shouldContinueRunnable = true;
    private PubNub pubnub = null;
    private PNConfiguration pnConfiguration = null;

    @SuppressLint("WrongViewCast")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatActBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileName = findViewById(R.id.contactNameTextView);
        profilePicture = findViewById(R.id.contactProfileImageView);
        status = findViewById(R.id.contactStatusTextView);
       SharedPreferences sharedPref = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        pictureBase64 = sharedPref.getString("key", null);


        //use putextra
        recieverId = getIntent().getStringExtra("id");

        //pictureBase64 = getIntent().getStringExtra("pictureUrl");
        fullName = getIntent().getStringExtra("fullName");

        handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
        profileName.setText(fullName);





        /*profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatAct.this, "Good", Toast.LENGTH_LONG).show();
                String serverKey = "AAAACY_0tNc:APA91bEqu9sIiw_9bpBenkX42eqILFFeIgZK-RSCryRkwsK7ssc11XA3bFpZBiklFOTU00tQcwZu4K1wGiLGXfXzWWP8bjTFPzJOZaTrooms0obqJxR3ix8IXBJG8anEoCBY3U0YOhZD";
                String fcmToken = "ebXqYeqRTaWx8tD38zrL2s:APA91bH-N5SYMfP0FwthAOWsSDZrcjyaO2F7eMTzZOgxRcZkSxizt3M1CQGghDxnpyoyNO-YRdCTxUw_pDCkbgsAOTIXOZbPeKp-Bm9HWULhkCoJB0NaLE-ciWmBwklYqrdy0Poy5xLn";

                try {
                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=" + serverKey);

                    JSONObject json = new JSONObject();
                    json.put("to", fcmToken);
                    JSONObject data = new JSONObject();
                    data.put("token", "eBhXGSlCQROgxVqwcZDcOn:APA91bEJK3lOx3zAAMGlrMU2dOYQEqndhLgome7-kYCLMMNs71ogCgLBF0BojJGzTACXJO0Stui8oQLcxpQhtXDBBFTt_lumoXv8PMfrk1QvRAxlVlSg7TncWsF4OmnZ9J39ZokL-HKS");
                    data.put("message", "dvdvdvdvdvvdvdvdvdvvdvdvd");
                    json.put("data", data);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        Toast.makeText(ChatAct.this, "Message sent successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChatAct.this, "Error sending message", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/



        timer = new Timer();
        TimerTask periodicCheck = new TimerTask() {
            @Override
            public void run() {


                // Check and publish items from the list
                for (int i = 0; i < list.size(); i++) {
                    publish(list.get(i));
                }
                list.clear();  // Clear the list after processing

            }
        };

// Schedule the periodicCheck task to run every 1 second (1000 milliseconds)
        timer.schedule(periodicCheck, 0, timerValue);




        timer2 = new Timer();
        TimerTask periodicChecks = new TimerTask() {
            @Override
            public void run() {

                if(status.getText().toString().equalsIgnoreCase("Online") || status.getText().toString().equalsIgnoreCase("typing....")){
                    for(int i =0; i<list2.size(); i++){
                        messageArivedOrNot(list2.get(i), i);
                    }
                    list2.clear();
                    boolList.clear();
                    listID.clear();
                }

            }
        };

// Schedule the periodicCheck task to run every 1 second (1000 milliseconds)
        timer2.schedule(periodicChecks, 0, timerValue);






        //converting the string into a bitmap to allow our picture to show in imageview
        Bitmap bitmap = decodeBase64(pictureBase64);
        if (bitmap != null) {
            profilePicture.setImageBitmap(bitmap);

        } else {
            // Handle the case where the Base64 string couldn't be decoded
            Toast.makeText(this, "Error decoding the image", Toast.LENGTH_LONG).show();
        }
        currentUserUID = FirebaseAuth.getInstance().getUid();
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


        //message event read is true
        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(true);


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
                //clean the list to avoid duplication
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                    boolean isTrue =false;
                    isTrue=  isReceiverReadMessage;
                    // String receiverReadMessage =  dataSnapshot.child(messageModel.getMsgId()).child("receiverReadMessage").getValue().toString() ;

                    if(messageModel != null){

                        if(!messageModel.getCurrentReadMessage()){
                            boolList.add(messageModel.getCurrentReadMessage());
                            list2.add(messageModel.getMsgId());
                            listID.add(messageModel.getSenderId());
                        }

                        list.add(messageModel.getMsgId());
                        messageList.add(messageModel);

                    }





                }



                // Sort the messages by timestamp
                Collections.sort(messageList, (o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));

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

// Set the time zone to South Africa
        TimeZone tz = TimeZone.getTimeZone("Africa/Johannesburg");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(tz);

// Get the current timestamp in South Africa
        String timestampString = sdf.format(new Date());
        long timestamp;

        try {
            Date date = sdf.parse(timestampString);
            timestamp = date.getTime(); // Get the timestamp in milliseconds
        } catch (ParseException e) {
            // e.printStackTrace();
            timestamp = 0L; // Handle the error as needed
        }


        MessageModel messageModel;
        if(status.getText().toString().equalsIgnoreCase("Online") || status.getText().toString().equalsIgnoreCase("typing....")){
            messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, timestamp, "No", true);
        }else{
            messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, timestamp, "No", false);
        }




        messageAdapter.add(messageModel);
        databaseReferenceSender
                .child(messageId)
                .setValue(messageModel);
        databaseReferenceReciever
                .child(messageId)
                .setValue(messageModel);



     /*   String user2FcmToken = "eBhXGSlCQROgxVqwcZDcOn:APA91bEJK3lOx3zAAMGlrMU2dOYQEqndhLgome7-kYCLMMNs71ogCgLBF0BojJGzTACXJO0Stui8oQLcxpQhtXDBBFTt_lumoXv8PMfrk1QvRAxlVlSg7TncWsF4OmnZ9J39ZokL-HKS"; // Replace with User 2's actual FCM token

// Create an intent that will be triggered when the notification is tapped
        Intent intent = new Intent(getApplicationContext(), HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

// Build the notification
       NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.edit_profile)
                .setContentTitle("New message from")
                .setContentText(message)
                .setAutoCancel(true) // Auto-cancel the notification when tapped
                .setContentIntent(pendingIntent);

// Get the NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// Notify with a specific tag and ID
        notificationManager.notify("YourNotificationTag", 0, mbuilder.build());*/

      /*  NotificationCompat.Builder mbuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.edit_profile,10)

                        .setContentTitle("New message from" )
                        .setContentText(message);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,mbuilder.build());*/

        // Notify the receiver via FCM
        //   sendFCMNotification(message);

        // Clear the input field
        binding.messageEd.setText("");
        binding.recycler.scrollToPosition(messageAdapter.getItemCount() - 1);
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {




            DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                    .child("isMessageRead")
                    .child(recieverRoom)
                    .child("isRead");

// Add a ValueEventListener to read the value
            isMessageReadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Read the value and store it in a boolean variable
                        isReceiverReadMessage = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));



                    } else {
                        // isReceiverReadMessage =false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that occur while reading the data

                }
            });


            userStateReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    userRole = snapshot.child("state").getValue(String.class);
                    boolean userTyping = Boolean.TRUE.equals(snapshot.child("typing").getValue(Boolean.class));

                    if (userTyping) {
                        status.setText("typing....");
                        isOnline=true;
                    } else {
                        status.setText("");

                        if ("Online".equalsIgnoreCase(userRole) && !"Online".equalsIgnoreCase(status.getText().toString())) {
                            status.setText("");
                            status.setText(userRole);
                            isOnline=true;
                        }
                        if (!"Online".equalsIgnoreCase(userRole)) {
                            status.setText("");
                            status.setText("Last seen " + userRole);
                            isOnline =false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle onCancelled
                }
            });

            if (shouldContinueRunnable) {
                handler.postDelayed(this, timerValue);
            }

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
    private void sendFCMNotification(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userReference = databaseReference.child("login").child("email").child(recieverId);


        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String  fcmToken =  dataSnapshot.child("FCMToken").getValue().toString() ;


                } else {
                    System.out.println("FCM Token does not exist for this user.");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error occurred while retrieving FCM Token: " + databaseError.getMessage());
            }
        });

        // Replace "recipientFCMToken" with the actual FCM token of the recipient
        String recipientFCMToken = "d7ME_H_jQjC5JwRlWVuVpl:APA91bFEM2rPJAFYfQipV0TSOFNsWR2y5CZj9UgQG8VwYRDFLmT3PQvw_-K8WGhAPzfsbUPP3GCjX4yUjQEIul8u5cwNjxfKKSGD3c7wehoNzk6DgK4jZy-B6tY4Fvc55vxUDbOAr-Y3";

        // Create a data payload for the FCM message
        RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder(recipientFCMToken);
        messageBuilder.addData("title", "New Message from " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        messageBuilder.addData("body", message);
        messageBuilder.addData("click_action", "OPEN_CHAT_ACTIVITY"); // Specify an action to open the chat activity

        // Send the FCM message
        FirebaseMessaging.getInstance().send(messageBuilder.build());

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        timerValue =1000;

        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(false);
    }


    private void publish(String v){

        if(isReceiverReadMessage){


            DatabaseReference databaseSenderReadMessage = FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child(v )

                    .child("receiverReadMessage");

            DatabaseReference databaseReceiverReadMessage = FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(recieverRoom)
                    .child(v )

                    .child("receiverReadMessage");


            databaseSenderReadMessage.setValue("Yes");
            databaseReceiverReadMessage.setValue("Yes");




        }


    }

    private void messageArivedOrNot(String v, int i){

        if(!boolList.get(i)){

            DatabaseReference databaseSenderReadMessage = FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child(v )

                    .child("currentReadMessage");

            DatabaseReference databaseReceiverReadMessage = FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(recieverRoom)
                    .child(v )

                    .child("currentReadMessage");


            databaseSenderReadMessage.setValue(true);
            databaseReceiverReadMessage.setValue(true);

        }


    }
    @Override
    protected void onPause() {
        super.onPause();
        shouldContinueRunnable = false;
        timer.cancel();
        timer2.cancel();
        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Timer timer = new Timer();
        shouldContinueRunnable = true;
        //message event read is true
        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        timer2.cancel();
        shouldContinueRunnable = false;
        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(false);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer2.cancel();
        shouldContinueRunnable = false;
        //message event read is true
        DatabaseReference isMessageReadRef = FirebaseDatabase.getInstance().getReference()
                .child("isMessageRead")
                .child(senderRoom);

// Set the "isRead" value
        isMessageReadRef.child("isRead").setValue(false);
    }
  /*  private void sendNotification(String recipientToken, String message) {
        PNPushType pushType = null;
        pubnub.publish()
                .channel("channel_name")
                .message(message)
                .pushType(PNPushType.FCM)
                .target(recipientToken)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (status.isError()) {
                            Log.e("PubNub", "Failed to send notification: " + status.getErrorData());
                        } else {
                            Log.d("PubNub", "Notification sent successfully. Timetoken: " + result.getTimetoken());
                        }
                    }
                });*/

}



