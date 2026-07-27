package com.xl6.opensesame

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView

data class SheetAction(
    val label: String,
    val destructive: Boolean = false,
    val action: () -> Unit
)

fun MainActivity.showBottomSheet(title: String, actions: List<SheetAction>) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(12), dp(18), dp(18))
        background = rounded(UiColors.Card, dp(24))
    }

    content.addView(ViewGroupHandle(this), LinearLayout.LayoutParams(dp(42), dp(5)).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        setMargins(0, 0, 0, dp(16))
    })

    content.addView(TextView(this).apply {
        text = title
        textSize = 18f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        includeFontPadding = false
        setPadding(0, 0, 0, dp(8))
    })

    actions.forEach { item ->
        content.addView(TextView(this).apply {
            text = item.label
            textSize = 16f
            setTextColor(if (item.destructive) UiColors.Danger else UiColors.Text)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(14), 0, dp(14))
            setOnClickListener {
                dialog.dismiss()
                item.action()
            }
        }, LinearLayout.LayoutParams(-1, -2))
    }

    dialog.setContentView(content)
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
    }
    dialog.show()
    dialog.window?.apply {
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
    }
}

private class ViewGroupHandle(context: android.content.Context) : android.view.View(context) {
    init {
        background = rounded(UiColors.BorderSoft, context.dp(3))
    }
}
