package com.xldev.opensesame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object UiColors {
    var Bg: Int = Color.rgb(245, 246, 248)
    var Card: Int = Color.WHITE
    var Text: Int = Color.rgb(9, 24, 43)
    var Muted: Int = Color.rgb(89, 101, 119)
    var Green: Int = Color.rgb(59, 130, 246)
    var GreenSoft: Int = Color.rgb(229, 240, 255)
    var Danger: Int = Color.rgb(190, 40, 36)
    var DangerSoft: Int = Color.rgb(255, 236, 236)
    var Warning: Int = Color.rgb(181, 93, 13)
    var NeutralSoft: Int = Color.rgb(241, 243, 246)
    var BorderSoft: Int = Color.rgb(213, 218, 226)

    fun applyTheme(dark: Boolean) {
        if (dark) {
            Bg = Color.rgb(15, 18, 24)
            Card = Color.rgb(26, 31, 40)
            Text = Color.rgb(238, 242, 247)
            Muted = Color.rgb(158, 169, 184)
            Green = Color.rgb(96, 165, 250)
            GreenSoft = Color.rgb(28, 49, 74)
            Danger = Color.rgb(248, 113, 113)
            DangerSoft = Color.rgb(70, 30, 34)
            Warning = Color.rgb(251, 146, 60)
            NeutralSoft = Color.rgb(35, 42, 52)
            BorderSoft = Color.rgb(61, 71, 86)
        } else {
            Bg = Color.rgb(245, 246, 248)
            Card = Color.WHITE
            Text = Color.rgb(9, 24, 43)
            Muted = Color.rgb(89, 101, 119)
            Green = Color.rgb(59, 130, 246)
            GreenSoft = Color.rgb(229, 240, 255)
            Danger = Color.rgb(190, 40, 36)
            DangerSoft = Color.rgb(255, 236, 236)
            Warning = Color.rgb(181, 93, 13)
            NeutralSoft = Color.rgb(241, 243, 246)
            BorderSoft = Color.rgb(213, 218, 226)
        }
    }
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
