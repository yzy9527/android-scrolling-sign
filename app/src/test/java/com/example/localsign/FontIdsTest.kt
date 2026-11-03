package com.example.localsign

import org.junit.Assert.*
import org.junit.Test

class FontIdsTest {
    @Test fun oldSettingsKeepOriginalTypeface() { assertEquals("system_bold", SignSettings().fontId) }
    @Test fun missingAndUnknownIdsFallBack() {
        assertEquals(FontIds.DEFAULT, FontIds.normalize(null))
        assertEquals(FontIds.DEFAULT, FontIds.normalize("removed_font"))
    }
    @Test fun sixDistinctStableChoices() {
        assertEquals(6, FontIds.available.size)
        assertEquals(6, FontIds.available.toSet().size)
        FontIds.available.forEach { assertEquals(it, FontIds.normalize(it)) }
    }
    @Test fun modeSwitchDoesNotChangeFont() {
        val settings = SignSettings(fontId = "long_cang", scrolling = true)
        assertEquals("long_cang", settings.copy(scrolling = false).fontId)
    }
    @Test fun fontSwitchKeepsOtherSettings() {
        val settings = SignSettings(text = "欢迎", speed = 90f, fontSize = 180f, scrolling = false)
        val changed = settings.copy(fontId = "zcool_xiao_wei")
        assertEquals(settings.text, changed.text)
        assertEquals(settings.speed, changed.speed)
        assertEquals(settings.fontSize, changed.fontSize)
        assertFalse(changed.scrolling)
    }
}
