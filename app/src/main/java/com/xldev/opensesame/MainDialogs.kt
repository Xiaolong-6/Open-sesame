package com.xldev.opensesame

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

internal fun MainActivity.showDoorMenu() {
    chooseDoorDialog()
}

internal fun MainActivity.showPlateMenu() {
    choosePlateDialog()
}

private data class CenterDialogAction(
    val label: String,
    val destructive: Boolean = false,
    val action: () -> Unit
)

private fun MainActivity.showCenterDialog(
    title: String,
    message: String? = null,
    body: View? = null,
    actions: List<CenterDialogAction> = listOf(CenterDialogAction(getString(R.string.ok)) {})
) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        minimumWidth = resources.displayMetrics.widthPixels - dp(64)
        setPadding(dp(20), dp(20), dp(20), dp(16))
        background = rounded(UiColors.Card, dp(14))
    }

    content.addView(TextView(this).apply {
        text = title
        textSize = 18f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        gravity = Gravity.CENTER
        includeFontPadding = false
        setPadding(0, 0, 0, dp(14))
    })

    if (!message.isNullOrBlank()) {
        content.addView(TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(UiColors.Muted)
            gravity = Gravity.CENTER
            setLineSpacing(dp(2).toFloat(), 1f)
            setPadding(0, 0, 0, dp(16))
        })
    }

    body?.let {
        content.addView(it, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(16))
        })
    }

    val buttons = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
    }

    actions.forEach { item ->
        buttons.addView(TextView(this).apply {
            text = item.label
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (item.destructive) UiColors.Danger else UiColors.Green)
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setOnClickListener {
                dialog.dismiss()
                item.action()
            }
        })
    }
    content.addView(buttons, LinearLayout.LayoutParams(-1, -2))

    dialog.setContentView(content)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.CENTER)
    }
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

    showCenterDialog(
        title = getString(R.string.add_door),
        body = container,
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.save)) {
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage(getString(R.string.invalid_europark_access_url), getString(R.string.try_again), UiColors.Danger)
            } else {
                val name = nameInput.text.toString().trim().ifBlank { getString(R.string.europark_door_fallback) }
                val profile = DoorProfile(store.newId("door"), name, url)
                doors.add(profile)
                store.saveDoors(doors)
                store.setActiveDoorId(profile.id)
                reload()
                render()
                showMessage(getString(R.string.ready_to_open), getString(R.string.open_door), UiColors.Green)
            }
            }
        )
    )
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

    showCenterDialog(
        title = getString(R.string.edit_door),
        body = container,
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.save)) {
            val url = normalizeAutoparkkiAccessUrl(urlInput.text.toString())
            if (url == null) {
                showMessage(getString(R.string.invalid_europark_access_url), getString(R.string.try_again), UiColors.Danger)
            } else {
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
            }
        )
    )
}

internal fun MainActivity.addPlateDialog(existing: PlateProfile?) {
    val input = EditText(this).apply {
        hint = "ABC-123"
        setText(existing?.plateNumber ?: "")
    }

    showCenterDialog(
        title = if (existing == null) getString(R.string.add_license_plate) else getString(R.string.edit_license_plate),
        body = input,
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.save)) {
            val plate = input.text.toString().trim().uppercase()
            if (plate.isNotBlank()) {
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
            }
        )
    )
}

internal fun MainActivity.chooseDoorDialog() {
    showProfilePickerDialog(
        title = getString(R.string.choose_door),
        primaryAction = getString(R.string.scan_new_door),
        helperText = getString(R.string.picker_interaction_hint),
        emptyText = getString(R.string.no_door_saved),
        rows = doors.map { door ->
            ProfilePickerRow(
                id = door.id,
                label = door.name,
                selected = door.id == activeDoorId,
                onSelect = {
                    store.setActiveDoorId(door.id)
                    reload()
                    render()
                },
                onEdit = { editDoorDialog(door) },
            )
        },
        onDeleteSelected = { ids ->
            val deletedActive = activeDoorId in ids
            doors.removeAll { it.id in ids }
            store.saveDoors(doors)
            if (deletedActive) {
                store.setActiveDoorId(doors.firstOrNull()?.id)
            }
            reload()
            render()
        },
        onPrimaryAction = { scanDoor() }
    )
}

internal fun MainActivity.choosePlateDialog() {
    showProfilePickerDialog(
        title = getString(R.string.choose_vehicle),
        primaryAction = getString(R.string.add_license_plate),
        helperText = getString(R.string.picker_interaction_hint),
        emptyText = getString(R.string.no_vehicle_saved),
        rows = plates.map { plate ->
            ProfilePickerRow(
                id = plate.id,
                label = plate.plateNumber,
                selected = plate.id == activePlateId,
                onSelect = {
                    store.setActivePlateId(plate.id)
                    reload()
                    render()
                },
                onEdit = { addPlateDialog(plate) },
            )
        },
        onDeleteSelected = { ids ->
            val deletedActive = activePlateId in ids
            plates.removeAll { it.id in ids }
            store.savePlates(plates)
            if (deletedActive) {
                store.setActivePlateId(plates.firstOrNull()?.id)
            }
            reload()
            render()
        },
        onPrimaryAction = { addPlateDialog(null) }
    )
}

