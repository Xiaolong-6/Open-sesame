package com.xl6.opensesame

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

internal fun MainActivity.showDoorMenu() {
    val door = activeDoor()
    showBottomSheet(
        title = getString(R.string.door),
        actions = listOf(
            SheetAction(getString(R.string.choose_door)) { chooseDoorDialog() },
            SheetAction(getString(R.string.scan_new_door)) { scanDoor() },
            SheetAction(getString(R.string.edit_current_door)) { door?.let { editDoorDialog(it) } ?: addDoorDialog(null) },
            SheetAction(getString(R.string.delete_current_door), destructive = true) { deleteActiveDoor() },
        )
    )
}

internal fun MainActivity.showPlateMenu() {
    val plate = activePlate()
    showBottomSheet(
        title = getString(R.string.vehicle),
        actions = listOf(
            SheetAction(getString(R.string.choose_vehicle)) { choosePlateDialog() },
            SheetAction(getString(R.string.add_license_plate)) { addPlateDialog(null) },
            SheetAction(getString(R.string.edit_current_plate)) { plate?.let { addPlateDialog(it) } ?: addPlateDialog(null) },
            SheetAction(getString(R.string.delete_current_plate), destructive = true) { deleteActivePlate() },
        )
    )
}

internal fun MainActivity.addDoorDialog(prefillUrl: String?) {
    val container = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), 0, dp(12), 0)
    }

    val nameInput = EditText(this).apply { hint = getString(R.string.door_name) }
    val urlInput = EditText(this).apply {
        hint = "https://dc.autoparkki.fi/access/..."
        setText(prefillUrl ?: "")
    }

    container.addView(nameInput)
    container.addView(urlInput)

    if (!prefillUrl.isNullOrBlank()) {
        nameInput.setText(getString(R.string.detecting_door_name))
        Thread {
            val suggested = opener.suggestDoorName(prefillUrl)
            runOnUiThread {
                if (nameInput.text.toString() == getString(R.string.detecting_door_name)) {
                    nameInput.setText(suggested)
                }
            }
        }.start()
    }

    AlertDialog.Builder(this)
        .setTitle(getString(R.string.add_door))
        .setView(container)
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.save)) { _, _ ->
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage(getString(R.string.invalid_europark_access_url), getString(R.string.try_again), UiColors.Danger)
                return@setPositiveButton
            }
            val name = nameInput.text.toString().trim().ifBlank { getString(R.string.europark_door_fallback) }
            val profile = DoorProfile(store.newId("door"), name, url)
            doors.add(profile)
            store.saveDoors(doors)
            store.setActiveDoorId(profile.id)
            reload()
            render()
            showMessage(getString(R.string.ready_to_open), getString(R.string.open_door), UiColors.Green)
        }
        .show()
}

internal fun MainActivity.editDoorDialog(door: DoorProfile) {
    val container = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), 0, dp(12), 0)
    }

    val nameInput = EditText(this).apply { setText(door.name) }
    val urlInput = EditText(this).apply { setText(door.accessUrl) }

    container.addView(nameInput)
    container.addView(urlInput)

    AlertDialog.Builder(this)
        .setTitle(getString(R.string.edit_door))
        .setView(container)
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.save)) { _, _ ->
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage(getString(R.string.invalid_europark_access_url), getString(R.string.try_again), UiColors.Danger)
                return@setPositiveButton
            }
            doors = doors.map {
                if (it.id == door.id) it.copy(
                    name = nameInput.text.toString(),
                    accessUrl = url
                ) else it
            }.toMutableList()
            store.saveDoors(doors)
            reload()
            render()
        }
        .show()
}

