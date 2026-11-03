package com.example.localsign

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("local_sign", Context.MODE_PRIVATE)
    fun load(): SignSettings {
        val defaults = SignSettings()
        return SignSettings(
            scrolling = prefs.getBoolean("scrolling", true),
            fontId = FontIds.normalize(prefs.getString("font_id", FontIds.DEFAULT)),
            text = prefs.getString("text", defaults.text) ?: defaults.text,
            fontSize = prefs.getFloat("size", defaults.fontSize).coerceIn(48f, 280f),
            speed = prefs.getFloat("speed", defaults.speed).coerceIn(30f, 300f),
            foreground = prefs.getInt("foreground", defaults.foreground),
            background = prefs.getInt("background", defaults.background)
        )
    }
    fun save(settings: SignSettings) {
        prefs.edit().putString("font_id", FontIds.normalize(settings.fontId)).putBoolean("scrolling", settings.scrolling).putString("text", settings.text).putFloat("size", settings.fontSize)
            .putFloat("speed", settings.speed).putInt("foreground", settings.foreground)
            .putInt("background", settings.background).apply()
    }
}
