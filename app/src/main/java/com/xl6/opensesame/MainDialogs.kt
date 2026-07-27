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
        title = "Door",
        actions = listOf(
            SheetAction("Choose door") { chooseDoorDialog() },
            SheetAction("Scan new door") { scanDoor() },
            SheetAction("Edit current door") { door?.let { editDoorDialog(it) } ?: addDoorDialog(null) },
            SheetAction("Delete current door", destructive = true) { deleteActiveDoor() },
        )
    )
}

internal fun MainActivity.showPlateMenu() {
    val plate = activePlate()
    showBottomSheet(
        title = "Vehicle",
        actions = listOf(
            SheetAction("Choose vehicle") { choosePlateDialog() },
            SheetAction("Add license plate") { addPlateDialog(null) },
            SheetAction("Edit current plate") { plate?.let { addPlateDialog(it) } ?: addPlateDialog(null) },
            SheetAction("Delete current plate", destructive = true) { deleteActivePlate() },
        )
    )
}

internal fun MainActivity.addDoorDialog(prefillUrl: String?) {
    val container = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), 0, dp(12), 0)
    }

    val nameInput = EditText(this).apply { hint = "Door name" }
    val urlInput = EditText(this).apply {
        hint = "https://dc.autoparkki.fi/access/..."
        setText(prefillUrl ?: "")
    }

    container.addView(nameInput)
    container.addView(urlInput)

    if (!prefillUrl.isNullOrBlank()) {
        nameInput.setText("Detecting door name...")
        Thread {
            val suggested = opener.suggestDoorName(prefillUrl)
            runOnUiThread {
                if (nameInput.text.toString() == "Detecting door name...") {
                    nameInput.setText(suggested)
                }
            }
        }.start()
    }

    AlertDialog.Builder(this)
        .setTitle("Add door")
        .setView(container)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save") { _, _ ->
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage("Invalid EuroPark access URL.", "TRY AGAIN", UiColors.Danger)
                return@setPositiveButton
            }
            val name = nameInput.text.toString().trim().ifBlank { "EuroPark (autoparkki) door" }
            val profile = DoorProfile(store.newId("door"), name, url)
            doors.add(profile)
            store.saveDoors(doors)
            store.setActiveDoorId(profile.id)
            reload()
            render()
            showMessage("Ready to open", "OPEN DOOR", UiColors.Green)
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
        .setTitle("Edit door")
        .setView(container)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save") { _, _ ->
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage("Invalid EuroPark access URL.", "TRY AGAIN", UiColors.Danger)
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
        .setTitle(if (existing == null) "Add license plate" else "Edit license plate")
        .setView(input)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save") { _, _ ->
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
        title = "Choose door",
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
        title = "Choose vehicle",
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
        .setTitle("Delete current door?")
        .setMessage("This removes the selected door URL from this phone.")
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Delete") { _, _ ->
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
        .setTitle("Delete current plate?")
        .setMessage("This removes the selected license plate from this phone.")
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Delete") { _, _ ->
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
        text = "Quick opener"
        textSize = 22f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        includeFontPadding = false
        setPadding(0, 0, 0, dp(12))
    })

    content.addView(TextView(this).apply {
        text = "Save an authorized door and vehicle locally. Tap OPEN DOOR to send the request.\n\nAlways verify the physical door."
        textSize = 15f
        setTextColor(UiColors.Text)
        setLineSpacing(dp(2).toFloat(), 1.0f)
    })

    val developerModeCheckbox = CheckBox(this).apply {
        text = "Developer mode"
        textSize = 14f
        setTextColor(UiColors.Text)
        isChecked = developerMode
        setPadding(0, dp(16), 0, dp(4))
    }
    content.addView(developerModeCheckbox)

    content.addView(TextView(this).apply {
        text = "v0.3.7"
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
        text = "UPDATE"
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(10), dp(14), dp(10))
        setOnClickListener {
            dialog.dismiss()
            openUrl("https://github.com/Xiaolong-6/Open-sesame/releases")
        }
    })

    buttons.addView(TextView(this).apply {
        text = "OK"
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
        appendLine("Version: 0.3.7")
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
            .setPositiveButton("OK", null)
            .show()
        return
    }

    AlertDialog.Builder(this)
        .setTitle("Debug")
        .setMessage(baseInfo + "\nCurrent door: selected\nFetching current door webpage info...")
        .setPositiveButton("OK", null)
        .show()

    Thread {
        val pageInfo = opener.debugAccessInfo(door.accessUrl)
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Debug")
                .setMessage(baseInfo + "\nCurrent door: selected\n\n" + pageInfo)
                .setPositiveButton("OK", null)
                .show()
        }
    }.start()
}

internal fun MainActivity.clearAll() {
    AlertDialog.Builder(this)
        .setTitle("Reset app data?")
        .setMessage("This deletes all local door URLs and license plates from this phone.")
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Reset") { _, _ ->
            store.clearAll()
            reload()
            lastOpenedAt = null
            render()
        }
        .show()
}
