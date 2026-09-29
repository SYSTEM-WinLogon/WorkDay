package daka.work.day.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import daka.work.day.db.DatabaseHelper
import daka.work.day.model.WorkRecord
import daka.work.day.utils.WorkTimeUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen1ClockIn(
    dbHelper: DatabaseHelper,
    onOpenManualDialog: (WorkRecord?) -> Unit,
    onShowToast: (String) -> Unit
) {
    var currentTimeString by remember { mutableStateOf("") }
    var todayRecord by remember { mutableStateOf<WorkRecord?>(null) }
    var recentRecords by remember { mutableStateOf<List<WorkRecord>>(emptyList()) }

    val todayStr = remember { WorkTimeUtils.getTodayDateString() }

    fun refreshData() {
        todayRecord = dbHelper.getRecordByDate(todayStr)
        recentRecords = dbHelper.getRecordsByMonth(WorkTimeUtils.getCurrentMonthString())
    }

    LaunchedEffect(Unit) {
        refreshData()
        while (true) {
            val now = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dayOfWeek = WorkTimeUtils.getDayOfWeek(todayStr)
            currentTimeString = "${format.format(now)} $dayOfWeek"
            delay(1000)
        }
    }

    val isClockedIn = todayRecord?.clockInTime != null && todayRecord?.clockOutTime == null

    var recentOperations by remember { mutableStateOf<List<Long>>(emptyList()) }

    fun handleClockToggle() {
        val now = System.currentTimeMillis()
        val isCompletingCycle = todayRecord != null && todayRecord!!.clockInTime != null && todayRecord!!.clockOutTime == null
        val isStartingNewCycle = todayRecord == null || (todayRecord!!.clockInTime != null && todayRecord!!.clockOutTime != null)
        val recentWindow = recentOperations.filter { now - it < 10 * 60 * 1000 }

        if (isStartingNewCycle && recentWindow.size >= 3) {
            onShowToast("短时间内不能超过 3 次上下班操作")
            return
        }

        when {
            todayRecord == null -> {
                dbHelper.clockIn(todayStr, now)
                onShowToast("上班打卡成功: ${WorkTimeUtils.getCurrentTimeString()}")
            }
            todayRecord!!.clockInTime != null && todayRecord!!.clockOutTime == null -> {
                dbHelper.clockOut(todayStr, now)
                recentOperations = recentWindow + now
                onShowToast("下班打卡成功: ${WorkTimeUtils.getCurrentTimeString()}")
            }
            else -> {
                dbHelper.clockIn(todayStr, now)
                onShowToast("重新开始上班打卡: ${WorkTimeUtils.getCurrentTimeString()}")
            }
        }
        refreshData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen 1 Requirement: Top App Bar with Title "开始打卡"
        TopAppBar(
            title = {
                Text(
                    text = "开始打卡",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Screen 1 Requirement: Centered text "快速打卡" (28sp)
                Text(
                    text = "快速打卡",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time clock subtitle
                Text(
                    text = currentTimeString.ifEmpty { "加载时间中..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Screen 1 Requirement: 332x216dp Container Box (surfaceContainerHigh background, 28dp radius)
                // Inside overlay: Computer Icon Button (136dp / M3 XL size)
                Box(
                    modifier = Modifier
                        .size(width = 332.dp, height = 216.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scaleSpring"
                    )

                    // Computer Outlined/Filled Icon Button (136dp XL)
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (isClockedIn) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .border(
                                width = 3.dp,
                                color = if (isClockedIn) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable {
                                isPressed = true
                                handleClockToggle()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isClockedIn) Icons.Filled.Computer else Icons.Outlined.Computer,
                            contentDescription = "打卡切换按钮",
                            modifier = Modifier.size(64.dp),
                            tint = if (isClockedIn) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    LaunchedEffect(isPressed) {
                        if (isPressed) {
                            delay(150)
                            isPressed = false
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        todayRecord?.clockInTime != null && todayRecord?.clockOutTime != null -> "状态: 今日考勤已完成 (${todayRecord?.formattedDuration})"
                        todayRecord?.clockInTime != null -> "状态: 已打上班卡，点击图标下班"
                        else -> "状态: 未打卡，点击图标打卡"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Punch Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("上班打卡", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    todayRecord?.formattedClockIn ?: "未打卡",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("下班打卡", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    todayRecord?.formattedClockOut ?: "未打卡",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("今日工时", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    todayRecord?.formattedDuration ?: "0小时0分",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            enabled = todayRecord == null || todayRecord?.clockOutTime != null,
                            onClick = {
                                if (todayRecord != null && todayRecord!!.clockInTime != null && todayRecord!!.clockOutTime == null) {
                                    onShowToast("进行中的打卡不能编辑考勤记录")
                                } else {
                                    onOpenManualDialog(todayRecord)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("手动补卡 / 修改今日打卡", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "近期打卡记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            if (recentRecords.isEmpty()) {
                item {
                    Text(
                        text = "暂无打卡记录",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                items(recentRecords, key = { it.id }) { record ->
                    RecordCardItem(
                        record = record,
                        onEdit = {
                            if (record.clockInTime != null && record.clockOutTime == null) {
                                onShowToast("进行中的打卡不能编辑考勤记录")
                            } else {
                                onOpenManualDialog(record)
                            }
                        },
                        onDelete = {
                            dbHelper.deleteRecord(record.id)
                            refreshData()
                            onShowToast("记录已删除")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun RecordCardItem(
    record: WorkRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isActiveRecord = record.clockInTime != null && record.clockOutTime == null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${record.date} ${WorkTimeUtils.getDayOfWeek(record.date)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = record.formattedDuration,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("上班: ${record.formattedClockIn}", style = MaterialTheme.typography.bodyMedium)
                    Text("下班: ${record.formattedClockOut}", style = MaterialTheme.typography.bodyMedium)
                }

                IconButton(
                    onClick = onEdit,
                    enabled = !isActiveRecord
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = if (isActiveRecord) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
