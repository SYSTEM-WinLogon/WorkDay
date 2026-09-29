package daka.work.day.utils

import daka.work.day.model.WorkRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WorkTimeUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val timeShortFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthDisplayFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.CHINA)

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentTimeString(): String {
        return timeFormat.format(Date())
    }

    fun getCurrentMonthString(): String {
        return monthFormat.format(Date())
    }

    fun formatMonthForDisplay(yearMonthStr: String): String {
        return try {
            val date = monthFormat.parse(yearMonthStr)
            date?.let { monthDisplayFormat.format(it) } ?: yearMonthStr
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun getPreviousMonth(yearMonthStr: String): String {
        return try {
            val cal = Calendar.getInstance()
            monthFormat.parse(yearMonthStr)?.let { cal.time = it }
            cal.add(Calendar.MONTH, -1)
            monthFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun getNextMonth(yearMonthStr: String): String {
        return try {
            val cal = Calendar.getInstance()
            monthFormat.parse(yearMonthStr)?.let { cal.time = it }
            cal.add(Calendar.MONTH, 1)
            monthFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun getDayOfWeek(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr)
            date?.let { dayOfWeekFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMillisToDuration(millis: Long): String {
        if (millis <= 0) return "0小时0分"
        val totalMinutes = millis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}小时${minutes}分"
    }

    fun formatMillisToTime(millis: Long?): String {
        if (millis == null) return "--:--"
        return timeShortFormat.format(Date(millis))
    }

    /**
     * Parse date (yyyy-MM-dd) and time (HH:mm) into epoch millis
     */
    fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long? {
        return try {
            val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            fullFormat.parse("$dateStr $timeStr")?.time
        } catch (e: Exception) {
            null
        }
    }

    data class MonthStats(
        val totalMillis: Long,
        val workDaysCount: Int,
        val completedDaysCount: Int,
        val formattedTotalTime: String,
        val averageHoursPerDay: String
    )

    fun calculateMonthStats(records: List<WorkRecord>): MonthStats {
        var totalMillis = 0L
        var workDaysCount = 0
        var completedDaysCount = 0

        for (record in records) {
            if (record.clockInTime != null) {
                workDaysCount++
            }
            if (record.clockInTime != null && record.clockOutTime != null && record.clockOutTime > record.clockInTime) {
                completedDaysCount++
                totalMillis += record.durationMillis
            }
        }

        val totalHoursDouble = totalMillis / (1000.0 * 3600.0)
        val avgHours = if (completedDaysCount > 0) {
            String.format(Locale.getDefault(), "%.1f", totalHoursDouble / completedDaysCount)
        } else {
            "0.0"
        }

        return MonthStats(
            totalMillis = totalMillis,
            workDaysCount = workDaysCount,
            completedDaysCount = completedDaysCount,
            formattedTotalTime = formatMillisToDuration(totalMillis),
            averageHoursPerDay = "$avgHours 小时/天"
        )
    }
}
