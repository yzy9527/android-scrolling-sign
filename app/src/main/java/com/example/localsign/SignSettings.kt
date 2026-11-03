package com.example.localsign

/** All values are local. Sizes and speed use dp, independent of system text scaling. */
data class SignSettings(
    val scrolling: Boolean = true,
    val fontId: String = FontIds.DEFAULT,
    val text: String = "你好，世界！ 👋",
    val fontSize: Float = 160f,
    val speed: Float = 120f,
    val foreground: Int = 0xFFB9F46B.toInt(),
    val background: Int = 0xFF000000.toInt()
)

object MarqueeMath {
    const val MAX_TEXT_LENGTH = 2000
    fun singleLine(text: String): String = text.replace(Regex("[\\r\\n\\t]+"), " ").trim()
    fun advance(offset: Double, deltaSeconds: Double, pixelsPerSecond: Double, period: Double): Double {
        if (period <= 0.0) return 0.0
        return (offset + deltaSeconds.coerceAtLeast(0.0) * pixelsPerSecond) % period
    }
}

/** Bounded layout search; callers measure real shaped lines, not character counts. */
object StaticTextFit {
    fun largestSize(requested: Float, fits: (Float) -> Boolean): Float {
        val upper = requested.coerceAtLeast(1f)
        if (fits(upper)) return upper
        var low = 1f
        var high = upper
        repeat(16) {
            val middle = (low + high) / 2f
            if (fits(middle)) low = middle else high = middle
        }
        return low
    }
    fun text(value: String): String = value.replace("\r\n", "\n").replace('\r', '\n').replace('\t', ' ').trim()
}

/** Persist stable identifiers rather than labels or menu positions. */
object FontIds {
    const val DEFAULT = "system_bold"
    val available = listOf(DEFAULT, "ma_shan_zheng", "zcool_xiao_wei", "zcool_kuai_le",
        "zcool_qing_ke_huang_you", "long_cang")
    fun normalize(id: String?): String = id?.takeIf { it in available } ?: DEFAULT
}
