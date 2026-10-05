package com.xldev.opensesame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

enum class StatusKind {
    Ready,
    Opening,
    Success,
    Error
}

class StatusIconView(context: Context) : View(context) {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiColors.Card
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var kind = StatusKind.Ready

    fun setKind(next: StatusKind) {
        kind = next
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width.coerceAtMost(height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size * 0.42f

        fillPaint.color = when (kind) {
            StatusKind.Ready -> UiColors.Green
            StatusKind.Opening -> UiColors.Warning
            StatusKind.Success -> UiColors.Green
            StatusKind.Error -> UiColors.Danger
        }
        canvas.drawCircle(cx, cy, radius, fillPaint)

        markPaint.strokeWidth = size * 0.1f
        when (kind) {
            StatusKind.Ready -> canvas.drawCircle(cx, cy, radius * 0.34f, markPaint)
            StatusKind.Opening -> {
                markPaint.style = Paint.Style.STROKE
                val rect = RectF(cx - radius * 0.45f, cy - radius * 0.45f, cx + radius * 0.45f, cy + radius * 0.45f)
                canvas.drawArc(rect, -80f, 260f, false, markPaint)
                markPaint.style = Paint.Style.FILL
            }
            StatusKind.Success -> {
                canvas.drawLine(cx - radius * 0.45f, cy, cx - radius * 0.12f, cy + radius * 0.32f, markPaint)
                canvas.drawLine(cx - radius * 0.12f, cy + radius * 0.32f, cx + radius * 0.48f, cy - radius * 0.38f, markPaint)
            }
            StatusKind.Error -> {
                canvas.drawLine(cx - radius * 0.35f, cy - radius * 0.35f, cx + radius * 0.35f, cy + radius * 0.35f, markPaint)
                canvas.drawLine(cx + radius * 0.35f, cy - radius * 0.35f, cx - radius * 0.35f, cy + radius * 0.35f, markPaint)
            }
        }
    }
}
