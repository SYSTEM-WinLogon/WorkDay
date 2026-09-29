package daka.work.day.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import daka.work.day.ThemeMode
import daka.work.day.db.DatabaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen3Settings(
    dbHelper: DatabaseHelper,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var pendingCsvText by remember { mutableStateOf<String?>(null) }

    val openLegalPage: () -> Unit = {
        val url = "https://baike.baidu.com/item/%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E5%8A%B3%E5%8A%A8%E6%B3%95/207140"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            onShowToast("无法打开劳动法网页")
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val content = pendingCsvText ?: ""
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                    outputStream.write(bom)
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }
                onShowToast("已导出到选择的位置")
            } catch (e: Exception) {
                onShowToast("导出失败：${e.message}")
            } finally {
                pendingCsvText = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen 3 Requirement: Top App Bar with Title "设置", MoreVert on right
        TopAppBar(
            title = {
                Text(
                    text = "设置",
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
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Settings Card 1: Attendance Configuration
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "劳动法相关规定",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "1. 工作时间：实行标准工时制的劳动者，每日工作时间一般不超过8小时，平均每周工作时间不超过44小时。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "2. 休息与休假：劳动者享有每周至少1天休息日，法定节假日按国家规定安排放假，常见包括元旦、春节、清明节、劳动节、中秋节和国庆节。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "3. 加班与调休：延长工作时间需依法安排并支付加班工资；特殊情况下可按规定安排调休，确保劳动者合法权益不受侵害。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "4. 计算规则：本应用按实际上下班时间自动统计工时，默认以8小时/天、44小时/周为参考，具体执行以当地政策与用人单位制度为准。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "查看《中华人民共和国劳动法》：百度百科",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openLegalPage() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Card 2: Data Management
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "数据管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val rows = dbHelper.getAllRecords()
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val toExportTime = { time: Long? ->
                                    time?.let { dateFormat.format(Date(it)) } ?: ""
                                }

                                val csvHeader = "id,date,clock_in,clock_out,note\n"
                                val csvRows = rows.joinToString(separator = "\n") { record ->
                                    listOf(
                                        record.id,
                                        record.date,
                                        toExportTime(record.clockInTime),
                                        toExportTime(record.clockOutTime),
                                        record.note.replace("\n", " ")
                                    ).joinToString(",") { value ->
                                        val escaped = value.toString().replace("\"", "\"\"")
                                        "\"$escaped\""
                                    }
                                }
                                pendingCsvText = csvHeader + csvRows
                                exportLauncher.launch("work_records_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("导出打卡记录到文件")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("清空所有打卡记录数据")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Card 3: App Version
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "主题模式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(
                                "跟随系统" to ThemeMode.AUTO,
                                "浅色" to ThemeMode.LIGHT,
                                "深色" to ThemeMode.DARK
                            )

                            options.forEach { (label, mode) ->
                                val selected = themeMode == mode
                                OutlinedButton(
                                    onClick = { onThemeModeChanged(mode) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(label, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = MaterialTheme.shapes.extraLarge, // 28dp radius
            title = { Text("确认清空") },
            text = { Text("确定要删除所有打卡记录吗？此操作无法撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        dbHelper.clearAllRecords()
                        showClearDialog = false
                        onShowToast("所有打卡记录已清空")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确认清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
