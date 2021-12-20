package com.example.CryptoAppAndroid.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.CryptoAppAndroid.MainActivity
import com.example.CryptoAppAndroid.R

@RequiresApi(Build.VERSION_CODES.M)
fun NotificationManager.sendNotification(
    messageBody: String,
    applicationContext: Context,
    channelId: String,
    notificationId: Int,
) {
    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        notificationId,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        channelId
    ).setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Crypto Alert")
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    notify(notificationId, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

fun NotificationManager.createChannel(channelId: String, channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.let {
            it.enableLights(true)
            it.lightColor = Color.RED
            it.enableVibration(true)
            it.description = "Crypto Alert Channel"
            it.vibrationPattern = longArrayOf(100, 100, 100)
        }

        Log.i("AlertWorker", "Creating channel $channelId, $channelName")

        this.createNotificationChannel(notificationChannel)
    }
}
