package daka.work.day

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModePreferenceTest {
    @Test
    fun themeModeFromPreference_should_restore_last_selected_mode() {
        assertEquals(ThemeMode.AUTO, ThemeMode.fromPreference(null))
        assertEquals(ThemeMode.AUTO, ThemeMode.fromPreference(ThemeMode.AUTO.name))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromPreference(ThemeMode.LIGHT.name))
        assertEquals(ThemeMode.DARK, ThemeMode.fromPreference(ThemeMode.DARK.name))
    }
}