internal fun MainActivity.addPlateDialog(existing: PlateProfile?) {
    val input = EditText(this).apply {
        hint = "ABC-123"
        setText(existing?.plateNumber ?: "")
    }

    AlertDialog.Builder(this)
        .setTitle(if (existing == null) getString(R.string.add_license_plate) else getString(R.string.edit_license_plate))
        .setView(input)
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.save)) { _, _ ->
            val plate = input.text.toString().trim().uppercase()
            if (plate.isBlank()) return@setPositiveButton

            if (existing == null) {
                val p = PlateProfile(store.newId("plate"), plate)
                plates.add(p)
                store.savePlates(plates)
                store.setActivePlateId(p.id)
            } else {
                plates = plates.map {
                    if (it.id == existing.id) it.copy(plateNumber = plate) else it
                }.toMutableList()
                store.savePlates(plates)
            }

            reload()
            render()
        }
        .show()
}

internal fun MainActivity.chooseDoorDialog() {
    if (doors.isEmpty()) {
        addDoorDialog(null)
        return
    }

    showBottomSheet(
        title = getString(R.string.choose_door),
        actions = doors.map { door ->
            SheetAction(door.name) {
                store.setActiveDoorId(door.id)
                reload()
                render()
            }
        }
    )
}

internal fun MainActivity.choosePlateDialog() {
    if (plates.isEmpty()) {
        addPlateDialog(null)
        return
    }

    showBottomSheet(
        title = getString(R.string.choose_vehicle),
        actions = plates.map { plate ->
            SheetAction(plate.plateNumber) {
                store.setActivePlateId(plate.id)
                reload()
                render()
            }
        }
    )
}

internal fun MainActivity.deleteActiveDoor() {
    val door = activeDoor() ?: return
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.delete_current_door_question))
        .setMessage(getString(R.string.delete_current_door_message))
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.delete)) { _, _ ->
            doors.removeAll { it.id == door.id }
            store.saveDoors(doors)
            store.setActiveDoorId(doors.firstOrNull()?.id)
            reload()
            render()
        }
        .show()
}

internal fun MainActivity.deleteActivePlate() {
    val plate = activePlate() ?: return
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.delete_current_plate_question))
        .setMessage(getString(R.string.delete_current_plate_message))
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.delete)) { _, _ ->
            plates.removeAll { it.id == plate.id }
            store.savePlates(plates)
            store.setActivePlateId(plates.firstOrNull()?.id)
            reload()
            render()
        }
        .show()
}

internal fun MainActivity.showInstructions() {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    var developerChecked = developerMode

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(22), dp(24), dp(22), dp(16))
        background = rounded(UiColors.Bg, 0)
    }

    val header = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 0, 0, dp(18))
    }
    header.addView(TextView(this).apply {
        text = getString(R.string.settings)
        textSize = 28f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        includeFontPadding = false
    }, LinearLayout.LayoutParams(0, -2, 1f))
    header.addView(CardActionIconView(this, CardActionIcon.Close).apply {
        background = roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(22), dp(1))
        setOnClickListener {
            developerMode = developerChecked
            store.setDeveloperMode(developerChecked)
            helpTapCount = 0
            dialog.dismiss()
            render()
        }
    }, LinearLayout.LayoutParams(dp(44), dp(44)))
    root.addView(header)

    val scroll = ScrollView(this).apply {
        clipToPadding = false
    }
    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(12))
    }
    scroll.addView(content)
    root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

    content.addView(TextView(this).apply {
        text = getString(R.string.help_body)
        textSize = 14f
        setTextColor(UiColors.Muted)
        setLineSpacing(dp(2).toFloat(), 1.0f)
        setPadding(0, 0, 0, dp(18))
    })

    val developerCheck = TextView(this)
    fun updateDeveloperCheck() {
        developerCheck.text = if (developerChecked) getString(R.string.on) else getString(R.string.off)
        developerCheck.setTextColor(if (developerChecked) UiColors.Green else UiColors.Muted)
        developerCheck.gravity = Gravity.CENTER
        developerCheck.textSize = 15f
        developerCheck.setTypeface(null, Typeface.BOLD)
        developerCheck.background = null
    }
    updateDeveloperCheck()

    content.addView(settingsSectionCard(getString(R.string.preferences)) {
        addView(settingsActionRow(getString(R.string.language)) {
            dialog.dismiss()
            showLanguageMenu()
        })
        addView(settingsActionRow(getString(R.string.theme), showDivider = false) {
            dialog.dismiss()
            showThemeMenu()
        })
    })

    content.addView(settingsSectionCard(getString(R.string.share_section)) {
        addView(settingsActionRow(getString(R.string.show_qr_code)) {
            dialog.dismiss()
            showReleaseQrDialog()
        })
        addView(settingsActionRow(getString(R.string.share_release_link), showDivider = false) {
            dialog.dismiss()
            shareReleaseLink()
        })
    })

    content.addView(settingsSectionCard(getString(R.string.about)) {
        addView(settingsActionRow(getString(R.string.open_releases_page)) {
            dialog.dismiss()
            openUrl(ReleaseInfo.RELEASES_URL)
        })
        addView(settingsStaticRow(getString(R.string.version_label, ReleaseInfo.VERSION_NAME), showDivider = false))
    })

    content.addView(settingsSectionCard(getString(R.string.developer_mode)) {
        addView(settingsToggleRow(getString(R.string.developer_mode), developerCheck, showDivider = false) {
            developerChecked = !developerChecked
            updateDeveloperCheck()
            developerMode = developerChecked
            store.setDeveloperMode(developerChecked)
            dialog.dismiss()
            render()
            showInstructions()
        })
    })

    if (developerChecked) {
        content.addView(settingsSectionCard(getString(R.string.developer_tools)) {
            addView(settingsActionRow(getString(R.string.debug)) {
                dialog.dismiss()
                debugFetch()
            })
            addView(settingsActionRow(getString(R.string.update)) {
                dialog.dismiss()
                openUrl(ReleaseInfo.RELEASES_URL)
            })
            addView(settingsActionRow(getString(R.string.reset_app_data), destructive = true, showDivider = false) {
                dialog.dismiss()
                clearAll()
            })
        })
    }

    dialog.setContentView(root)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.CENTER)
    }
}

