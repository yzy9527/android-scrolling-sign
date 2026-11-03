package com.example.localsign

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class DisplayActivity : ComponentActivity() {
    private var marquee: MarqueeView? = null
    private var resumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsStore(this).load()
        if (MarqueeMath.singleLine(settings.text).isBlank()) { finish(); return }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        setContent {
            SignTheme {
                var controlsVisible by remember { mutableStateOf(true) }
                LaunchedEffect(controlsVisible) {
                    if (controlsVisible) { delay(4000); controlsVisible = false }
                }
                Box(Modifier.fillMaxSize()) {
                    AndroidView(factory = { context ->
                        MarqueeView(context).also {
                            marquee = it
                            it.configure(settings)
                            it.active = resumed
                            it.setOnClickListener { controlsVisible = !controlsVisible }
                        }
                    }, modifier = Modifier.fillMaxSize())
                    if (controlsVisible) {
                        Button(onClick = { finish() }, modifier = Modifier.align(Alignment.TopStart)
                            .safeDrawingPadding().padding(16.dp)) { Text("← 返回编辑") }
                    }
                }
            }
        }
    }
    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    override fun onResume() {
        super.onResume()
        resumed = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        marquee?.active = true
        hideSystemBars()
    }
    override fun onPause() {
        resumed = false
        marquee?.active = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }
    override fun onDestroy() { marquee?.active = false; marquee = null; super.onDestroy() }
}
