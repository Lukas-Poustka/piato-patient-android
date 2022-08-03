package de.piatohealth.patient.helpers

import android.content.Context
import org.json.JSONException

object FunctionHelper {
    fun getOpenRequestNo(context: Context): Int {
        val sp = context.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        return sp.getInt(Const.OPEN_REQUESTS, 0)
    }

    fun setOpenRequestsZero(context: Context) {
        val sp = context.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(Const.OPEN_REQUESTS, 0)
        editor.apply()
    }

    fun addOpenRequest(context: Context) {
        val sp = context.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val openRequests = sp.getInt(Const.OPEN_REQUESTS, 0)
        val editor = sp.edit()
        editor.putInt(Const.OPEN_REQUESTS, openRequests + 1)
        editor.apply()
    }

    fun substractOpenRequest(context: Context) {
        val sp = context.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val openRequests = sp.getInt(Const.OPEN_REQUESTS, 0)
        val editor = sp.edit()
        editor.putInt(Const.OPEN_REQUESTS, openRequests - 1)
        editor.apply()
    }

    fun deleteNurseData(context: Context) {
        val sp = context.getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        val editor = sp.edit()
        try {
            editor.putString(Const.CLOUD_TOKEN, "")
            editor.putString(Const.CLINIC_TOKEN, "")
            editor.putString(Const.OWN_NURSE_KEY, "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        editor.apply()
        setOpenRequestsZero(context)
        /*MySQLiteInactivePatientHelper(context).cleanTablePatient()
        MySQLitePatientDataHelper(context).cleanTablePatientData()
        MySQLiteNurseHelper(context).cleanTableNurse()
        MySQLiteRoomHelper(context).cleanTableRoom()
        MySQLiteActivatedPatientHelper(context).cleanTableActivatedPatient()
        MySQLitePersonalCallHelper(context).cleanTablePersonalCall()
        MySQLiteOtherCallHelper(context).cleanTableOtherCall()
        MySQLiteCallStateHelper(context).cleanTableCallState()*/
    }
}