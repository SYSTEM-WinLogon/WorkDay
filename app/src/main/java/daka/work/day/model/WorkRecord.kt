package daka.work.day.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkRecord(
    val id: Long = 0,
    val date: String, // Format: "yyyy-MM-dd"
    val clockInTime: Long? = null, // Milliseconds timestamp
    val clockOutTime: Long? = null, // Milliseconds timestamp
    val note: String = ""
) {
    /**
     * Calculates duration in milliseconds if both clockInTime and clockOutTime are present.
     */
    val durationMillis: Long
        get() {
            return if (clockInTime != null && clockOutTime != null && clockOutTime > clockInTime) {
                clockOutTime - clockInTime
            } else {
                0L
            }
        }

    /**
     * Duration in hours as a decimal number.
     */
    val durationHours: Double
        get() = durationMillis / (1000.0 * 3600.0)

    val formattedClockIn: String
        get() {
            return clockInTime?.let {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
            } ?: "未打卡"
        }

    val formattedClockOut: String
        get() {
            return clockOutTime?.let {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
            } ?: "未打卡"
        }

    /**
     * Formatted string for daily working duration (e.g., "8小时15分")
     */
    val formattedDuration: String
        get() {
            if (clockInTime == null) return "未打卡"
            if (clockOutTime == null) return "进行中"
            val totalMinutes = durationMillis / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return "${hours}小时${minutes}分"
        }
}
