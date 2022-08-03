package de.piatohealth.patient.notification

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.piatohealth.patient.helpers.Const
import de.piatohealth.patient.sync.JobManager
import de.piatohealth.patient.sync.SyncJob

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data: Map<String, String?> = remoteMessage.data
        if (data[Const.EVENT] != null) {
            if (data[Const.EVENT] == Const.CALLS_UPDATED) {
                Log.w("FCM", "Update calls")
                JobManager(applicationContext).startOneTimeSyncJobNotify(
                    SyncJob.GET_CALLS,
                    JobManager.JOB_ID_GET_CALLS,
                    Const.CALLS_UPDATED
                )
            } else if (data[Const.EVENT] == Const.CALL_ACTIVATED) {
                Log.w("FCM", "Call activated")
                JobManager(applicationContext).startOneTimeSyncJobNotify(
                    SyncJob.GET_CALLS,
                    JobManager.JOB_ID_GET_CALLS_CALL_ACTIVATED,
                    Const.CALL_ACTIVATED
                )
            } else if (data[Const.EVENT] == Const.PLEASE_SUBSCRIBE_TO_CALL) {
                Log.w("FCM", "Please subscribe to call")
                JobManager(applicationContext).startOneTimeSyncJobNotify(
                    SyncJob.GET_CALLS,
                    JobManager.JOB_ID_GET_CALLS_CALL_PLEASE_SUBSCRIBE_TO_CALL,
                    Const.PLEASE_SUBSCRIBE_TO_CALL
                )
            }
        }
    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        val sp: SharedPreferences =
            applicationContext.getSharedPreferences(Const.GLOBAL, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(Const.FCM_TOKEN, fcmToken)
        editor.putBoolean(Const.FCM_TOKEN_AVAILABLE, true)
        editor.putBoolean(Const.FCM_TOKEN_UPLOADED, false)
        editor.apply()
        Log.w("FCM", "FCM-Token angekommen")
    }
}