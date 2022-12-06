package com.example.hanger.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hanger.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseIdService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            updateToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val sent = message.data["sent"]

        val currUser = FirebaseAuth.getInstance().currentUser!!

        if(sent == currUser.uid) {
            sendNotification(message)
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        val user = message.data["user"]!!
        val icon = message.data["icon"]!!
        val title = message.data["title"]!!
        val body = message.data["body"]!!

        val notification: RemoteMessage.Notification? = message.notification
        val j: Int = Integer.parseInt(user.replace("[\\D]".toRegex(), ""))

        val intent = Intent(this, MessageActivity::class.java)
        intent.putExtra("userid", user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_IMMUTABLE)
        val channelId = "fcm_default_channel"
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(Integer.parseInt(icon))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if(j > 0) {
            i = j
        }

        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(i, builder.build())
    }

    private fun updateToken(refreshToken: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(refreshToken)
        ref.child(firebaseUser.uid).setValue(token)
    }
}