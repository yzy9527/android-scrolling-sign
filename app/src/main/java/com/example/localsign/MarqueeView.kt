package com.example.localsign

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/** Measures shaped text once, draws visible copies only, and advances by elapsed frame time.
 * No wide bitmap or fixed-width character assumptions: CJK, emoji and mixed scripts use Android shaping.
 */
class MarqueeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), Choreographer.FrameCallback {
    private val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD }
    private var settings = SignSettings()
    private var textLayout: StaticLayout? = null
    private var staticScale = 1f
    private var offset = 0.0
    private var lastFrame = 0L
    private var scheduled = false
    var active: Boolean = false
        set(value) { field = value; refreshFrames() }

    init { isClickable = true; importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES }

    fun configure(value: SignSettings) {
        settings = value
        paint.typeface = SignFonts.typeface(context, value.fontId)
        contentDescription = "${if (value.scrolling) "滚动" else "静止"}看板：${value.text}。轻触可显示退出按钮。"
        setBackgroundColor(value.background)
        rebuildLayout()
        refreshFrames()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { rebuildLayout() }

    private fun rebuildLayout() {
        if (width <= 0 || height <= 0) return
        if (!settings.scrolling) { rebuildStaticLayout(); return }
        paint.color = settings.foreground
        // Fit unusually short landscape windows without cropping tall glyphs/emoji.
        paint.textSize = min(settings.fontSize * resources.displayMetrics.density, height * 0.76f)
        val text = MarqueeMath.singleLine(settings.text)
        fun scrollingLayout(): StaticLayout {
            val measured = ceil(Layout.getDesiredWidth(text, paint).toDouble()).toInt().coerceAtLeast(1)
            return StaticLayout.Builder.obtain(text, 0, text.length, paint, measured + 2)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(true)
                .setMaxLines(1).build()
        }
        var layout = scrollingLayout()
        // Different Chinese families and fallback emoji have different ascent/descent.
        // Fit the actual layout height instead of assuming identical font metrics.
        repeat(3) {
            if (layout.height > height * 0.92f && paint.textSize > 1f) {
                paint.textSize = max(1f, paint.textSize * height * 0.90f / layout.height)
                layout = scrollingLayout()
            }
        }
        textLayout = layout
        offset = 0.0
        lastFrame = 0L
        invalidate()
    }

    private fun rebuildStaticLayout() {
        paint.color = settings.foreground
        val density = resources.displayMetrics.density
        val availableWidth = (width - min(width * 0.1f, 48f * density)).toInt().coerceAtLeast(1)
        val availableHeight = (height - min(height * 0.1f, 32f * density)).coerceAtLeast(1f)
        val text = StaticTextFit.text(settings.text)
        fun layoutAt(size: Float): StaticLayout {
            paint.textSize = size
            return StaticLayout.Builder.obtain(text, 0, text.length, paint, availableWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(true)
                .build()
        }
        fun widestLine(layout: StaticLayout): Float =
            (0 until layout.lineCount).maxOfOrNull { layout.getLineWidth(it) } ?: 0f
        val size = StaticTextFit.largestSize(settings.fontSize * density) { candidate ->
            val layout = layoutAt(candidate)
            layout.height <= availableHeight && widestLine(layout) <= availableWidth
        }
        val layout = layoutAt(size)
        textLayout = layout
        // Extreme inputs (e.g. 1000 explicit lines) can exceed even a 1px font.
        // Scale the complete layout instead of dropping lines or clipping content.
        staticScale = min(1f, min(availableHeight / layout.height.coerceAtLeast(1),
            availableWidth / max(layout.width.toFloat(), widestLine(layout)).coerceAtLeast(1f)))
        offset = 0.0
        lastFrame = 0L
        invalidate()
    }

    private fun period(): Double = (textLayout?.width ?: 1) + max(width * 0.25, 96.0 * resources.displayMetrics.density)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layout = textLayout ?: return
        if (!settings.scrolling) {
            val save = canvas.save()
            canvas.translate((width - layout.width * staticScale) / 2f,
                (height - layout.height * staticScale) / 2f)
            canvas.scale(staticScale, staticScale)
            layout.draw(canvas)
            canvas.restoreToCount(save)
            return
        }
        val period = period()
        // Start at the right edge. Repeated copies enter without an abrupt reset.
        var x = width.toDouble() - offset
        while (x > 0) x -= period
        while (x < width) {
            if (x + layout.width >= 0) {
                val save = canvas.save()
                canvas.translate(x.toFloat(), (height - layout.height) / 2f)
                layout.draw(canvas)
                canvas.restoreToCount(save)
            }
            x += period
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        scheduled = false
        if (!settings.scrolling || !active || !isAttachedToWindow || windowVisibility != VISIBLE) return
        if (lastFrame != 0L) offset = MarqueeMath.advance(offset, (frameTimeNanos - lastFrame) / 1e9,
            settings.speed * resources.displayMetrics.density.toDouble(), period())
        lastFrame = frameTimeNanos
        invalidate()
        scheduleFrame()
    }

    private fun scheduleFrame() {
        if (settings.scrolling && !scheduled && active && isAttachedToWindow && windowVisibility == VISIBLE) {
            scheduled = true
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
    private fun refreshFrames() {
        Choreographer.getInstance().removeFrameCallback(this)
        scheduled = false
        lastFrame = 0L
        scheduleFrame()
    }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); refreshFrames() }
    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this)
        scheduled = false
        lastFrame = 0L
        super.onDetachedFromWindow()
    }
    override fun onWindowVisibilityChanged(visibility: Int) { super.onWindowVisibilityChanged(visibility); refreshFrames() }
    override fun performClick(): Boolean { super.performClick(); return true }
}
