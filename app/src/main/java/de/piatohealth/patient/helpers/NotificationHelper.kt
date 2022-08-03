package de.piatohealth.patient.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.piatohealth.patient.notification.NotificationReceiver

object NotificationHelper {
    const val CHANNEL_NAME_CALL_ACTIVATED = "Call activated"
    const val CHANNEL_ID_CALL_ACTIVATED = "call_activated"
    const val NOTIFICATION_ID_CALL_ACTIVATED = 1000000001

    const val CHANNEL_NAME_PLEASE_SUBSCRIBE_TO_CALL = "Please subscribe to call"
    const val CHANNEL_ID_PLEASE_SUBSCRIBE_TO_CALL = "please_subscribe_to_call"
    const val NOTIFICATION_ID_PLEASE_SUBSCRIBE_TO_CALL = 1000000002

    const val CHANNEL_NAME_THANK_YOU = "Thank you"
    const val CHANNEL_ID_THANK_YOU = "thank_you"
    const val NOTIFICATION_ID_THANK_YOU = 1000000003

    fun createNotification(
        context: Context,
        notificationId: Int,
        time: Long,
        title: String?,
        text: String?,
        channelName: String?,
        channelId: String?,
        fragmentName: String?,
        objectId: Int
    ) {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId)
        intent.putExtra(NotificationReceiver.NOTIFICATION_TITLE, title)
        intent.putExtra(NotificationReceiver.NOTIFICATION_TEXT, text)
        intent.putExtra(NotificationReceiver.NOTIFICATION_CHANNEL_NAME, channelName)
        intent.putExtra(NotificationReceiver.NOTIFICATION_CHANNEL_ID, channelId)
        intent.putExtra(NotificationReceiver.NOTIFICATION_FRAGMENT_NAME, fragmentName)
        intent.putExtra(NotificationReceiver.NOTIFICATION_OBJECT_ID, objectId)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, Intent(
                context,
                NotificationReceiver::class.java
            ), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pendingIntent)
    }
}