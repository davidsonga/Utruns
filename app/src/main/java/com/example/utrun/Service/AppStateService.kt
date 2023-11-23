package com.example.utrun.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.utrun.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppStateService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private var appStartTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.reference.child("login").child("email").child(auth.currentUser?.uid ?: "")
        setUserOnlineStatus("Online")
        appStartTime = System.currentTimeMillis()

        // Create a notification channel
        createNotificationChannel()

        // Create a foreground notification with the same channel ID
        val notification: Notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("App Service")
            .setContentText("Monitoring app state")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("[dd/MM//yy] [HH:mm:ss]", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Set user's online status to the time spent in milliseconds
        val currentDateTime = getCurrentDateTime()
        setUserOnlineStatus(currentDateTime)
    }

    private fun setUserOnlineStatus(status: String) {
        Toast.makeText(this,"great", Toast.LENGTH_LONG).show()
        userRef.child("state").setValue(status)
        userRef.child("typing").setValue(false)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channelId"
            val channelName = "Channel Name"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}