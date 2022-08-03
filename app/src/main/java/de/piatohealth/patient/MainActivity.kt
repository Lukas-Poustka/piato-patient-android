package de.piatohealth.patient

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.integration.android.IntentIntegrator
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import de.piatohealth.patient.helpers.Const
import de.piatohealth.patient.intro.Intro01
import de.piatohealth.patient.sync.JobManager
import de.piatohealth.patient.sync.SyncJob
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_screen)

        if (applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
                .getString(Const.CLOUD_TOKEN, "") != "" &&
            applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
                .getString(Const.CLINIC_TOKEN, "") != "" &&
            applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
                .getString(Const.OWN_NURSE_KEY, "") != ""
        ) {
            JobManager(this).startOneTimeSyncJob(
                SyncJob.ALL,
                JobManager.JOB_ID_ALL
            )
        }
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
                        .getString(Const.DEVICE_TOKEN, "") == ""
                ) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, Intro01(), Intro01.TAG)
                        .commitAllowingStateLoss()
                }/* else if (applicationContext.getSharedPreferences(
                        Const.GLOBAL,
                        Context.MODE_PRIVATE
                    )
                        .getString(Const.CLOUD_TOKEN, "") == ""
                ) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, Login(), Login.TAG)
                        .commitAllowingStateLoss()
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, Calls(), Calls.TAG)
                        .commitAllowingStateLoss()
                }*/
            }, 200
        )
    }

    /*fun startQRCodeScanning(description: String, requestCode: Int) {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt(description)
        integrator.setCameraId(0)
        integrator.setBarcodeImageEnabled(false)
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.captureActivity = CaptureActivityPortrait::class.java
        integrator.setRequestCode(requestCode)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val result = IntentIntegrator.parseActivityResult(resultCode, data)
            val resultContents = result.contents
            if (requestCode == Const.QR_CODE_SCANNER_ACTIVATE_PATIENT && resultCode == RESULT_OK && resultContents != null) {
                activatePatient(resultContents)
            } else if (requestCode == Const.QR_CODE_SCANNER_ACTIVATE_APP && resultCode == RESULT_OK && resultContents != null) {
                activateApp(resultContents)
            } else if (requestCode == Const.QR_CODE_SCANNER_LOGIN && resultCode == RESULT_OK && resultContents != null) {
                login(resultContents)
            }
        }
    }

    private fun activatePatient(activationCode: String) {
        Log.w(SyncJob.TAG, "Activate patient...")

        val sp: SharedPreferences =
            applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val patientKey = sp.getString(Const.PATIENT_KEY_ACTIVATION, "")
        val editor = sp.edit()
        editor.putString(Const.PATIENT_KEY_ACTIVATION, "")
        editor.apply()

        val jsonObject = JSONObject()
        try {
            jsonObject.put(Const.ACTIVATION_CODE, activationCode)
            jsonObject.put(Const.PATIENT_KEY, patientKey)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val entity = StringEntity(jsonObject.toString(), "UTF-8")
        SyncClient.post(
            applicationContext,
            Const.BASE_CLOUD_URL + Const.VERSION_URL + Const.NURSES_URL + Const.PATIENTS_URL + Const.ACTIVATE_URL,
            entity,
            object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header?>?,
                    response: JSONObject
                ) {
                    val roomKey = MySQLiteInactivePatientHelper(applicationContext).getInactivePatient(patientKey!!).roomKey
                    MySQLiteActivatedPatientHelper(applicationContext).addActivatedPatient(
                        patientKey,
                        roomKey!!,
                        "[]"
                    )
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.container,
                            PatientAssignment.newInstance(patientKey),
                            PatientAssignment.TAG
                        )
                        .addToBackStack(PatientAssignment.TAG)
                        .commitAllowingStateLoss()

                    JobManager(applicationContext).startOneTimeSyncJob(SyncJob.GET_INACTIVE_PATIENTS, JobManager.JOB_ID_GET_INACTIVE_PATIENTS)

                    Log.w("Activate patient", "Successful")
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Activate patient", "error: $throwable")
                    Log.w(
                        "Activate patient",
                        "response: $errorResponse"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_failure_title)
                        .setMessage(R.string.activate_failure_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONArray?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Activate patient", "error: $throwable")
                    Log.w(
                        "Activate patient",
                        "response: $errorResponse"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_failure_title)
                        .setMessage(R.string.activate_failure_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    super.onFailure(statusCode, headers, responseString, throwable)
                    Log.w("Activate patient", "error: $throwable")
                    Log.w(
                        "Activate patient",
                        "response: $responseString"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_failure_title)
                        .setMessage(R.string.activate_failure_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
        )
    }

    private fun activateApp(activationCode: String) {
        Log.w(SyncJob.TAG, "Activate app...")
        val sp: SharedPreferences =
            applicationContext.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val jsonObject = JSONObject()
        try {
            jsonObject.put(Const.ACTIVATION_CODE, activationCode)
            jsonObject.put(Const.NOTIFICATION_TOKEN, sp.getString(Const.FCM_TOKEN, ""))
            jsonObject.put(Const.OS_PLATFORM, Const.ANDROID)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val entity = StringEntity(jsonObject.toString(), "UTF-8")
        SyncClient.post(
            applicationContext,
            Const.BASE_CLINIC_URL + Const.VERSION_URL + Const.DEVICES_URL + Const.ACTIVATE_APP_URL,
            entity,
            object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header?>?,
                    response: JSONObject
                ) {
                    val editor = sp.edit()
                    try {
                        editor.putString(Const.DEVICE_TOKEN, response.getString(Const.DEVICE_TOKEN))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    editor.apply()

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, Login(), Login.TAG)
                        .commitAllowingStateLoss()

                    Log.w("Activate app", "Successful")
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Activate app", "error: $throwable")
                    Log.w(
                        "Activate app",
                        "response: $errorResponse"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_invalid_title)
                        .setMessage(R.string.activate_invalid_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONArray?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Activate app", "error: $throwable")
                    Log.w(
                        "Activate app",
                        "response: $errorResponse"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_invalid_title)
                        .setMessage(R.string.activate_invalid_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    super.onFailure(statusCode, headers, responseString, throwable)
                    Log.w("Activate app", "error: $throwable")
                    Log.w(
                        "Activate app",
                        "response: $responseString"
                    )

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        R.style.AlertDialogStyle
                    )
                        .setTitle(R.string.activate_invalid_title)
                        .setMessage(R.string.activate_invalid_text)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }

            })
    }*/

    /*private fun login(loginCode: String) {
        Log.w(SyncJob.TAG, "Login...")
        val jsonObject = JSONObject()
        try {
            jsonObject.put(Const.LOGIN_CODE, loginCode)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val entity = StringEntity(jsonObject.toString(), "UTF-8")
        SyncClient.post(
            applicationContext,
            Const.BASE_CLINIC_URL + Const.VERSION_URL + Const.NURSES_URL + Const.LOGIN_URL,
            entity,
            object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header?>?,
                    response: JSONObject
                ) {
                    val sp: SharedPreferences = applicationContext.getSharedPreferences(
                        Const.GLOBAL,
                        Context.MODE_PRIVATE
                    )
                    val editor = sp.edit()
                    try {
                        editor.putString(
                            Const.CLOUD_TOKEN,
                            response.getString(Const.CLOUD_TOKEN)
                        )
                        editor.putString(
                            Const.CLINIC_TOKEN,
                            response.getString(Const.CLINIC_TOKEN)
                        )
                        editor.putString(
                            Const.OWN_NURSE_KEY,
                            response.getString(Const.NURSE_KEY)
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    editor.apply()

                    JobManager(applicationContext).startOneTimeSyncJob(SyncJob.ALL, JobManager.JOB_ID_ALL)
                    editor.putBoolean(Const.LOGIN_DATA_UPDATE_RUNNING, true)
                    editor.apply()

                    Log.w("Login", "Successful")

                    if (sp.getBoolean(Const.SHOW_INTRO, true)) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, Intro01(), Intro01.TAG)
                            .commitAllowingStateLoss()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, PatientListStation(), PatientListStation.TAG)
                            .commitAllowingStateLoss()
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Login", "error: $throwable")
                    Log.w("Login", "response: $errorResponse")
                    when (statusCode) {
                        401 -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.container, ActivateApp(), ActivateApp.TAG)
                                .addToBackStack(ActivateApp.TAG)
                                .commitAllowingStateLoss()
                        }
                        422 -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                        else -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONArray?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.w("Login", "error: $throwable")
                    Log.w("Login", "response: $errorResponse")
                    when (statusCode) {
                        401 -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.container, ActivateApp(), ActivateApp.TAG)
                                .addToBackStack(ActivateApp.TAG)
                                .commitAllowingStateLoss()
                        }
                        422 -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                        else -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    super.onFailure(statusCode, headers, responseString, throwable)
                    Log.w("Login", "error: $throwable")
                    Log.w("Login", "response: $responseString")
                    when (statusCode) {
                        401 -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.container, ActivateApp(), ActivateApp.TAG)
                                .addToBackStack(ActivateApp.TAG)
                                .commitAllowingStateLoss()
                        }
                        422 -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                        else -> {
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.AlertDialogStyle
                            )
                                .setTitle(R.string.login_invalid_title)
                                .setMessage(R.string.login_invalid_text)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                    }
                }
            })
    }*/
}