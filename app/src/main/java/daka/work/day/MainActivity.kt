package daka.work.day

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import daka.work.day.db.DatabaseHelper
import daka.work.day.model.WorkRecord
import daka.work.day.theme.AppTheme
import daka.work.day.ui.ManualEntryDialog
import daka.work.day.ui.Screen1ClockIn
import daka.work.day.ui.Screen2Calendar
import daka.work.day.ui.Screen3Settings

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DatabaseHelper(this)

        setContent {
            MainAppScreen(
                dbHelper = dbHelper,
                initialDarkTheme = isSystemInDarkTheme(),
                onShowToast = { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK;

    companion object {
        fun fromPreference(value: String?): ThemeMode {
            return when (value) {
                LIGHT.name -> LIGHT
                DARK.name -> DARK
                else -> AUTO
            }
        }
    }

    fun toPreference(): String = this.name
}

object ThemePreferenceStore {
    private const val PREFS_NAME = "app_theme_settings"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ThemeMode.fromPreference(prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name))
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.toPreference()).apply()
    }
}

@Composable
fun MainAppScreen(
    dbHelper: DatabaseHelper,
    initialDarkTheme: Boolean = false,
    onShowToast: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    var editingRecord by remember { mutableStateOf<WorkRecord?>(null) }
    var showManualDialog by remember { mutableStateOf(false) }
    var themeMode by rememberSaveable {
        mutableStateOf(ThemePreferenceStore.getThemeMode(context))
    }
    val systemDarkTheme = isSystemInDarkTheme()
    val darkThemeEnabled = when (themeMode) {
        ThemeMode.AUTO -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    AppTheme(darkTheme = darkThemeEnabled) {
        val navItems = listOf(
            NavItem("打卡", Icons.Filled.Home, Icons.Outlined.Home),
            NavItem("日期", Icons.Filled.DateRange, Icons.Outlined.DateRange),
            NavItem("设置", Icons.Filled.Settings, Icons.Outlined.Settings)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedScreenIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedScreenIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AnimatedContent(
                    targetState = selectedScreenIndex,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) togetherWith fadeOut(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    },
                    label = "ScreenTransition"
                ) { targetState ->
                    when (targetState) {
                        0 -> Screen1ClockIn(
                            dbHelper = dbHelper,
                            onOpenManualDialog = { record ->
                                editingRecord = record
                                showManualDialog = true
                            },
                            onShowToast = onShowToast
                        )

                        1 -> Screen2Calendar(
                            dbHelper = dbHelper,
                            onOpenManualDialog = { record ->
                                editingRecord = record
                                showManualDialog = true
                            },
                            onOpenDateDetail = { /* date detail removed */ },
                            onShowToast = onShowToast
                        )

                        else -> Screen3Settings(
                            dbHelper = dbHelper,
                            themeMode = themeMode,
                            onThemeModeChanged = {
                                themeMode = it
                                ThemePreferenceStore.saveThemeMode(context, it)
                            },
                            onShowToast = onShowToast
                        )
                    }
                }
            }
        }

        if (showManualDialog) {
            ManualEntryDialog(
                existingRecord = editingRecord,
                onDismiss = {
                    showManualDialog = false
                    editingRecord = null
                },
                onSave = { recordToSave ->
                    dbHelper.saveOrUpdateRecord(recordToSave)
                    showManualDialog = false
                    editingRecord = null
                    onShowToast("考勤记录已保存")
                }
            )
        }
    }
}
