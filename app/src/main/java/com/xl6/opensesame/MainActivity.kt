package com.xl6.opensesame

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
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
    internal lateinit var store: ProfileStore
    internal val opener = AutoparkkiOpener()

    internal var doors = mutableListOf<DoorProfile>()
    internal var plates = mutableListOf<PlateProfile>()
    internal var activeDoorId: String? = null
    internal var activePlateId: String? = null
    internal var developerMode = false
    internal var helpTapCount = 0
    private var isOpening = false
    internal var lastOpenedAt: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var statusText: TextView
    private lateinit var statusIcon: StatusIconView
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

    internal fun reload() {
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

    internal fun render() {
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

        layout.addView(headerView { showInstructions() })
        layout.addView(statusBarView(
            isOpening = isOpening,
            bindStatusIcon = { statusIcon = it },
            bindStatusText = { statusText = it },
        ), LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(12), 0, dp(12))
        })

        layout.addView(profileCardView(
            title = "Door",
            value = activeDoor()?.name ?: "No door saved",
            empty = activeDoor() == null,
            emptyAction = "Scan door QR code",
            onClick = { chooseDoorDialog() },
            onAction = { scanDoor() },
            onMenu = { showDoorMenu() },
        ))

        layout.addView(profileCardView(
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
            layout.addView(advancedSectionView(
                onDebug = { debugFetch() },
                onUpdate = { openUrl("https://github.com/Xiaolong-6/Open-sesame/releases") },
                onReset = { clearAll() },
            ))
        }

        layout.addView(TextView(this).apply {
            text = "v0.3.3"
            textSize = 12f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
            setOnClickListener { unlockDeveloperMode() }
        })
    }

    internal fun scanDoor() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        startActivity(Intent(this, QrScannerActivity::class.java))
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
                    mainHandler.postDelayed({
                        if (!isOpening && ::openButton.isInitialized) {
                            openButton.text = "OPEN DOOR"
                        }
                    }, 2500)
                    vibrateSuccess()
                } else {
                    updateOpeningUi("TRY AGAIN", UiColors.Danger, result.message.ifBlank { "Opening failed - retry" })
                    vibrateFailure()
                }
            }
        }.start()
    }

    internal fun unlockDeveloperMode() {
        helpTapCount += 1
        if (helpTapCount >= 5 && !developerMode) {
            developerMode = true
            helpTapCount = 0
            render()
            showMessage("Developer mode enabled.", "OPEN DOOR", UiColors.Green)
        }
    }

    internal fun activeDoor(): DoorProfile? = doors.firstOrNull { it.id == activeDoorId }
    internal fun activePlate(): PlateProfile? = plates.firstOrNull { it.id == activePlateId }

    internal fun showMessage(msg: String, buttonLabel: String, color: Int) {
        if (::statusText.isInitialized) {
            statusText.text = msg
            statusText.setTextColor(color)
        }
        if (::statusIcon.isInitialized) {
            statusIcon.setKind(if (color == UiColors.Danger) StatusKind.Error else StatusKind.Ready)
        }
        if (::openButton.isInitialized) {
            openButton.text = buttonLabel
            openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(18))
            openButton.alpha = if (isOpening) 0.72f else 1f
            openButton.isEnabled = !isOpening
        }
    }

    private fun updateOpeningUi(buttonLabel: String, color: Int, status: String) {
        openButton.text = buttonLabel
        openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(18))
        openButton.isEnabled = !isOpening
        openButton.alpha = if (isOpening) 0.72f else 1f
        statusText.text = status
        statusText.setTextColor(color)
        statusIcon.setKind(when (color) {
            UiColors.Danger -> StatusKind.Error
            UiColors.Warning -> StatusKind.Opening
            else -> StatusKind.Success
        })
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

    internal fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
