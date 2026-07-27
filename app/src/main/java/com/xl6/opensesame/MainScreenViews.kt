package com.xl6.opensesame

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

fun MainActivity.headerView(onHelp: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        addView(TextView(this@headerView).apply {
            text = "Open-Sesame"
            textSize = 30f
            setTextColor(UiColors.Text)
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(TextView(this@headerView).apply {
            text = "?"
            textSize = 19f
            setTextColor(UiColors.Text)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            background = roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(22), dp(1))
            setOnClickListener { onHelp() }
        }, LinearLayout.LayoutParams(dp(44), dp(44)))
    }
}

fun MainActivity.statusBarView(
    isOpening: Boolean,
    bindStatusIcon: (StatusIconView) -> Unit,
    bindStatusText: (TextView) -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(10), dp(14), dp(10))
        background = rounded(UiColors.GreenSoft, dp(14))

        val icon = StatusIconView(this@statusBarView).apply {
            setKind(if (isOpening) StatusKind.Opening else StatusKind.Ready)
        }
        bindStatusIcon(icon)
        addView(icon, LinearLayout.LayoutParams(dp(24), dp(24)).apply {
            setMargins(0, 0, dp(10), 0)
        })

        val status = TextView(this@statusBarView).apply {
            text = if (isOpening) "Sending request..." else "Ready to open"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (isOpening) UiColors.Warning else UiColors.Green)
            includeFontPadding = false
        }
        bindStatusText(status)
        addView(status)
    }
}

fun MainActivity.profileCardView(
    title: String,
    value: String,
    empty: Boolean,
    emptyAction: String,
    onClick: () -> Unit,
    onAction: () -> Unit,
    onMenu: () -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(12), dp(14))
        background = rounded(UiColors.Card, dp(16))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(12))
        }

        val row = LinearLayout(this@profileCardView).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { if (empty) onAction() else onClick() }
        }

        val textBlock = LinearLayout(this@profileCardView).apply {
            orientation = LinearLayout.VERTICAL
        }

        textBlock.addView(TextView(this@profileCardView).apply {
            text = title
            textSize = 13f
            setTextColor(UiColors.Muted)
            includeFontPadding = false
        })

            textBlock.addView(TextView(this@profileCardView).apply {
                text = value
                textSize = 18f
                setTextColor(if (empty) UiColors.Muted else UiColors.Text)
                setTypeface(null, if (empty) Typeface.NORMAL else Typeface.BOLD)
                includeFontPadding = false
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
                setPadding(0, dp(7), 0, 0)
            })

            row.addView(textBlock, LinearLayout.LayoutParams(0, -2, 1f))

            if (empty) {
            row.addView(TextView(this@profileCardView).apply {
                text = emptyAction
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                background = rounded(UiColors.Green, dp(12))
                setPadding(dp(12), dp(10), dp(12), dp(10))
                    setOnClickListener { onAction() }
                })
            } else {
                val actions = LinearLayout(this@profileCardView).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(TextView(this@profileCardView).apply {
                        text = ">"
                        textSize = 22f
                        gravity = Gravity.CENTER
                        setTextColor(UiColors.Muted)
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(dp(30), dp(40)))

                    addView(TextView(this@profileCardView).apply {
                        text = "..."
                        textSize = 17f
                        gravity = Gravity.CENTER
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(UiColors.Muted)
                        includeFontPadding = false
                        setOnClickListener { onMenu() }
                    }, LinearLayout.LayoutParams(dp(44), dp(40)))
                }
                row.addView(actions, LinearLayout.LayoutParams(dp(78), dp(40)))
            }

        addView(row)
    }
}

fun MainActivity.advancedSectionView(
    onDebug: () -> Unit,
    onUpdate: () -> Unit,
    onReset: () -> Unit
): LinearLayout {
    return sectionView("Advanced") {
        addView(actionRow(
            quietAction("DEBUG") { onDebug() },
            quietAction("UPDATE") { onUpdate() },
            dangerAction("RESET") { onReset() },
        ))
    }
}

private fun MainActivity.sectionView(title: String, content: LinearLayout.() -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = rounded(UiColors.Card, dp(16))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(2), 0, dp(12))
        }

        addView(TextView(this@sectionView).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(UiColors.Text)
            includeFontPadding = false
            setPadding(0, 0, 0, dp(10))
        })

        content()
    }
}
