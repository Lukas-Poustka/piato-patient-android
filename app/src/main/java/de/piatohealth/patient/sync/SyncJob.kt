package de.piatohealth.patient.sync

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.piatohealth.patient.helpers.Const
import de.piatohealth.patient.helpers.FunctionHelper
import java.util.*

class SyncJob : JobService() {
    private var params: JobParameters? = null
    private var syncWhat = 1
    private var stopServiceTimer: Timer? = null
    private var context: Context? = null
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "SyncJob started")
        this.params = params
        context = applicationContext
        stopServiceTimer = Timer()
        stopServiceTimer!!.schedule(object : TimerTask() {
            override fun run() {
                jobFinished(params, false)
            }
        }, TIME_OUT.toLong())
        initSync()
        sync()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "Job stopped")
        return true
    }

    override fun onDestroy() {
        if (stopServiceTimer != null) {
            stopServiceTimer!!.cancel()
        }
        Log.w(TAG, "SyncJob finished")
        super.onDestroy()
    }

    private fun initSync() {
        syncWhat = params!!.extras.getInt(JobManager.SYNC_WHAT)
    }

    private fun sync() {
        when (syncWhat) {
            GET_INACTIVE_PATIENTS -> {
                Log.w(TAG, "Get inactive patients")
            }
            ALL -> {
                Log.w(TAG, "Get all")
            }
        }
    }

    fun refreshPatientList() {
        val sp = applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        if (sp.getBoolean(Const.LOGIN_DATA_UPDATE_RUNNING, false)) {
            FunctionHelper.substractOpenRequest(applicationContext)
            val openRequestNo = FunctionHelper.getOpenRequestNo(applicationContext)
            if (openRequestNo > 0) {
                Log.w("Getting data update from server...", "$openRequestNo opened requests pending")
            } else {
                val editor = sp.edit()
                editor.putBoolean(Const.LOGIN_DATA_UPDATE_RUNNING, false)
                editor.apply()
                Log.w(TAG, "Refresh patient list")
                val intent = Intent(Const.REFRESH_ACTIVATED_PATIENTS)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
    }

    companion object {
        const val ALL = 0
        const val GET_INACTIVE_PATIENTS = 7
        const val GET_CALLS = 14
        const val SUBSCRIBE_CALL = 15
        const val UNSUBSCRIBE_CALL = 16
        const val ANSWER_CALL = 17
        const val PROCEED_CALL = 18
        const val COMPLETE_CALL = 19
        const val UPLOAD_FAILURE = "upload-failure"
        val TAG: String = SyncJob::class.java.simpleName
        private const val TIME_OUT = 15000
    }
}