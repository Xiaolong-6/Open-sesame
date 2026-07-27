package com.xl6.opensesame

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var store: ProfileStore
    private val opener = AutoparkkiOpener()

    private var doors = mutableListOf<DoorProfile>()
    private var plates = mutableListOf<PlateProfile>()
    private var activeDoorId: String? = null
    private var activePlateId: String? = null
    private var developerMode = false
    private var helpTapCount = 0
    private var isOpening = false
    private var lastOpenedAt: String? = null

    private lateinit var statusText: TextView
    private lateinit var messageText: TextView
    private lateinit var openButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = UiColors.Bg
        window.navigationBarColor = UiColors.Bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        store = ProfileStore(this)
        reload()
        render()
    }

    override fun onResume() {
        super.onResume()
        val scanned = QrScannerActivity.lastScannedText
        if (!scanned.isNullOrBlank()) {
            QrScannerActivity.lastScannedText = null
            val url = extractAutoparkkiUrl(scanned)
            if (url == null) {
                showMessage("Invalid QR content.", "TRY AGAIN", UiColors.Danger)
            } else {
                addDoorDialog(url)
            }
        }
    }

    private fun reload() {
        doors = store.loadDoors()
        plates = store.loadPlates()
        if (plates.isEmpty()) {
            val defaultPlate = PlateProfile(store.newId("plate"), "ABC-123")
            plates.add(defaultPlate)
            store.savePlates(plates)
            store.setActivePlateId(defaultPlate.id)
        }

        activeDoorId = store.getActiveDoorId()
        activePlateId = store.getActivePlateId()

        if (activeDoorId == null || doors.none { it.id == activeDoorId }) {
            activeDoorId = doors.firstOrNull()?.id
            store.setActiveDoorId(activeDoorId)
        }

        if (activePlateId == null || plates.none { it.id == activePlateId }) {
            activePlateId = plates.firstOrNull()?.id
            store.setActivePlateId(activePlateId)
        }
    }

    private fun render() {
        val root = ScrollView(this).apply {
            setBackgroundColor(UiColors.Bg)
            clipToPadding = false
            fitsSystemWindows = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(14))
        }

        root.addView(layout)
        setContentView(root)

        layout.addView(header())
        layout.addView(statusBar(), LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(12), 0, dp(12))
        })

        layout.addView(profileCard(
            title = "Door",
            value = activeDoor()?.name ?: "No door saved",
            empty = activeDoor() == null,
            emptyAction = "Scan door QR code",
            onClick = { chooseDoorDialog() },
            onAction = { scanDoor() },
            onMenu = { showDoorMenu() },
        ))

        layout.addView(profileCard(
            title = "Vehicle",
            value = activePlate()?.plateNumber ?: "No vehicle saved",
            empty = activePlate() == null,
            emptyAction = "Add license plate",
            onClick = { choosePlateDialog() },
            onAction = { addPlateDialog(null) },
            onMenu = { showPlateMenu() },
        ))

        openButton = TextView(this).apply {
            text = "OPEN DOOR"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            background = rounded(UiColors.Green, dp(18))
            setPadding(dp(10), dp(20), dp(10), dp(20))
            isEnabled = !isOpening
            alpha = if (isOpening) 0.72f else 1f
            setOnClickListener { openDoor() }
        }
        layout.addView(openButton, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(4), 0, dp(10))
        })

        messageText = TextView(this).apply {
            text = lastOpenedAt?.let { "Last opened successfully at $it" } ?: "Scan once. Open anytime."
            textSize = 13f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(6), dp(2), dp(6), dp(8))
        }
        layout.addView(messageText)

        if (developerMode) {
            layout.addView(advancedSection())
        }

        layout.addView(TextView(this).apply {
            text = "v0.3.0"
            textSize = 12f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
            setOnClickListener { unlockDeveloperMode() }
        })
    }

    private fun header(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "Open-Sesame"
                textSize = 30f
                setTextColor(UiColors.Text)
                setTypeface(null, Typeface.BOLD)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, -2, 1f))

            addView(TextView(this@MainActivity).apply {
                text = "?"
                textSize = 19f
                setTextColor(UiColors.Text)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                background = roundedStroke(Color.TRANSPARENT, UiColors.BorderSoft, dp(22), dp(1))
                setOnClickListener { showInstructions() }
            }, LinearLayout.LayoutParams(dp(44), dp(44)))
        }
    }

    private fun statusBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(UiColors.GreenSoft, dp(14))

            statusText = TextView(this@MainActivity).apply {
                text = if (isOpening) "Sending request..." else "Ready to open"
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isOpening) UiColors.Warning else UiColors.Green)
                includeFontPadding = false
            }
            addView(statusText)
        }
    }

    private fun profileCard(
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

            val row = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener { if (empty) onAction() else onClick() }
            }

            val textBlock = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }

            textBlock.addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 13f
                setTextColor(UiColors.Muted)
                includeFontPadding = false
            })

            textBlock.addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 18f
                setTextColor(if (empty) UiColors.Muted else UiColors.Text)
                setTypeface(null, if (empty) Typeface.NORMAL else Typeface.BOLD)
                includeFontPadding = false
                maxLines = 1
                setPadding(0, dp(7), 0, 0)
            })

            row.addView(textBlock, LinearLayout.LayoutParams(0, -2, 1f))

            if (empty) {
                row.addView(TextView(this@MainActivity).apply {
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
                row.addView(TextView(this@MainActivity).apply {
                    text = ">"
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setTextColor(UiColors.Muted)
                    includeFontPadding = false
                    setPadding(dp(8), 0, dp(8), 0)
                })
                row.addView(TextView(this@MainActivity).apply {
                    text = "..."
                    textSize = 18f
                    gravity = Gravity.CENTER
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(UiColors.Muted)
                    includeFontPadding = false
                    setOnClickListener { onMenu() }
                }, LinearLayout.LayoutParams(dp(40), dp(40)))
            }

            addView(row)
        }
    }

    private fun advancedSection(): LinearLayout {
        return section("Advanced") {
            addView(actionRow(
                quietAction("DEBUG") { debugFetch() },
                quietAction("UPDATE") { openUrl("https://github.com/Xiaolong-6/Open-sesame/releases") },
                dangerAction("RESET") { clearAll() },
            ))
        }
    }

    private fun section(title: String, content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(UiColors.Card, dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, dp(2), 0, dp(12))
            }

            addView(TextView(this@MainActivity).apply {
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

    private fun showDoorMenu() {
        val door = activeDoor()
        val items = arrayOf("Choose door", "Scan new door", "Edit current door", "Delete current door")
        AlertDialog.Builder(this)
            .setTitle("Door")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> chooseDoorDialog()
                    1 -> scanDoor()
                    2 -> door?.let { editDoorDialog(it) } ?: addDoorDialog(null)
                    3 -> deleteActiveDoor()
                }
            }
            .show()
    }

    private fun showPlateMenu() {
        val plate = activePlate()
        val items = arrayOf("Choose vehicle", "Add license plate", "Edit current plate", "Delete current plate")
        AlertDialog.Builder(this)
            .setTitle("Vehicle")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> choosePlateDialog()
                    1 -> addPlateDialog(null)
                    2 -> plate?.let { addPlateDialog(it) } ?: addPlateDialog(null)
                    3 -> deleteActivePlate()
                }
            }
            .show()
    }

    private fun scanDoor() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        startActivity(Intent(this, QrScannerActivity::class.java))
    }

    private fun addDoorDialog(prefillUrl: String?) {
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
                val url = urlInput.text.toString().trim()
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

    private fun editDoorDialog(door: DoorProfile) {
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
                doors = doors.map {
                    if (it.id == door.id) it.copy(
                        name = nameInput.text.toString(),
                        accessUrl = urlInput.text.toString()
                    ) else it
                }.toMutableList()
                store.saveDoors(doors)
                reload()
                render()
            }
            .show()
    }

    private fun addPlateDialog(existing: PlateProfile?) {
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

    private fun chooseDoorDialog() {
        if (doors.isEmpty()) {
            addDoorDialog(null)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Choose door")
            .setItems(doors.map { it.name }.toTypedArray()) { _, which ->
                store.setActiveDoorId(doors[which].id)
                reload()
                render()
            }
            .show()
    }

    private fun choosePlateDialog() {
        if (plates.isEmpty()) {
            addPlateDialog(null)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Choose vehicle")
            .setItems(plates.map { it.plateNumber }.toTypedArray()) { _, which ->
                store.setActivePlateId(plates[which].id)
                reload()
                render()
            }
            .show()
    }

    private fun deleteActiveDoor() {
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

    private fun deleteActivePlate() {
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

    private fun openDoor() {
        if (isOpening) return
        val door = activeDoor()
        val plate = activePlate()

        if (door == null) {
            showMessage("No door selected.", "TRY AGAIN", UiColors.Danger)
            vibrateFailure()
            return
        }

        if (plate == null) {
            showMessage("No vehicle selected.", "TRY AGAIN", UiColors.Danger)
            vibrateFailure()
            return
        }

        isOpening = true
        updateOpeningUi("OPENING...", UiColors.Warning, "Sending request...")
        vibrateLight()

        Thread {
            val result = try {
                opener.openDoor(door, plate)
            } catch (e: Exception) {
                OpenResult(false, e.message ?: "Unknown error")
            }

            runOnUiThread {
                isOpening = false
                if (result.ok) {
                    lastOpenedAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    updateOpeningUi("OPENED", UiColors.Green, "Door opened at $lastOpenedAt")
                    messageText.text = "Last opened successfully at $lastOpenedAt"
                    vibrateSuccess()
                } else {
                    updateOpeningUi("TRY AGAIN", UiColors.Danger, result.message.ifBlank { "Opening failed - retry" })
                    vibrateFailure()
                }
            }
        }.start()
    }

    private fun showInstructions() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }

        content.addView(TextView(this).apply {
            text = "Save an authorized EuroPark (autoparkki) door QR URL and license plate locally, then reuse them for one-tap opening.\n\nThe app sends the web request only. Always verify the physical door."
            textSize = 15f
            setTextColor(UiColors.Text)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        })

        content.addView(TextView(this).apply {
            text = "v0.3.0-native-lite"
            textSize = 12f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
            setOnClickListener { unlockDeveloperMode() }
        })

        AlertDialog.Builder(this)
            .setTitle("Quick opener")
            .setView(content)
            .setNegativeButton("Update") { _, _ -> openUrl("https://github.com/Xiaolong-6/Open-sesame/releases") }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun debugFetch() {
        val door = activeDoor()
        val baseInfo = buildString {
            appendLine("Version: 0.3.0-native-lite")
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

    private fun clearAll() {
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

    private fun unlockDeveloperMode() {
        helpTapCount += 1
        if (helpTapCount >= 5 && !developerMode) {
            developerMode = true
            helpTapCount = 0
            render()
            showMessage("Developer mode enabled.", "OPEN DOOR", UiColors.Green)
        }
    }

    private fun activeDoor(): DoorProfile? = doors.firstOrNull { it.id == activeDoorId }
    private fun activePlate(): PlateProfile? = plates.firstOrNull { it.id == activePlateId }

    private fun showMessage(msg: String, buttonLabel: String, color: Int) {
        if (::statusText.isInitialized) {
            statusText.text = msg
            statusText.setTextColor(color)
        }
        if (::openButton.isInitialized) {
            openButton.text = buttonLabel
            openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(18))
            openButton.alpha = if (isOpening) 0.72f else 1f
            openButton.isEnabled = !isOpening
        }
        if (::messageText.isInitialized) {
            messageText.text = msg
        }
    }

    private fun updateOpeningUi(buttonLabel: String, color: Int, status: String) {
        openButton.text = buttonLabel
        openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(18))
        openButton.isEnabled = !isOpening
        openButton.alpha = if (isOpening) 0.72f else 1f
        statusText.text = status
        statusText.setTextColor(color)
        messageText.text = status
    }

    private fun extractAutoparkkiUrl(raw: String): String? {
        val match = Regex("https?://[^\\s\\\"'<>]+", RegexOption.IGNORE_CASE).find(raw.trim())
        val url = (match?.value ?: raw.trim())

        return try {
            val parsed = Uri.parse(url)
            val host = parsed.host?.lowercase() ?: return null
            if (
                parsed.scheme == "https" &&
                (host == "autoparkki.fi" || host.endsWith(".autoparkki.fi")) &&
                parsed.path?.startsWith("/access/") == true
            ) {
                url
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
