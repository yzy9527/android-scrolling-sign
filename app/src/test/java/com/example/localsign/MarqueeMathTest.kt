package com.example.localsign

import org.junit.Assert.*
import org.junit.Test

class MarqueeMathTest {
    @Test fun newlinesAndTabsBecomeSpaces() {
        assertEquals("你好 👋 世界", MarqueeMath.singleLine(" 你好\n\t👋\r\n世界 "))
    }
    @Test fun blankInputIsRejected() { assertTrue(MarqueeMath.singleLine(" \n\t　").isBlank()) }
    @Test fun emojiAndLongTextArePreserved() {
        val text = "你好👨‍👩‍👧‍👦🚀".repeat(100)
        assertEquals(text, MarqueeMath.singleLine(text))
    }
    @Test fun ordinaryFrameAdvancesByTime() {
        assertEquals(12.0, MarqueeMath.advance(10.0, 1.0 / 60, 120.0, 1000.0), 0.00001)
    }
    @Test fun wrapsSeamlessly() {
        assertEquals(1.0, MarqueeMath.advance(999.0, 1.0 / 60, 120.0, 1000.0), 0.00001)
    }
    @Test fun slowFramesStillMaintainSpeed() {
        assertEquals(12.0, MarqueeMath.advance(0.0, 0.1, 120.0, 1000.0), 0.00001)
    }
    @Test fun invalidPeriodDoesNotDivideByZero() {
        assertEquals(0.0, MarqueeMath.advance(1.0, 0.1, 120.0, 0.0), 0.0)
    }
    @Test fun frameRateDoesNotChangeSpeed() {
        fun simulate(fps: Int): Double {
            var offset = 0.0
            repeat(fps * 10) { offset = MarqueeMath.advance(offset, 1.0 / fps, 120.0, 500.0) }
            return offset
        }
        assertEquals(simulate(60), simulate(120), 0.00001)
    }
}
