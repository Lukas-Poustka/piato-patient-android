package de.piatohealth.patient.calls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.piatohealth.patient.sync.JobManager
import de.piatohealth.patient.sync.SyncJob

class RefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        JobManager(context).startOneTimeSyncJob(SyncJob.GET_CALLS, JobManager.JOB_ID_GET_CALLS_PERIODIC)
    }
}