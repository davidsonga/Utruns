package com.example.utrun;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            // Handle the data payload (if any)
            // This is where you can extract any additional information from the message
        }



        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String clickAction = remoteMessage.getData().get("click_action");
            if (clickAction != null && clickAction.equals("OPEN_CHAT_ACTIVITY")) {
                //Intent intent = new Intent(this, ChatAct.class);
               // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               // @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                // Build the notification
               // NotificationCompat.Builder builder =
                     //   new NotificationCompat.Builder(this, "channel_id")
                              //  .setContentTitle(title)
                              //  .setContentText(body)
                               // .setSmallIcon(R.drawable.edit_profile)
                              //  .setAutoCancel(true)
                                //.setContentIntent(pendingIntent);

              //  NotificationManager notificationMarnage=null;
                // Show the notification
             //   notificationMarnage.notify(0, builder.build());
            }
            // Display the notification
            showNotification(title, body);


        }
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        // Create a notification channel for devices running Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "channel_id")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.edit_profile)
                        .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(0, builder.build());
    }
}