private fun MainActivity.settingsSectionCard(title: String, content: LinearLayout.() -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(UiColors.Card, dp(14))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(14))
        }
        addView(TextView(this@settingsSectionCard).apply {
            text = title
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(UiColors.Muted)
            includeFontPadding = false
            setPadding(dp(14), dp(14), dp(14), dp(4))
        })
        content()
    }
}

private fun MainActivity.settingsActionRow(
    label: String,
    destructive: Boolean = false,
    showDivider: Boolean = true,
    onClick: () -> Unit
): LinearLayout {
    return settingsBaseRow(label, showDivider, onClick, destructive = destructive) {
        addView(CardActionIconView(this@settingsActionRow, CardActionIcon.Chevron), LinearLayout.LayoutParams(dp(28), dp(28)))
    }
}

private fun MainActivity.settingsStaticRow(label: String, showDivider: Boolean = true): LinearLayout {
    return settingsBaseRow(label, showDivider, null, muted = true)
}

private fun MainActivity.settingsToggleRow(
    label: String,
    checkView: TextView,
    showDivider: Boolean = true,
    onClick: () -> Unit
): LinearLayout {
    return settingsBaseRow(label, showDivider, onClick) {
        addView(checkView, LinearLayout.LayoutParams(dp(44), dp(28)))
    }
}

private fun MainActivity.settingsBaseRow(
    label: String,
    showDivider: Boolean,
    onClick: (() -> Unit)?,
    destructive: Boolean = false,
    muted: Boolean = false,
    trailing: (LinearLayout.() -> Unit)? = null
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        onClick?.let { click -> setOnClickListener { click() } }

        val row = LinearLayout(this@settingsBaseRow).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(11), dp(12), dp(11))
        }

        row.addView(TextView(this@settingsBaseRow).apply {
            text = label
            textSize = 15f
            setTextColor(when {
                destructive -> UiColors.Danger
                muted -> UiColors.Muted
                else -> UiColors.Text
            })
            setTypeface(null, if (muted) Typeface.NORMAL else Typeface.BOLD)
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, -2, 1f))

        trailing?.invoke(row)
        addView(row)

        if (showDivider) {
            addView(android.view.View(this@settingsBaseRow).apply {
                setBackgroundColor(UiColors.BorderSoft)
                alpha = 0.55f
            }, LinearLayout.LayoutParams(-1, dp(1)).apply {
                setMargins(dp(14), 0, dp(14), 0)
            })
        }
    }
}

