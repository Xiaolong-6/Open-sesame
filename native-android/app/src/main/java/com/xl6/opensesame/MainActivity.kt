package com.xl6.opensesame

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var store: ProfileStore
    private val opener = AutoparkkiOpener()

    private var doors = mutableListOf<DoorProfile>()
    private var plates = mutableListOf<PlateProfile>()
    private var activeDoorId: String? = null
    private var activePlateId: String? = null

    private lateinit var doorButton: TextView
    private lateinit var plateButton: TextView
    private lateinit var statusText: TextView
    private lateinit var messageText: TextView

    private val bg = Color.rgb(244, 241, 234)
    private val card = Color.WHITE
    private val textColor = Color.rgb(31, 41, 51)
    private val muted = Color.rgb(105, 115, 134)
    private val green = Color.rgb(31, 122, 90)
    private val greenSoft = Color.rgb(232, 243, 238)
    private val danger = Color.rgb(180, 35, 24)
    private val dangerSoft = Color.rgb(253, 232, 232)
    private val neutralSoft = Color.rgb(242, 244, 247)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
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
                showMessage("FAILED", "Invalid QR content.")
            } else {
                addDoorDialog(url)
            }
        }
    }

    private fun reload() {
        doors = store.loadDoors()
        plates = store.loadPlates()
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
            setBackgroundColor(bg)
            clipToPadding = false
            fitsSystemWindows = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        root.addView(layout)
        setContentView(root)

        layout.addView(TextView(this).apply {
            text = "Open-Sesame"
            textSize = 32f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
        })

        layout.addView(TextView(this).apply {
            text = "You only have to scan once"
            textSize = 15f
            setTextColor(muted)
            includeFontPadding = false
            setPadding(0, dp(6), 0, dp(12))
        })

        layout.addView(section("Step 1. Scan the door code") {
            doorButton = selectorButton(activeDoor()?.name ?: "Choose door") { chooseDoorDialog() }
            addView(doorButton)
            addView(row(
                primaryAction("SCAN") { scanDoor() },
                secondaryAction("EDIT") { activeDoor()?.let { editDoorDialog(it) } ?: addDoorDialog(null) },
                dangerAction("DELETE") { deleteActiveDoor() },
            ))
        })

        layout.addView(section("Step 2. Enter your license plate") {
            plateButton = selectorButton(activePlate()?.plateNumber ?: "Choose plate") { choosePlateDialog() }
            addView(plateButton)
            addView(row(
                primaryAction("ADD") { addPlateDialog(null) },
                secondaryAction("EDIT") { activePlate()?.let { addPlateDialog(it) } ?: addPlateDialog(null) },
                dangerAction("DELETE") { deleteActivePlate() },
            ))
        })

        layout.addView(section("Step 3. Open the door") {
            addView(TextView(this@MainActivity).apply {
                text = "OPEN"
                textSize = 30f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.NORMAL)
                background = rounded(green, dp(18))
                setPadding(0, dp(16), 0, dp(16))
                setOnClickListener { openDoor() }
            })
        })

        val statusBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = rounded(greenSoft, dp(18))
        }

        statusBox.addView(TextView(this).apply {
            text = "Status"
            textSize = 13f
            setTextColor(muted)
            includeFontPadding = false
        })

        statusText = TextView(this).apply {
            text = "READY"
            textSize = 22f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
            setPadding(0, dp(4), 0, 0)
        }
        statusBox.addView(statusText)

        messageText = TextView(this).apply {
            text = "Ready"
            textSize = 14f
            setTextColor(Color.rgb(52, 64, 84))
            includeFontPadding = false
            setPadding(0, dp(6), 0, 0)
        }
        statusBox.addView(messageText)

        layout.addView(statusBox, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(12))
        })

        layout.addView(debugSection())

        layout.addView(TextView(this).apply {
            text = "v0.2-native-lite. Opens authorized Autoparkki doors faster by saving scanned door QR URLs and license plates locally."
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(10), dp(6), 0)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        })
    }

    private fun debugSection(): LinearLayout {
        return section("Debug") {
            addView(TextView(this@MainActivity).apply {
                text = "Version: native-lite 0.2\nMode: real opener prototype\nDoors: ${doors.size}\nPlates: ${plates.size}"
                textSize = 13f
                setTextColor(muted)
                setPadding(0, 0, 0, dp(8))
            })

            addView(row(
                primaryAction("DEBUG") { debugFetch() },
                secondaryAction("RELEASES") { openUrl("https://github.com/Xiaolong-6/Open-sesame/releases") },
                dangerAction("CLEAR") { clearAll() },
            ))
        }
    }

    private fun section(title: String, content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(card, dp(18))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 0, 0, dp(12))
            }

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(textColor)
                includeFontPadding = false
                setPadding(0, 0, 0, dp(10))
            })

            content()
        }
    }

    private fun selectorButton(value: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = value
            textSize = 18f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            maxLines = 1
            background = rounded(greenSoft, dp(14))
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener { onClick() }
        }
    }

    private fun primaryAction(label: String, onClick: () -> Unit): TextView {
        return action(label, Color.WHITE, green, onClick)
    }

    private fun secondaryAction(label: String, onClick: () -> Unit): TextView {
        return action(label, Color.WHITE, green, onClick)
    }

    private fun dangerAction(label: String, onClick: () -> Unit): TextView {
        return action(label, danger, dangerSoft, onClick)
    }

    private fun action(label: String, textColor: Int, backgroundColor: Int, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(textColor)
            background = rounded(backgroundColor, dp(14))
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener { onClick() }
        }
    }

    private fun row(vararg views: TextView): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
            views.forEachIndexed { index, view ->
                addView(view, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    val left = if (index == 0) 0 else dp(5)
                    val right = if (index == views.lastIndex) 0 else dp(5)
                    setMargins(left, 0, right, 0)
                })
            }
        }
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
                val name = nameInput.text.toString().trim().ifBlank { "Autoparkki door" }
                val profile = DoorProfile(store.newId("door"), name, url)
                doors.add(profile)
                store.saveDoors(doors)
                store.setActiveDoorId(profile.id)
                reload()
                render()
                showMessage("READY", "Saved door: $name")
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
            .setTitle(if (existing == null) "Add plate" else "Edit plate")
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
            .setTitle("Choose plate")
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
            .setTitle("Delete door?")
            .setMessage(door.name)
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
            .setTitle("Delete plate?")
            .setMessage(plate.plateNumber)
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
        val door = activeDoor()
        val plate = activePlate()

        if (door == null) {
            showMessage("FAILED", "No door selected.")
            return
        }

        if (plate == null) {
            showMessage("FAILED", "No plate selected.")
            return
        }

        showMessage("OPENING", "Opening ${door.name} for ${plate.plateNumber}...")

        Thread {
            val result = try {
                opener.openDoor(door, plate)
            } catch (e: Exception) {
                OpenResult(false, e.message ?: "Unknown error")
            }

            runOnUiThread {
                showMessage(if (result.ok) "SUCCESS" else "FAILED", result.message)
            }
        }.start()
    }

    private fun debugFetch() {
        val door = activeDoor() ?: return
        Thread {
            val name = opener.suggestDoorName(door.accessUrl)
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Debug fetch")
                    .setMessage("Door name suggestion:\n$name\n\nURL:\n${door.accessUrl}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }.start()
    }

    private fun clearAll() {
        AlertDialog.Builder(this)
            .setTitle("Clear all local profiles?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear") { _, _ ->
                store.clearAll()
                reload()
                render()
            }
            .show()
    }

    private fun activeDoor(): DoorProfile? = doors.firstOrNull { it.id == activeDoorId }
    private fun activePlate(): PlateProfile? = plates.firstOrNull { it.id == activePlateId }

    private fun showMessage(status: String, msg: String) {
        statusText.text = status
        statusText.setTextColor(if (status == "FAILED") danger else green)
        messageText.text = msg
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

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
