package de.piatohealth.patient.sync

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.piatohealth.patient.helpers.Const
import java.util.*


class JobManager(private val context: Context?) {
    fun startOneTimeSyncJob(syncWhat: Int, jobId: Int) {
        var jobIdLocal = jobId
        val componentName = ComponentName(context!!, SyncJob::class.java)
        val bundle = PersistableBundle()
        bundle.putInt(SYNC_WHAT, syncWhat)
        val uploadInfoOneTime = JobInfo.Builder(jobIdLocal, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setMinimumLatency(1)
            .setOverrideDeadline(1)
            .setExtras(bundle)
            .build()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        while (scheduler.getPendingJob(jobIdLocal) != null) {
            jobIdLocal += 1
        }
        scheduler.schedule(uploadInfoOneTime)
        val finalJobId = jobIdLocal
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (finalJobId == UPLOAD_JOB_ID_ACTIVATE_APP) {
                    val intent = Intent(Const.INTENT_ACTIVATE_APP)
                    intent.putExtra(SyncJob.UPLOAD_FAILURE, true)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                } else if (finalJobId == UPLOAD_JOB_ID_FCM_TOKEN) {
                    val intent = Intent(Const.INTENT_UPLOAD_INIT_TOKEN)
                    intent.putExtra(SyncJob.UPLOAD_FAILURE, true)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
            }
        }, TIME_OUT.toLong())
    }

    fun startOneTimeSyncJobPatKey(syncWhat: Int, jobId: Int, patientKey: String) {
        var jobIdLocal = jobId
        val componentName = ComponentName(context!!, SyncJob::class.java)
        val bundle = PersistableBundle()
        bundle.putInt(SYNC_WHAT, syncWhat)
        bundle.putString(Const.PATIENT_KEY, patientKey)
        val uploadInfoOneTime = JobInfo.Builder(jobIdLocal, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setMinimumLatency(1)
            .setOverrideDeadline(1)
            .setExtras(bundle)
            .build()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        while (scheduler.getPendingJob(jobIdLocal) != null) {
            jobIdLocal += 1
        }
        scheduler.schedule(uploadInfoOneTime)
        val finalJobId = jobIdLocal
        Timer().schedule(object : TimerTask() {
            override fun run() {
                cancelJob(finalJobId)
            }
        }, TIME_OUT.toLong())
    }

    fun startOneTimeSyncJobCallKey(syncWhat: Int, jobId: Int, callKey: String) {
        var jobIdLocal = jobId
        val componentName = ComponentName(context!!, SyncJob::class.java)
        val bundle = PersistableBundle()
        bundle.putInt(SYNC_WHAT, syncWhat)
        bundle.putString(Const.CALL_KEY, callKey)
        val uploadInfoOneTime = JobInfo.Builder(jobIdLocal, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setMinimumLatency(1)
            .setOverrideDeadline(1)
            .setExtras(bundle)
            .build()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        while (scheduler.getPendingJob(jobIdLocal) != null) {
            jobIdLocal += 1
        }
        scheduler.schedule(uploadInfoOneTime)
        val finalJobId = jobIdLocal
        Timer().schedule(object : TimerTask() {
            override fun run() {
                cancelJob(finalJobId)
            }
        }, TIME_OUT.toLong())
    }

    fun startOneTimeSyncJobNotify(syncWhat: Int, jobId: Int, notificationType: String) {
        var jobIdLocal = jobId
        val componentName = ComponentName(context!!, SyncJob::class.java)
        val bundle = PersistableBundle()
        bundle.putInt(SYNC_WHAT, syncWhat)
        bundle.putString(Const.NOTIFICATION_TYPE, notificationType)
        val uploadInfoOneTime = JobInfo.Builder(jobIdLocal, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setMinimumLatency(1)
            .setOverrideDeadline(1)
            .setExtras(bundle)
            .build()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        while (scheduler.getPendingJob(jobIdLocal) != null) {
            jobIdLocal += 1
        }
        scheduler.schedule(uploadInfoOneTime)
        val finalJobId = jobIdLocal
        Timer().schedule(object : TimerTask() {
            override fun run() {
                cancelJob(finalJobId)
            }
        }, TIME_OUT.toLong())
    }

    fun cancelJob(id: Int) {
        if (context != null) {
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.cancel(id)
        }
    }

    companion object {
        val TAG: String = JobManager::class.java.simpleName
        const val SYNC_WHAT = "SYNC_WHAT"
        const val JOB_ID_ALL = 0
        const val JOB_ID_GET_CALLS_PERIODIC = 1
        const val UPLOAD_JOB_ID_ACTIVATE_APP = 2
        const val UPLOAD_JOB_ID_FCM_TOKEN = 3
        const val JOB_ID_GET_INACTIVE_PATIENTS = 7
        const val JOB_ID_GET_PATIENT_DATA = 8
        const val JOB_ID_GET_NURSES = 9
        const val JOB_ID_GET_ROOMS = 10
        const val JOB_ID_GET_ACTIVATED_PATIENTS = 11
        const val JOB_ID_SUBSCRIBE_PATIENT = 12
        const val JOB_ID_UNSUBSCRIBE_PATIENT = 13
        const val JOB_ID_GET_CALLS = 14
        const val JOB_ID_SUBSCRIBE_CALL = 15
        const val JOB_ID_UNSUBSCRIBE_CALL = 16
        const val JOB_ID_ANSWER_CALL = 17
        const val JOB_ID_PROCEED_CALL = 18
        const val JOB_ID_COMPLETE_CALL = 19
        const val JOB_ID_GET_CALLS_CALL_ACTIVATED = 20
        const val JOB_ID_GET_CALLS_CALL_PLEASE_SUBSCRIBE_TO_CALL = 21
        private const val TIME_OUT = 15000
    }
}