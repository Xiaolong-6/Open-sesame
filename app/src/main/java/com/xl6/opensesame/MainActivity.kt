package com.xl6.opensesame

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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

    private val scanDoorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val scanned = result.data?.getStringExtra(QrScannerActivity.EXTRA_SCAN_RESULT)
        if (!scanned.isNullOrBlank()) {
            handleScannedDoor(scanned)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleController.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val darkTheme = ThemeController.isDark(this)
        UiColors.applyTheme(darkTheme)
        enableEdgeToEdge(
            statusBarStyle = if (darkTheme) SystemBarStyle.dark(UiColors.Bg) else SystemBarStyle.light(UiColors.Bg, UiColors.Bg),
            navigationBarStyle = if (darkTheme) SystemBarStyle.dark(UiColors.Bg) else SystemBarStyle.light(UiColors.Bg, UiColors.Bg)
        )

        store = ProfileStore(this)
        developerMode = store.getDeveloperMode()
        reload()
        render()
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
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiColors.Bg)
            fitsSystemWindows = true
            setPadding(dp(22), dp(18), dp(22), dp(14))
        }

        setContentView(root)

        root.addView(headerView { showInstructions() })

        val bodyScroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = true
        }
        root.addView(bodyScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(12), dp(10), dp(6))
        }

        bodyScroll.addView(layout, ViewGroup.LayoutParams(-1, -1))

        layout.addView(doorSummaryCardView(
            value = activeDoor()?.name ?: getString(R.string.no_door_saved),
            empty = activeDoor() == null,
            onClick = { chooseDoorDialog() },
        ))

        layout.addView(openPanelCardView(
            plateValue = activePlate()?.plateNumber ?: getString(R.string.no_vehicle_saved),
            plateEmpty = activePlate() == null,
            isOpening = isOpening,
            bindOpenButton = { openButton = it },
            onOpen = { openDoor() },
            onPlateClick = { choosePlateDialog() },
        ))

        layout.addView(statusInfoCardView(
            isOpening = isOpening,
            lastOpenedAt = lastOpenedAt,
            bindStatusIcon = { statusIcon = it },
            bindStatusText = { statusText = it },
            bindMessageText = { messageText = it },
        ))

        layout.addView(TextView(this).apply {
            text = ReleaseInfo.displayVersion
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
        scanDoorLauncher.launch(Intent(this, QrScannerActivity::class.java))
    }

    private fun handleScannedDoor(scanned: String) {
        val url = normalizeAutoparkkiAccessUrl(scanned)
        if (url == null) {
            showMessage(getString(R.string.invalid_qr_content), getString(R.string.try_again), UiColors.Danger)
        } else {
            addDoorDialog(url)
        }
    }

    private fun openDoor() {
        if (isOpening) return
        val door = activeDoor()
        val plate = activePlate()

        if (door == null) {
            showMessage(getString(R.string.no_door_selected), getString(R.string.try_again), UiColors.Danger)
            vibrateFailure()
            return
        }

        if (plate == null) {
            showMessage(getString(R.string.no_vehicle_selected), getString(R.string.try_again), UiColors.Danger)
            vibrateFailure()
            return
        }

        if (normalizeAutoparkkiAccessUrl(door.accessUrl) == null) {
            showMessage(getString(R.string.invalid_europark_access_url), getString(R.string.try_again), UiColors.Danger)
            vibrateFailure()
            return
        }

        isOpening = true
        updateOpeningUi(getString(R.string.opening), UiColors.Warning, getString(R.string.sending_request))
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
                    updateOpeningUi(getString(R.string.opened), UiColors.Green, getString(R.string.door_opened_at, lastOpenedAt))
                    messageText.text = getString(R.string.last_opened_successfully_at, lastOpenedAt)
                    mainHandler.postDelayed({
                        if (!isOpening && ::openButton.isInitialized) {
                            openButton.text = getString(R.string.open_door)
                        }
                    }, 2500)
                    vibrateSuccess()
                } else {
                    updateOpeningUi(getString(R.string.try_again), UiColors.Danger, result.message.ifBlank { getString(R.string.opening_failed_retry) })
                    vibrateFailure()
                }
            }
        }.start()
    }

    internal fun unlockDeveloperMode() {
        helpTapCount += 1
        if (helpTapCount >= 5 && !developerMode) {
            developerMode = true
            store.setDeveloperMode(true)
            helpTapCount = 0
            render()
            showMessage(getString(R.string.developer_mode_enabled), getString(R.string.open_door), UiColors.Green)
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
            openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(4))
            openButton.alpha = if (isOpening) 0.72f else 1f
            openButton.isEnabled = !isOpening
        }
    }

    private fun updateOpeningUi(buttonLabel: String, color: Int, status: String) {
        openButton.text = buttonLabel
        openButton.background = rounded(if (color == UiColors.Danger) UiColors.Danger else UiColors.Green, dp(4))
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

    internal fun normalizeAutoparkkiAccessUrl(raw: String): String? {
        val match = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE).find(raw.trim())
        val url = (match?.value ?: raw.trim())

        return try {
            val parsed = url.toUri()
            val host = parsed.host?.lowercase() ?: return null
            if (
                parsed.scheme == "https" &&
                (host == "autoparkki.fi" || host.endsWith(".autoparkki.fi")) &&
                parsed.path?.startsWith("/access/") == true
            ) {
                parsed.toString()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    internal fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
