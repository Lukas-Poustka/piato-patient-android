package de.piatohealth.patient.sync

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.entity.StringEntity
import de.piatohealth.patient.BuildConfig
import de.piatohealth.patient.helpers.Const


object SyncClient {
    fun post(
        context: Context?,
        url: String,
        entity: StringEntity?,
        responseHandler: JsonHttpResponseHandler?
    ) {
        val sp: SharedPreferences =
            context!!.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val cloudToken: String? = sp.getString(Const.CLOUD_TOKEN, "")
        val clinicToken: String? = sp.getString(Const.CLINIC_TOKEN, "")
        val deviceToken: String? = sp.getString(Const.DEVICE_TOKEN, "")
        if (url == Const.BASE_CLINIC_URL + Const.VERSION_URL + Const.NURSES_URL + Const.LOGIN_URL) {
            val client: AsyncHttpClient = initClient(url, deviceToken)
            client.post(context, url, entity, "application/json", responseHandler)
        } else if (url.startsWith(Const.BASE_CLOUD_URL)) {
            val client: AsyncHttpClient = initClient(url, cloudToken)
            client.post(context, url, entity, "application/json", responseHandler)
        } else {
            val client: AsyncHttpClient = initClient(url, clinicToken)
            client.post(context, url, entity, "application/json", responseHandler)
        }
    }

    fun get(context: Context?, url: String, responseHandler: JsonHttpResponseHandler?) {
        val sp: SharedPreferences =
            context!!.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val cloudToken: String? = sp.getString(Const.CLOUD_TOKEN, "")
        val clinicToken: String? = sp.getString(Const.CLINIC_TOKEN, "")
        if (url.startsWith(Const.BASE_CLOUD_URL)) {
            val client: AsyncHttpClient = initClient(url, cloudToken)
            client.get(context, url, responseHandler)
        } else {
            val client: AsyncHttpClient = initClient(url, clinicToken)
            client.get(context, url, responseHandler)
        }
    }

    private fun initClient(url: String, token: String?): AsyncHttpClient {
        Log.w("Upload URL: ", url)
        val client = AsyncHttpClient()
        client.addHeader("Accept", "application/json")
        client.addHeader("Content-Type", "application/json")
        client.addHeader("Authorization", "Bearer $token")
        client.addHeader("x-Platform", "Android")
        client.addHeader("x-OS-Version", Build.VERSION.SDK_INT.toString())
        client.addHeader("x-App-version", BuildConfig.VERSION_NAME)
        client.setTimeout(15000)
        return client
    }


}