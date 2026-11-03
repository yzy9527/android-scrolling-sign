package com.example.localsign

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

data class SignFont(val id: String, val label: String, val description: String, @FontRes val resource: Int? = null)

/** Fonts are bundled in the APK, never requested from a provider or a network. */
object SignFonts {
    val choices = listOf(
        SignFont(FontIds.DEFAULT, "经典黑体", "清晰醒目 · 系统字体"),
        SignFont("ma_shan_zheng", "马善政毛笔", "笔锋鲜明 · 毛笔楷书", R.font.ma_shan_zheng),
        SignFont("zcool_xiao_wei", "站酷小薇", "横细竖粗 · 宋体风格", R.font.zcool_xiao_wei),
        SignFont("zcool_kuai_le", "站酷快乐", "活泼跳跃 · 趣味手写", R.font.zcool_kuai_le),
        SignFont("zcool_qing_ke_huang_you", "站酷庆科黄油", "纤细圆润 · 艺术字体", R.font.zcool_qing_ke_huang_you),
        SignFont("long_cang", "龙藏草书", "随性连笔 · 草书风格", R.font.long_cang)
    )
    private val cached = mutableMapOf<String, Typeface>()
    @Synchronized
    fun typeface(context: Context, id: String): Typeface {
        val key = FontIds.normalize(id)
        if (key == FontIds.DEFAULT) return Typeface.DEFAULT_BOLD
        return cached.getOrPut(key) {
            val resource = requireNotNull(choices.first { it.id == key }.resource)
            requireNotNull(ResourcesCompat.getFont(context.applicationContext, resource))
        }
    }

    fun licenseText(context: Context): String = context.assets.list("font_licenses").orEmpty()
        .filter { it.endsWith(".txt", ignoreCase = true) || it.endsWith(".md", ignoreCase = true) }
        .sorted().joinToString("\n\n────────────────\n\n") { filename ->
            "$filename\n\n" + context.assets.open("font_licenses/$filename").bufferedReader().use { it.readText() }
        }
}
