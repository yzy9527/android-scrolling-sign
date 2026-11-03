package com.example.localsign

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val Ink = Color(0xFF10151F)
private val Lime = Color(0xFFB9F46B)

@Composable
fun SignTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Lime, onPrimary = Ink,
        background = Ink, surface = Color(0xFF1A2230), onSurface = Color(0xFFF1F4F8)), content = content)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SettingsStore(this)
        setContent {
            SignTheme {
                var settings by remember { mutableStateOf(store.load()) }
                fun update(value: SignSettings) { settings = value; store.save(value) }
                Editor(settings, ::update) {
                    store.save(settings)
                    startActivity(Intent(this, DisplayActivity::class.java))
                }
            }
        }
    }
}

@Composable
private fun Editor(settings: SignSettings, update: (SignSettings) -> Unit, start: () -> Unit) {
    val valid = MarqueeMath.singleLine(settings.text).isNotBlank()
    val context = LocalContext.current
    val selectedFamily = remember(settings.fontId) { FontFamily(SignFonts.typeface(context, settings.fontId)) }
    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.safeDrawingPadding().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OFFLINE · 只在你的手机里", color = Lime, fontSize = 12.sp, letterSpacing = 2.sp)
                Text("让文字，大声说。", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("输入一句话，变成横屏大字看板。", color = Color(0xFF9DAABD))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("展示方式", fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(selected = settings.scrolling, onClick = { update(settings.copy(scrolling = true)) },
                        label = { Text("滚动") })
                    FilterChip(selected = !settings.scrolling, onClick = { update(settings.copy(scrolling = false)) },
                        label = { Text("静止") })
                }
                Text(if (settings.scrolling) "从右向左，连续循环。" else "文字居中，自动换行；放不下时自动缩小，完整显示。",
                    color = Color(0xFF9DAABD), fontSize = 12.sp)
            }
            OutlinedTextField(value = settings.text, onValueChange = {
                if (it.length <= MarqueeMath.MAX_TEXT_LENGTH) update(settings.copy(text = it))
            }, modifier = Modifier.fillMaxWidth(), label = { Text("想展示的文字") },
                minLines = 3, maxLines = 6, isError = !valid,
                supportingText = { Text(if (!valid) "请输入文字，不能只有空格。" else "${settings.text.length} / 2000 · ${if (settings.scrolling) "换行会按空格滚动" else "保留换行，长文可能缩得很小"}") },
                shape = RoundedCornerShape(16.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = Color(settings.background)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("字体与颜色预览", color = Color(settings.foreground).copy(alpha = 0.65f), fontSize = 12.sp)
                    Text(MarqueeMath.singleLine(settings.text).ifBlank { "输入你的文字" },
                        color = Color(settings.foreground), fontSize = 30.sp, fontFamily = selectedFamily,
                        maxLines = 1, modifier = Modifier.fillMaxWidth())
                }
            }
            FontChoices(settings.fontId) { update(settings.copy(fontId = it)) }
            Column {
                SettingSlider("字号", "${settings.fontSize.roundToInt()} dp", settings.fontSize, 48f..280f) {
                    update(settings.copy(fontSize = it))
                }
                if (settings.scrolling) SettingSlider("滚动速度", "${settings.speed.roundToInt()} dp / 秒", settings.speed, 30f..300f) {
                    update(settings.copy(speed = it))
                }
            }
            ColorChoices("文字颜色", settings.foreground,
                listOf("荧光绿" to 0xFFB9F46B, "白色" to 0xFFFFFFFF, "黄色" to 0xFFFFD75E,
                    "粉色" to 0xFFFF82BB, "天蓝" to 0xFF7CD5FF)) {
                update(settings.copy(foreground = it))
            }
            ColorChoices("背景颜色", settings.background,
                listOf("黑色" to 0xFF000000, "深蓝" to 0xFF101D3A, "暗红" to 0xFF3A1020,
                    "深绿" to 0xFF123027)) {
                update(settings.copy(background = it))
            }
            Button(onClick = start, enabled = valid, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)) {
                Text("开始横屏展示  →", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("${if (settings.scrolling) "从右向左 · 无限循环" else "静止居中 · 完整展示"}\n展示时保持亮屏，轻触屏幕显示返回按钮。文字和设置自动保存在本机。",
                color = Color(0xFF9DAABD), fontSize = 12.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun SettingSlider(label: String, valueLabel: String, value: Float, range: ClosedFloatingPointRange<Float>, change: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(valueLabel, color = Lime)
    }
    Slider(value = value, onValueChange = change, valueRange = range,
        modifier = Modifier.semantics { contentDescription = label })
}

@Composable
private fun ColorChoices(label: String, selected: Int, choices: List<Pair<String, Long>>, change: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { (name, color) ->
                Box(Modifier.size(48.dp).clickable { change(color.toInt()) }.border(if (selected == color.toInt()) 3.dp else 1.dp,
                    if (selected == color.toInt()) Lime else Color(0xFF607086), CircleShape)
                    .padding(5.dp).background(Color(color), CircleShape)
                    .semantics { contentDescription = "$label：$name${if (selected == color.toInt()) "，已选中" else ""}" },
                    contentAlignment = Alignment.Center) {
                    if (selected == color.toInt()) Text("✓", color = if (label == "文字颜色") Ink else Color.White)
                }
            }
        }
    }
}


@Composable
private fun FontChoices(selected: String, change: (String) -> Unit) {
    val context = LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("选择字体", fontWeight = FontWeight.Medium)
        Text("6 种中文风格 · 内置离线使用，生僻字与 emoji 由系统补充",
            color = Color(0xFF9DAABD), fontSize = 12.sp)
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起字体列表" else "展开全部 6 款字体")
        }
        SignFonts.choices.filter { expanded || it.id == selected }.forEach { font ->
            val family = remember(font.id) { FontFamily(SignFonts.typeface(context, font.id)) }
            val chosen = selected == font.id
            Surface(shape = RoundedCornerShape(14.dp), color = if (chosen) Color(0xFF263523) else Color(0xFF1A2230),
                modifier = Modifier.fillMaxWidth().selectable(selected = chosen, role = Role.RadioButton,
                    onClick = { change(font.id); expanded = false }).semantics { contentDescription = "${font.label}，${font.description}" }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(font.label, color = if (chosen) Lime else Color.White, fontSize = 14.sp)
                        if (chosen) Text("已选择 ✓", color = Lime, fontSize = 12.sp)
                    }
                    Text("你好，世界！大字看板", fontFamily = family, fontSize = 25.sp,
                        color = Color.White, maxLines = 2)
                    Text(font.description, color = Color(0xFF9DAABD), fontSize = 12.sp)
                }
            }
        }
        TextButton(onClick = { showLicenses = true }) { Text("字体许可与来源") }
    }
    if (showLicenses) {
        val notices = remember { SignFonts.licenseText(context) }
        AlertDialog(onDismissRequest = { showLicenses = false }, title = { Text("字体许可与来源") },
            text = { Text(notices, modifier = Modifier.heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()), fontSize = 12.sp) },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text("关闭") } })
    }
}
