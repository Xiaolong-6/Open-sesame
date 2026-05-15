package com.xl6.opensesame

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
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

    private lateinit var doorButton: Button
    private lateinit var plateButton: Button
    private lateinit var statusText: TextView
    private lateinit var messageText: TextView

    private val green = Color.rgb(31, 122, 90)
    private val bg = Color.rgb(244, 241, 234)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                showMessage("FAILED", "Invalid QR: $scanned")
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
        val root = ScrollView(this).apply { setBackgroundColor(bg) }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
        }
        root.addView(layout)
        setContentView(root)

        layout.addView(TextView(this).apply {
            text = "Open-Sesame"
            textSize = 34f
            setTextColor(Color.rgb(31, 41, 51))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        layout.addView(TextView(this).apply {
            text = "You only have to scan once"
            textSize = 15f
            setTextColor(Color.rgb(105, 115, 134))
            setPadding(0, dp(2), 0, dp(12))
        })

        layout.addView(section("Step 1. Scan the door code") {
            doorButton = mainButton(activeDoor()?.name ?: "Choose door") { chooseDoorDialog() }
            addView(doorButton)
            addView(row(
                actionButton("Scan") { scanDoor() },
                actionButton("Edit") { activeDoor()?.let { editDoorDialog(it) } ?: addDoorDialog(null) },
                dangerButton("Delete") { deleteActiveDoor() },
            ))
        })

        layout.addView(section("Step 2. Enter your license plate") {
            plateButton = mainButton(activePlate()?.plateNumber ?: "Choose plate") { choosePlateDialog() }
            addView(plateButton)
            addView(row(
                actionButton("Add") { addPlateDialog(null) },
                actionButton("Edit") { activePlate()?.let { addPlateDialog(it) } ?: addPlateDialog(null) },
                dangerButton("Delete") { deleteActivePlate() },
            ))
        })

        layout.addView(section("Step 3. Open the door") {
            val open = Button(this@MainActivity).apply {
                text = "OPEN"
                textSize = 26f
                setTextColor(Color.WHITE)
                setBackgroundColor(green)
                minHeight = dp(64)
                setOnClickListener { openDoor() }
            }
            addView(open)
        })

        val statusBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.rgb(232, 243, 238))
        }

        statusBox.addView(TextView(this).apply {
            text = "Status"
            textSize = 12f
            setTextColor(Color.rgb(102, 112, 133))
        })

        statusText = TextView(this).apply {
            text = "READY"
            textSize = 22f
            setTextColor(green)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        statusBox.addView(statusText)

        messageText = TextView(this).apply {
            text = "Ready"
            textSize = 13f
            setTextColor(Color.rgb(52, 64, 84))
        }
        statusBox.addView(messageText)
        layout.addView(statusBox)

        layout.addView(section("Debug") {
            addView(TextView(this@MainActivity).apply {
                text = "Version: native-lite 0.1\\nMode: real opener prototype\\nDoors: ${doors.size}\\nPlates: ${plates.size}"
                textSize = 13f
            })
            addView(row(
                actionButton("Debug fetch") { debugFetch() },
                actionButton("Releases") { openUrl("https://github.com/Xiaolong-6/Open-sesame/releases") },
                dangerButton("Clear") { clearAll() },
            ))
        })

        layout.addView(TextView(this).apply {
            text = "v0.1-native-lite. Opens authorized Autoparkki doors faster by saving scanned door QR URLs and license plates locally."
            textSize = 12f
            setTextColor(Color.rgb(105, 115, 134))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        })
    }

    private fun section(title: String, content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(Color.WHITE)
            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.setMargins(0, 0, 0, dp(12))
            layoutParams = lp

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 17f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(31, 41, 51))
                setPadding(0, 0, 0, dp(8))
            })

            content()
        }
    }

    private fun mainButton(textValue: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            textSize = 16f
            setTextColor(Color.rgb(31, 41, 51))
            setBackgroundColor(Color.rgb(232, 243, 238))
            minHeight = dp(50)
            setOnClickListener { onClick() }
        }
    }

    private fun actionButton(textValue: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            setTextColor(Color.WHITE)
            setBackgroundColor(green)
            setOnClickListener { onClick() }
        }
    }

    private fun dangerButton(textValue: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            setTextColor(Color.rgb(180, 35, 24))
            setBackgroundColor(Color.rgb(253, 232, 232))
            setOnClickListener { onClick() }
        }
    }

    private fun row(vararg views: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
            views.forEach {
                addView(it, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    setMargins(dp(3), 0, dp(3), 0)
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
            Thread {
                val suggested = opener.suggestDoorName(prefillUrl)
                runOnUiThread {
                    if (nameInput.text.isNullOrBlank()) nameInput.setText(suggested)
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
                    if (it.id == door.id) it.copy(name = nameInput.text.toString(), accessUrl = urlInput.text.toString())
                    else it
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
                    plates = plates.map { if (it.id == existing.id) it.copy(plateNumber = plate) else it }.toMutableList()
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
                    .setMessage("Door name suggestion:\\n$name\\n\\nURL:\\n${door.accessUrl}")
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
        statusText.setTextColor(if (status == "FAILED") Color.rgb(180, 35, 24) else green)
        messageText.text = msg
    }

    private fun extractAutoparkkiUrl(raw: String): String? {
        val match = Regex("https?://[^\\s\\\"'<>]+", RegexOption.IGNORE_CASE).find(raw.trim())
        val url = (match?.value ?: raw.trim())
        return try {
            val parsed = Uri.parse(url)
            val host = parsed.host?.lowercase() ?: return null
            if (parsed.scheme == "https" && (host == "autoparkki.fi" || host.endsWith(".autoparkki.fi")) && parsed.path?.startsWith("/access/") == true) {
                url
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
