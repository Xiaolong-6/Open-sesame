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
            text = getString(R.string.app_name)
            textSize = 27f
            setTextColor(UiColors.Text)
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(CardActionIconView(this@headerView, CardActionIcon.Hamburger).apply {
            background = roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(20), dp(1))
            setOnClickListener { onHelp() }
        }, LinearLayout.LayoutParams(dp(40), dp(40)))
    }
}

fun MainActivity.doorSummaryCardView(
    value: String,
    empty: Boolean,
    emptyAction: String,
    onClick: () -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(20), dp(22), dp(20), dp(20))
        background = rounded(UiColors.Card, dp(7))
        elevation = dp(1).toFloat()
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(14))
        }

        addView(LinearLayout(this@doorSummaryCardView).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val textBlock = LinearLayout(this@doorSummaryCardView).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            textBlock.addView(TextView(this@doorSummaryCardView).apply {
                text = getString(R.string.door)
                textSize = if (empty) 15f else 13f
                setTextColor(UiColors.Muted)
                setTypeface(null, if (empty) Typeface.BOLD else Typeface.NORMAL)
                gravity = Gravity.CENTER
                includeFontPadding = false
            })

            textBlock.addView(TextView(this@doorSummaryCardView).apply {
                text = if (empty) emptyAction else value
                textSize = if (empty) 17f else 19f
                setTextColor(if (empty) Color.WHITE else UiColors.Text)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                includeFontPadding = false
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
                if (empty) {
                    background = rounded(UiColors.Green, dp(4))
                    setPadding(dp(12), dp(14), dp(12), dp(14))
                } else {
                    setPadding(0, dp(7), 0, 0)
                }
            }, LinearLayout.LayoutParams(-1, -2).apply {
                if (empty) {
                    setMargins(0, dp(16), 0, 0)
                }
            })

            addView(textBlock, LinearLayout.LayoutParams(-1, -2))
        })
    }
}

fun MainActivity.openPanelCardView(
    plateValue: String,
    plateEmpty: Boolean,
    isOpening: Boolean,
    bindOpenButton: (TextView) -> Unit,
    onOpen: () -> Unit,
    emptyAction: String,
    onPlateClick: () -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(22), dp(20), dp(20))
        background = rounded(UiColors.Card, dp(7))
        elevation = dp(1).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(14))
        }

        addView(TextView(this@openPanelCardView).apply {
            text = getString(R.string.registration_number)
            textSize = 15f
            setTextColor(UiColors.Text)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }, LinearLayout.LayoutParams(-1, -2))

        addView(TextView(this@openPanelCardView).apply {
            text = if (plateEmpty) emptyAction else plateValue
            textSize = 22f
            setTextColor(if (plateEmpty) UiColors.Green else UiColors.Text)
            setTypeface(null, if (plateEmpty) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER
            includeFontPadding = false
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            background = roundedStroke(UiColors.Card, UiColors.Muted, dp(4), dp(1))
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { onPlateClick() }
        }, LinearLayout.LayoutParams(-1, dp(72)).apply {
            setMargins(0, dp(20), 0, dp(14))
        })

        val button = TextView(this@openPanelCardView).apply {
            text = if (isOpening) getString(R.string.opening) else getString(R.string.open_door)
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            background = rounded(UiColors.Green, dp(4))
            setPadding(dp(10), dp(16), dp(10), dp(16))
            isEnabled = !isOpening
            alpha = if (isOpening) 0.72f else 1f
            setOnClickListener { onOpen() }
        }
        bindOpenButton(button)
        addView(button, LinearLayout.LayoutParams(-1, -2))
    }
}

fun MainActivity.statusInfoCardView(
    isOpening: Boolean,
    lastOpenedAt: String?,
    bindStatusIcon: (StatusIconView) -> Unit,
    bindStatusText: (TextView) -> Unit,
    bindMessageText: (TextView) -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(18), dp(18), dp(18), dp(18))
        background = rounded(UiColors.Card, dp(7))
        elevation = dp(1).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(6))
        }

        addView(LinearLayout(this@statusInfoCardView).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            val icon = StatusIconView(this@statusInfoCardView).apply {
                setKind(if (isOpening) StatusKind.Opening else StatusKind.Ready)
            }
            bindStatusIcon(icon)
            addView(icon, LinearLayout.LayoutParams(dp(22), dp(22)).apply {
                setMargins(0, 0, dp(10), 0)
            })

            val status = TextView(this@statusInfoCardView).apply {
                text = if (isOpening) getString(R.string.sending_request) else getString(R.string.ready_to_open)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isOpening) UiColors.Warning else UiColors.Green)
                includeFontPadding = false
            }
            bindStatusText(status)
            addView(status)
        })

        val message = TextView(this@statusInfoCardView).apply {
            text = lastOpenedAt?.let { getString(R.string.last_opened_successfully_at, it) } ?: getString(R.string.scan_once_open_anytime)
            textSize = 13f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(0, dp(14), 0, 0)
        }
        bindMessageText(message)
        addView(message)
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
            text = if (isOpening) getString(R.string.sending_request) else getString(R.string.ready_to_open)
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

                addView(CardActionIconView(this@profileCardView, CardActionIcon.Chevron), LinearLayout.LayoutParams(dp(30), dp(40)))

                addView(CardActionIconView(this@profileCardView, CardActionIcon.Menu).apply {
                    background = roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(20), dp(1))
                    setOnClickListener { onMenu() }
                }, LinearLayout.LayoutParams(dp(40), dp(40)).apply {
                    setMargins(dp(4), 0, 0, 0)
                })
            }
            row.addView(actions, LinearLayout.LayoutParams(dp(78), dp(40)))
        }

        addView(row)
    }
}

fun MainActivity.advancedSectionView(
    onDebug: () -> Unit,
    onLanguage: () -> Unit,
    onUpdate: () -> Unit,
    onReset: () -> Unit
): LinearLayout {
    return sectionView(getString(R.string.advanced)) {
        addView(actionRow(
            quietAction(getString(R.string.debug)) { onDebug() },
            quietAction(getString(R.string.update)) { onUpdate() },
            dangerAction(getString(R.string.reset)) { onReset() },
        ))
        addView(actionRow(
            quietAction(getString(R.string.language)) { onLanguage() },
        ), LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(10), 0, 0)
        })
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
