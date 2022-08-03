package de.piatohealth.patient.helpers

import de.piatohealth.patient.BuildConfig

internal object Const {
    const val CALLS_REFRESH_PERIOD_IN_SEC = 60

    const val GLOBAL = "global"
    const val DEVICE_ID = "device_id"

    const val SHOW_INTRO = "show_intro"

    const val FCM_TOKEN = "fcm_token"
    const val FCM_TOKEN_AVAILABLE = "fcm_token_available"
    const val FCM_TOKEN_UPLOADED = "fcm_token_uploaded"
    const val INTENT_ACTIVATE_APP = "intent-upload-init-token"
    const val INTENT_UPLOAD_INIT_TOKEN = "intent-upload-init-token"
    const val REFRESH_CALLS = "refresh-calls"
    const val REFRESH_INACTIVE_PATIENTS = "refresh-inactive-patients"
    const val REFRESH_ACTIVATED_PATIENTS = "refresh-activated-patients"
    const val REFRESH_PATIENT_DATA = "refresh-patient-data"
    const val REFRESH_ROOMS = "refresh-rooms"
    const val REFRESH_NURSES = "refresh-nurses"
    const val SCHEDULE_CALLS_UPDATE = "schedule-calls-update"
    const val OPEN_REQUESTS = "open-requests"

    val BASE_CLOUD_URL =
        if (BuildConfig.DEBUG) "https://cloud.piato.dev/appcom/" else "https://cloud.piatodemo.de/appcom/"
    val BASE_CLINIC_URL =
        if (BuildConfig.DEBUG) "https://clinic.piato.dev/appcom/" else "https://clinic.piatodemo.de/appcom/"
    const val VERSION_URL = "v1/"
    const val NURSES_URL = "nurses/"
    const val PATIENTS_URL = "patients/"
    const val DEVICES_URL = "devices/"
    const val ACTIVATE_APP_URL = "activate"
    const val LOGIN_URL = "login"
    const val ACTIVATE_URL = "activate"
    const val GET_PATIENTS_URL = "patients"
    const val SUBSCRIBE_URL = "subscribe"
    const val UNSUBSCRIBE_URL = "unsubscribe"
    const val ACTIVATED_URL = "activated"
    const val INACTIVE_URL = "inactive"
    const val CALLS_URL = "calls"
    const val ANSWER_URL = "answer"
    const val PROCEED_URL = "proceed"
    const val COMPLETE_URL = "complete"

    // API keywords
    const val ACTIVATION_CODE = "activationCode"
    const val NOTIFICATION_TOKEN = "notificationToken"
    const val OS_PLATFORM = "osPlatform"
    const val ANDROID = "android"
    const val DEVICE_TOKEN = "deviceToken"
    const val LOGIN_CODE = "loginCode"
    const val CLOUD_TOKEN = "cloudToken"
    const val CLINIC_TOKEN = "clinicToken"
    const val NURSE_KEY = "nurseKey"
    const val PATIENTS = "patients"
    const val PATIENT_KEY = "patientKey"
    const val ROOM_KEY = "roomKey"
    const val KEY = "key"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val BIRTHDAY = "birthday"
    const val ADMITTED_AT = "admittedAt"
    const val NURSES = "nurses"
    const val IS_RED = "isRed"
    const val ROOMS = "rooms"
    const val TITLE = "title"
    const val PERSONAL = "personal"
    const val OTHER = "other"
    const val CALL_KEY = "callKey"
    const val STATE_KEY = "stateKey"
    const val TYPE = "type"
    const val EVENT = "event"
    const val CALL_ACTIVATED = "CallActivated"
    const val CALLS_UPDATED = "CallsUpdated"
    const val PLEASE_SUBSCRIBE_TO_CALL = "PleaseSubscribeToCall"
    const val THANK_YOU = "ThankYou"
    const val DETAIL = "detail"
    const val IS_ESCALATED = "isEscalated"
    const val STATE = "state"
    const val MINUTES = "minutes"

    // sharedPreferences keys
    const val OWN_NURSE_KEY = "own_nurse_key"
    const val NOTIFICATION_TYPE = "notification_type"
    const val LOGIN_DATA_UPDATE_RUNNING  = "login-data-update-running"

    const val QR_CODE_SCANNER_ACTIVATE_PATIENT = 10000
    const val PATIENT_KEY_ACTIVATION = "patient_key_activation"
    const val QR_CODE_SCANNER_ACTIVATE_APP = 10001
    const val QR_CODE_SCANNER_LOGIN = 10002
}