internal fun MainActivity.showLanguageMenu() {
    val current = LocaleController.getLanguageOverride(this)
    fun label(name: String, language: String): String {
        return if (current == language) "$name ON" else name
    }

    showBottomSheet(
        title = getString(R.string.language),
        actions = listOf(
            SheetAction(label(getString(R.string.system_language), LocaleController.LANGUAGE_SYSTEM)) {
                setLanguageAndRestart(LocaleController.LANGUAGE_SYSTEM)
            },
            SheetAction(label(getString(R.string.english), LocaleController.LANGUAGE_ENGLISH)) {
                setLanguageAndRestart(LocaleController.LANGUAGE_ENGLISH)
            },
            SheetAction(label(getString(R.string.finnish), LocaleController.LANGUAGE_FINNISH)) {
                setLanguageAndRestart(LocaleController.LANGUAGE_FINNISH)
            },
            SheetAction(label(getString(R.string.chinese), LocaleController.LANGUAGE_CHINESE)) {
                setLanguageAndRestart(LocaleController.LANGUAGE_CHINESE)
            },
        )
    )
}

private fun MainActivity.setLanguageAndRestart(language: String) {
    LocaleController.setLanguageOverride(this, language)
    store.setDeveloperMode(true)
    recreate()
}

internal fun MainActivity.showThemeMenu() {
    val current = ThemeController.getThemeOverride(this)
    fun label(name: String, theme: String): String {
        return if (current == theme) "$name ON" else name
    }

    showBottomSheet(
        title = getString(R.string.theme),
        actions = listOf(
            SheetAction(label(getString(R.string.system_theme), ThemeController.THEME_SYSTEM)) {
                setThemeAndRestart(ThemeController.THEME_SYSTEM)
            },
            SheetAction(label(getString(R.string.light_theme), ThemeController.THEME_LIGHT)) {
                setThemeAndRestart(ThemeController.THEME_LIGHT)
            },
            SheetAction(label(getString(R.string.dark_theme), ThemeController.THEME_DARK)) {
                setThemeAndRestart(ThemeController.THEME_DARK)
            },
        )
    )
}

private fun MainActivity.setThemeAndRestart(theme: String) {
    ThemeController.setThemeOverride(this, theme)
    recreate()
}

internal fun MainActivity.debugFetch() {
    val door = activeDoor()
    val baseInfo = buildString {
        appendLine("Version: ${ReleaseInfo.VERSION_NAME}")
        appendLine("Mode: real opener")
        appendLine("Doors: ${doors.size}")
        appendLine("Plates: ${plates.size}")
        appendLine("Operator: EuroPark")
        appendLine("Legacy page handler: autoparkki")
    }

    if (door == null) {
        AlertDialog.Builder(this)
            .setTitle("Debug")
            .setMessage(baseInfo + "\nCurrent door: none selected.\nScan a EuroPark (autoparkki) door first.")
            .setPositiveButton(getString(R.string.ok), null)
            .show()
        return
    }

    AlertDialog.Builder(this)
        .setTitle("Debug")
        .setMessage(baseInfo + "\nCurrent door: selected\nFetching current door webpage info...")
        .setPositiveButton(getString(R.string.ok), null)
        .show()

    Thread {
        val pageInfo = opener.debugAccessInfo(door.accessUrl)
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Debug")
                .setMessage(baseInfo + "\nCurrent door: selected\n\n" + pageInfo)
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }.start()
}

internal fun MainActivity.clearAll() {
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.reset_app_data_question))
        .setMessage(getString(R.string.reset_app_data_message))
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.reset)) { _, _ ->
            store.clearAll()
            reload()
            lastOpenedAt = null
            render()
        }
        .show()
}
