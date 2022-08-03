package de.piatohealth.patient.helpers

object DateTimeHelper {
    fun changeDateFormat(timestampOld: String): String {
        return timestampOld.substring(8, 10) + "." + timestampOld.substring(
            5,
            7
        ) + "." + timestampOld.substring(0, 4)
    }
}