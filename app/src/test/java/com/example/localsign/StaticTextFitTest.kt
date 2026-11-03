package com.example.localsign

import org.junit.Assert.*
import org.junit.Test

class StaticTextFitTest {
    @Test fun newInstallDefaultsToScrolling() { assertTrue(SignSettings().scrolling) }
    @Test fun staticModeCanBeSelectedWithoutChangingText() {
        val original = SignSettings(text = "你好")
        assertEquals("你好", original.copy(scrolling = false).text)
        assertFalse(original.copy(scrolling = false).scrolling)
    }
    @Test fun preservesExplicitLinesAndEmoji() {
        assertEquals("你好\n世界 👋\nHello", StaticTextFit.text(" 你好\r\n世界 👋\rHello "))
    }
    @Test fun shortTextKeepsRequestedSize() {
        assertEquals(160f, StaticTextFit.largestSize(160f) { true }, 0f)
    }
    @Test fun overflowFindsLargestFittingSize() {
        val size = StaticTextFit.largestSize(280f) { it <= 42f }
        assertTrue(size <= 42f)
        assertTrue(size > 41.99f)
    }
    @Test fun extremeOverflowReturnsPositiveFallback() {
        assertEquals(1f, StaticTextFit.largestSize(280f) { false }, 0f)
    }
    @Test fun neverEnlargesBeyondRequestedSize() {
        assertEquals(48f, StaticTextFit.largestSize(48f) { it <= 200f }, 0f)
    }
}
