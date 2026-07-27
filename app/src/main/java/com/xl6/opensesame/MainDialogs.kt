package com.xl6.opensesame

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
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

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(24), dp(22), dp(24), dp(16))
        background = rounded(UiColors.Card, dp(18))
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
        textSize = 15f
        setTextColor(UiColors.Text)
        setLineSpacing(dp(2).toFloat(), 1.0f)
    })

    val developerModeCheckbox = CheckBox(this).apply {
        text = getString(R.string.developer_mode)
        textSize = 14f
        setTextColor(UiColors.Text)
        isChecked = developerMode
        setPadding(0, dp(16), 0, dp(4))
    }
    content.addView(developerModeCheckbox)

    content.addView(TextView(this).apply {
        text = ReleaseInfo.displayVersion
        textSize = 12f
        setTextColor(UiColors.Muted)
        gravity = Gravity.CENTER
        setPadding(0, dp(8), 0, dp(12))
    })

    val buttons = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
    }

    buttons.addView(TextView(this).apply {
        text = getString(R.string.share)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(10), dp(14), dp(10))
        setOnClickListener {
            dialog.dismiss()
            shareReleaseLink()
        }
    })

    buttons.addView(TextView(this).apply {
        text = getString(R.string.update)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(10), dp(14), dp(10))
        setOnClickListener {
            dialog.dismiss()
            openUrl(ReleaseInfo.RELEASES_URL)
        }
    })

    buttons.addView(TextView(this).apply {
        text = getString(R.string.ok)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(10), dp(4), dp(10))
        setOnClickListener {
            developerMode = developerModeCheckbox.isChecked
            helpTapCount = 0
            dialog.dismiss()
            render()
        }
    })

    content.addView(buttons)

    dialog.setContentView(content)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout((resources.displayMetrics.widthPixels * 0.86f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
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
