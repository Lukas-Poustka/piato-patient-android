package de.piatohealth.patient.calls

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import de.piatohealth.patient.helpers.Const
import java.text.SimpleDateFormat
import java.util.*

object RefreshHelper {
    private const val REFRESH_ALARM_ID = 1000000100
    val TAG: String = RefreshHelper::class.java.simpleName

    fun scheduleNextRefresh(context: Context) {
        val intent = Intent(context, RefreshReceiver::class.java)
        val nextSync = Date().time + Const.CALLS_REFRESH_PERIOD_IN_SEC * 1000
        val nextSyncStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMAN).format(nextSync)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextSync,
            PendingIntent.getBroadcast(
                context,
                REFRESH_ALARM_ID,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.w(TAG, "getCalls refresh scheduled on $nextSyncStr")
    }

    fun cancelRefresh(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context, REFRESH_ALARM_ID, Intent(
                context,
                RefreshReceiver::class.java
            ), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pendingIntent)
        Log.w(TAG, "getCalls refresh cancelled")
    }
}