package de.piatohealth.patient.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import de.piatohealth.patient.MainActivity
import de.piatohealth.patient.R

class NotificationReceiver : BroadcastReceiver() {
    private var notifyManager: NotificationManager? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (notifyManager == null) {
            notifyManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        val fragmentName = intent.getStringExtra(NOTIFICATION_FRAGMENT_NAME)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE)
        val notificationText = intent.getStringExtra(NOTIFICATION_TEXT)
        val notificationChannelName = intent.getStringExtra(NOTIFICATION_CHANNEL_NAME)
        val notificationChannelId = intent.getStringExtra(NOTIFICATION_CHANNEL_ID)
        val notificationObjectId = intent.getIntExtra(NOTIFICATION_OBJECT_ID, 0)
        val builder = NotificationCompat.Builder(context, notificationChannelName!!)
        val mChannel = NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notifyManager!!.createNotificationChannel(mChannel)
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra(fragmentName, true)
        resultIntent.putExtra(NOTIFICATION_OBJECT_ID, notificationObjectId)
        val stackBuilder = TaskStackBuilder.create(context)

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent: PendingIntent? =
            stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(resultPendingIntent)
        if (notificationChannelId != null) {
            builder.setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.green_100))
                .setContentText(notificationText)
                .setChannelId(notificationChannelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        }
        notifyManager!!.notify(notificationId, builder.build())
    }

    companion object {
        const val NOTIFICATION_ID = "notification-id"
        const val NOTIFICATION_TITLE = "notification-title"
        const val NOTIFICATION_TEXT = "notification-text"
        const val NOTIFICATION_CHANNEL_NAME = "notification-channel-name"
        const val NOTIFICATION_CHANNEL_ID = "notification-channel-id"
        const val NOTIFICATION_FRAGMENT_NAME = "notification-fragment-name"
        const val NOTIFICATION_OBJECT_ID = "notification-object-id"
    }
}