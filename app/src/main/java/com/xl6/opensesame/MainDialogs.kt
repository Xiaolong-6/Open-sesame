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

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(22), dp(22), dp(22), dp(14))
        background = rounded(UiColors.Card, dp(20))
    }

    content.addView(TextView(this).apply {
        text = getString(R.string.quick_opener)
        textSize = 22f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        includeFontPadding = false
        setPadding(0, 0, 0, dp(12))
    })

    content.addView(TextView(this).apply {
        text = getString(R.string.help_body)
        textSize = 14f
        setTextColor(UiColors.Muted)
        setLineSpacing(dp(2).toFloat(), 1.0f)
    })

    val developerCheck = TextView(this)
    fun updateDeveloperCheck() {
        developerCheck.text = if (developerChecked) "ON" else ""
        developerCheck.setTextColor(Color.WHITE)
        developerCheck.gravity = Gravity.CENTER
        developerCheck.textSize = 15f
        developerCheck.setTypeface(null, Typeface.BOLD)
        developerCheck.background = if (developerChecked) {
            rounded(UiColors.Green, dp(6))
        } else {
            roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(6), dp(2))
        }
    }
    updateDeveloperCheck()

    val actionList = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(14), 0, dp(8))
    }
    actionList.addView(helpActionRow(getString(R.string.share_open_sesame)) {
        developerMode = developerChecked
        store.setDeveloperMode(developerChecked)
        helpTapCount = 0
        dialog.dismiss()
        showShareMenu()
    })
    actionList.addView(helpActionRow(getString(R.string.open_releases_page)) {
        developerMode = developerChecked
        store.setDeveloperMode(developerChecked)
        helpTapCount = 0
        dialog.dismiss()
        openUrl(ReleaseInfo.RELEASES_URL)
    })
    actionList.addView(helpToggleRow(getString(R.string.developer_mode), developerCheck) {
        developerChecked = !developerChecked
        updateDeveloperCheck()
    })
    content.addView(actionList)

    val footer = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(6), 0, 0)
    }

    footer.addView(TextView(this).apply {
        text = ReleaseInfo.displayVersion
        textSize = 12f
        setTextColor(UiColors.Muted)
        gravity = Gravity.CENTER_VERTICAL
    }, LinearLayout.LayoutParams(0, -2, 1f))

    footer.addView(TextView(this).apply {
        text = getString(R.string.close)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(16), dp(10), 0, dp(10))
        setOnClickListener {
            developerMode = developerChecked
            store.setDeveloperMode(developerChecked)
            helpTapCount = 0
            dialog.dismiss()
            render()
        }
    })
    content.addView(footer)

    dialog.setContentView(content)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout((resources.displayMetrics.widthPixels * 0.86f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

private fun MainActivity.helpActionRow(label: String, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(12), 0, dp(12))
        setOnClickListener { onClick() }

        addView(TextView(this@helpActionRow).apply {
            text = label
            textSize = 15f
            setTextColor(UiColors.Text)
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(TextView(this@helpActionRow).apply {
            text = ">"
            textSize = 24f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }, LinearLayout.LayoutParams(dp(28), dp(28)))
    }
}

private fun MainActivity.helpToggleRow(label: String, checkView: TextView, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(12), 0, dp(12))
        setOnClickListener { onClick() }

        addView(TextView(this@helpToggleRow).apply {
            text = label
            textSize = 15f
            setTextColor(UiColors.Text)
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(checkView, LinearLayout.LayoutParams(dp(24), dp(24)))
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