internal fun MainActivity.deleteActiveDoor() {
    val door = activeDoor() ?: return
    deleteDoor(door)
}

internal fun MainActivity.deleteDoor(door: DoorProfile) {
    showCenterDialog(
        title = getString(R.string.delete_current_door_question),
        message = getString(R.string.delete_current_door_message),
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.delete), destructive = true) {
            val wasActive = door.id == activeDoorId
            doors.removeAll { it.id == door.id }
            store.saveDoors(doors)
            if (wasActive) {
                store.setActiveDoorId(doors.firstOrNull()?.id)
            }
            reload()
            render()
            }
        )
    )
}

internal fun MainActivity.deleteActivePlate() {
    val plate = activePlate() ?: return
    deletePlate(plate)
}

internal fun MainActivity.deletePlate(plate: PlateProfile) {
    showCenterDialog(
        title = getString(R.string.delete_current_plate_question),
        message = getString(R.string.delete_current_plate_message),
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.delete), destructive = true) {
            val wasActive = plate.id == activePlateId
            plates.removeAll { it.id == plate.id }
            store.savePlates(plates)
            if (wasActive) {
                store.setActivePlateId(plates.firstOrNull()?.id)
            }
            reload()
            render()
            }
        )
    )
}

private data class ProfilePickerRow(
    val id: String,
    val label: String,
    val selected: Boolean,
    val onSelect: () -> Unit,
    val onEdit: () -> Unit
)

private fun MainActivity.showProfilePickerDialog(
    title: String,
    primaryAction: String,
    helperText: String,
    emptyText: String,
    rows: List<ProfilePickerRow>,
    onDeleteSelected: (Set<String>) -> Unit,
    onPrimaryAction: () -> Unit
) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    var deleteMode = false
    val checkedIds = mutableSetOf<String>()

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        minimumWidth = resources.displayMetrics.widthPixels - dp(64)
        setPadding(dp(20), dp(20), dp(20), dp(18))
        background = rounded(UiColors.Card, dp(14))
    }

    content.addView(TextView(this).apply {
        text = primaryAction
        textSize = 16f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        background = rounded(UiColors.Green, dp(4))
        setPadding(dp(12), dp(14), dp(12), dp(14))
        setOnClickListener {
            dialog.dismiss()
            onPrimaryAction()
        }
    }, LinearLayout.LayoutParams(-1, -2).apply {
        setMargins(0, 0, 0, dp(16))
    })

    content.addView(TextView(this).apply {
        text = title
        textSize = 18f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        gravity = Gravity.CENTER
        includeFontPadding = false
        setPadding(0, 0, 0, dp(12))
    })

    content.addView(TextView(this).apply {
        text = helperText
        textSize = 13f
        setTextColor(UiColors.Muted)
        gravity = Gravity.CENTER
        includeFontPadding = false
        setPadding(0, 0, 0, dp(10))
    })

    val listContainer = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }
    content.addView(listContainer)

    fun renderRows() {
        listContainer.removeAllViews()

        if (rows.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = emptyText
                textSize = 15f
                setTextColor(UiColors.Muted)
                gravity = Gravity.CENTER
                includeFontPadding = false
                setPadding(dp(8), dp(18), dp(8), dp(18))
            })
            return
        }

        rows.forEachIndexed { index, row ->
            if (index > 0) {
                listContainer.addView(View(this).apply {
                    setBackgroundColor(UiColors.BorderSoft)
                    alpha = 0.55f
                }, LinearLayout.LayoutParams(-1, dp(1)))
            }
            listContainer.addView(profilePickerRowView(
                dialog = dialog,
                item = row,
                deleteMode = deleteMode,
                checked = row.id in checkedIds,
                onToggleDelete = {
                    if (row.id in checkedIds) {
                        checkedIds.remove(row.id)
                    } else {
                        checkedIds.add(row.id)
                    }
                    renderRows()
                },
                onEnterDeleteMode = {
                    if (!deleteMode) {
                        deleteMode = true
                        vibrateSelectionMode()
                    }
                    checkedIds.add(row.id)
                    renderRows()
                }
            ))
        }

        if (deleteMode) {
            listContainer.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                val selectedRow = rows.firstOrNull { it.id in checkedIds }
                addView(selectionActionButton(
                    label = getString(R.string.edit_selected),
                    enabled = checkedIds.size == 1,
                    destructive = false
                ) {
                    if (checkedIds.size == 1 && selectedRow != null) {
                        dialog.dismiss()
                        selectedRow.onEdit()
                    }
                }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    setMargins(0, 0, dp(5), 0)
                })

                addView(selectionActionButton(
                    label = getString(R.string.delete_selected),
                    enabled = checkedIds.isNotEmpty(),
                    destructive = true
                ) {
                    if (checkedIds.isNotEmpty()) {
                        dialog.dismiss()
                        onDeleteSelected(checkedIds.toSet())
                    }
                }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    setMargins(dp(5), 0, 0, 0)
                })
            }, LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, dp(14), 0, 0)
            })

            listContainer.addView(TextView(this).apply {
                text = getString(R.string.cancel)
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(UiColors.Muted)
                gravity = Gravity.CENTER
                includeFontPadding = false
                setPadding(dp(12), dp(13), dp(12), 0)
                setOnClickListener {
                    deleteMode = false
                    checkedIds.clear()
                    renderRows()
                }
            })
        }
    }

    renderRows()

    dialog.setContentView(content)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.CENTER)
    }
}

