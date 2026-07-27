package com.xl6.opensesame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

enum class CardActionIcon {
    Chevron,
    Menu,
    Hamburger
}

class CardActionIconView(context: Context, private val icon: CardActionIcon) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiColors.Muted
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width.coerceAtMost(height).toFloat()
        val cx = width / 2f
        val cy = height / 2f

        when (icon) {
            CardActionIcon.Chevron -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.08f
                canvas.drawLine(cx - size * 0.12f, cy - size * 0.2f, cx + size * 0.1f, cy, paint)
                canvas.drawLine(cx + size * 0.1f, cy, cx - size * 0.12f, cy + size * 0.2f, paint)
            }
            CardActionIcon.Menu -> {
                paint.style = Paint.Style.FILL
                val radius = size * 0.045f
                canvas.drawCircle(cx, cy - size * 0.16f, radius, paint)
                canvas.drawCircle(cx, cy, radius, paint)
                canvas.drawCircle(cx, cy + size * 0.16f, radius, paint)
            }
            CardActionIcon.Hamburger -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.075f
                canvas.drawLine(cx - size * 0.2f, cy - size * 0.15f, cx + size * 0.2f, cy - size * 0.15f, paint)
                canvas.drawLine(cx - size * 0.2f, cy, cx + size * 0.2f, cy, paint)
                canvas.drawLine(cx - size * 0.2f, cy + size * 0.15f, cx + size * 0.2f, cy + size * 0.15f, paint)
            }
        }
    }
}
