package com.xl6.opensesame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object UiColors {
    val Bg: Int = Color.rgb(244, 241, 234)
    val Card: Int = Color.WHITE
    val Text: Int = Color.rgb(31, 41, 51)
    val Muted: Int = Color.rgb(105, 115, 134)
    val Green: Int = Color.rgb(31, 122, 90)
    val GreenSoft: Int = Color.rgb(232, 243, 238)
    val Danger: Int = Color.rgb(180, 35, 24)
    val DangerSoft: Int = Color.rgb(253, 232, 232)
    val Warning: Int = Color.rgb(181, 71, 8)
    val NeutralSoft: Int = Color.rgb(246, 247, 249)
    val BorderSoft: Int = Color.rgb(220, 226, 232)
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun rounded(color: Int, radius: Int): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }
}

fun roundedStroke(color: Int, strokeColor: Int, radius: Int, strokeWidth: Int): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        setStroke(strokeWidth, strokeColor)
    }
}

fun Context.actionRow(vararg views: TextView): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        views.forEachIndexed { index, view ->
            addView(view, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                val left = if (index == 0) 0 else dp(5)
                val right = if (index == views.lastIndex) 0 else dp(5)
                setMargins(left, 0, right, 0)
            })
        }
    }
}

fun Context.quietAction(label: String, onClick: () -> Unit): TextView {
    return actionButton(label, UiColors.Muted, UiColors.NeutralSoft, onClick)
}

fun Context.dangerAction(label: String, onClick: () -> Unit): TextView {
    return actionButton(label, UiColors.Danger, UiColors.DangerSoft, onClick)
}

private fun Context.actionButton(
    label: String,
    textColor: Int,
    backgroundColor: Int,
    onClick: () -> Unit
): TextView {
    return TextView(this).apply {
        text = label
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER
        setTextColor(textColor)
        background = roundedStroke(backgroundColor, UiColors.BorderSoft, dp(12), dp(1))
        setPadding(dp(4), 0, dp(4), 0)
        setOnClickListener { onClick() }
    }
}