private fun MainActivity.profilePickerRowView(
    dialog: Dialog,
    item: ProfilePickerRow,
    deleteMode: Boolean,
    checked: Boolean,
    onToggleDelete: () -> Unit,
    onEnterDeleteMode: () -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(10), 0, dp(10))

        if (deleteMode) {
            addView(CheckBox(this@profilePickerRowView).apply {
                isChecked = checked
                setOnClickListener { onToggleDelete() }
            }, LinearLayout.LayoutParams(dp(44), dp(44)))
        }

        addView(TextView(this@profilePickerRowView).apply {
            text = item.label
            textSize = 16f
            setTextColor(UiColors.Text)
            setTypeface(null, if (item.selected) Typeface.BOLD else Typeface.NORMAL)
            includeFontPadding = false
            ellipsize = android.text.TextUtils.TruncateAt.END
            maxLines = 1
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                if (deleteMode) {
                    onToggleDelete()
                } else {
                    dialog.dismiss()
                    item.onSelect()
                }
            }
            setOnLongClickListener {
                if (!deleteMode) {
                    onEnterDeleteMode()
                }
                true
            }
        }, LinearLayout.LayoutParams(0, dp(44), 1f))
    }
}

private fun MainActivity.selectionActionButton(
    label: String,
    enabled: Boolean,
    destructive: Boolean = false,
    onClick: () -> Unit
): TextView {
    return TextView(this).apply {
        text = label
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(if (destructive) Color.WHITE else UiColors.Green)
        gravity = Gravity.CENTER
        includeFontPadding = false
        setPadding(dp(4), 0, dp(4), 0)
        alpha = if (enabled) 1f else 0.45f
        background = if (destructive) {
            rounded(UiColors.Danger, dp(4))
        } else {
            roundedStroke(UiColors.GreenSoft, UiColors.BorderSoft, dp(4), dp(1))
        }
        setOnClickListener {
            if (enabled) {
                onClick()
            }
        }
    }
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

    var versionText: TextView? = null
    content.addView(settingsSectionCard(getString(R.string.about)) {
        addView(settingsActionRow(getString(R.string.open_releases_page)) {
            dialog.dismiss()
            openUrl(ReleaseInfo.RELEASES_URL)
        })
        addView(settingsStaticRow(
            getString(R.string.version_label, ReleaseInfo.VERSION_NAME),
            showDivider = false,
            bindLabel = { versionText = it }
        ))
    })
    checkForAboutUpdate(dialog, versionText)

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

private fun MainActivity.settingsStaticRow(
    label: String,
    showDivider: Boolean = true,
    bindLabel: ((TextView) -> Unit)? = null
): LinearLayout {
    return settingsBaseRow(label, showDivider, null, muted = true, bindLabel = bindLabel)
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
    bindLabel: ((TextView) -> Unit)? = null,
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
            bindLabel?.invoke(this)
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

private fun MainActivity.checkForAboutUpdate(dialog: Dialog, versionText: TextView?) {
    if (versionText == null) return
    Thread {
        val hasNew = try {
            UpdateChecker.hasNewRelease()
        } catch (_: Exception) {
            false
        }

        if (hasNew) {
            runOnUiThread {
                if (dialog.isShowing) {
                    versionText.text = getString(R.string.version_label_new, ReleaseInfo.VERSION_NAME)
                }
            }
        }
    }.start()
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
        showCenterDialog(
            title = "Debug",
            message = baseInfo + "\nCurrent door: none selected.\nScan a EuroPark (autoparkki) door first."
        )
        return
    }

    showCenterDialog(
        title = "Debug",
        message = baseInfo + "\nCurrent door: selected\nFetching current door webpage info..."
    )

    Thread {
        val pageInfo = opener.debugAccessInfo(door.accessUrl)
        runOnUiThread {
            showCenterDialog(
                title = "Debug",
                message = baseInfo + "\nCurrent door: selected\n\n" + pageInfo
            )
        }
    }.start()
}

internal fun MainActivity.clearAll() {
    showCenterDialog(
        title = getString(R.string.reset_app_data_question),
        message = getString(R.string.reset_app_data_message),
        actions = listOf(
            CenterDialogAction(getString(R.string.cancel)) {},
            CenterDialogAction(getString(R.string.reset), destructive = true) {
            store.clearAll()
            reload()
            lastOpenedAt = null
            render()
            }
        )
    )
}
