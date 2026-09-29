package daka.work.day.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import daka.work.day.model.WorkRecord
import daka.work.day.utils.WorkTimeUtils
import java.util.Calendar
import java.util.Locale

@Composable
fun ManualEntryDialog(
    existingRecord: WorkRecord?,
    onDismiss: () -> Unit,
    onSave: (WorkRecord) -> Unit
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()

    var selectedDate by remember {
        mutableStateOf(existingRecord?.date ?: WorkTimeUtils.getTodayDateString())
    }
    var selectedClockIn by remember {
        mutableStateOf(existingRecord?.clockInTime)
    }
    var selectedClockOut by remember {
        mutableStateOf(existingRecord?.clockOutTime)
    }
    var note by remember {
        mutableStateOf(existingRecord?.note ?: "")
    }

    val isActiveRecord = existingRecord?.clockInTime != null && existingRecord.clockOutTime == null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge, // 28dp radius
        title = {
            Text(
                text = if (existingRecord != null) "编辑考勤记录" else "手动补卡 / 增加记录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "选择日期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        val parts = selectedDate.split("-")
                        val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
                        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
                        val day = parts.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(context, { _, y, m, d ->
                            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
                        }, year, month, day).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = selectedDate, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "上班打卡时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val tempCal = Calendar.getInstance()
                            selectedClockIn?.let { tempCal.timeInMillis = it }
                            TimePickerDialog(context, { _, h, m ->
                                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                selectedClockIn = WorkTimeUtils.parseDateTimeToMillis(selectedDate, timeStr)
                            }, tempCal.get(Calendar.HOUR_OF_DAY), tempCal.get(Calendar.MINUTE), true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = WorkTimeUtils.formatMillisToTime(selectedClockIn),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { selectedClockIn = null }) {
                        Text("清除", color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "下班打卡时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val tempCal = Calendar.getInstance()
                            selectedClockOut?.let { tempCal.timeInMillis = it }
                            TimePickerDialog(context, { _, h, m ->
                                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                selectedClockOut = WorkTimeUtils.parseDateTimeToMillis(selectedDate, timeStr)
                            }, tempCal.get(Calendar.HOUR_OF_DAY), tempCal.get(Calendar.MINUTE), true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = WorkTimeUtils.formatMillisToTime(selectedClockOut),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { selectedClockOut = null }) {
                        Text("清除", color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注说明 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isActiveRecord,
                onClick = {
                    val record = WorkRecord(
                        id = existingRecord?.id ?: 0,
                        date = selectedDate,
                        clockInTime = selectedClockIn,
                        clockOutTime = selectedClockOut,
                        note = note
                    )
                    onSave(record)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
