package com.xl6.opensesame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.app.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

internal fun MainActivity.shareReleaseLink() {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ReleaseInfo.SHARE_TEXT)
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_open_sesame)))
}

internal fun MainActivity.showShareMenu() {
    showBottomSheet(
        title = getString(R.string.share_open_sesame),
        actions = listOf(
            SheetAction(getString(R.string.show_qr_code)) { showReleaseQrDialog() },
            SheetAction(getString(R.string.share_release_link)) { shareReleaseLink() },
            SheetAction(getString(R.string.open_releases_page)) { openUrl(ReleaseInfo.RELEASES_URL) },
        )
    )
}

internal fun MainActivity.showReleaseQrDialog() {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(24), dp(22), dp(24), dp(16))
        background = rounded(UiColors.Card, dp(18))
    }

    content.addView(TextView(this).apply {
        text = getString(R.string.release_qr_title)
        textSize = 20f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Text)
        gravity = Gravity.CENTER
        includeFontPadding = false
    })

    content.addView(TextView(this).apply {
        text = getString(R.string.release_qr_body)
        textSize = 14f
        setTextColor(UiColors.Muted)
        gravity = Gravity.CENTER
        setPadding(0, dp(10), 0, dp(16))
    })

    val qrSize = minOf(resources.displayMetrics.widthPixels - dp(96), dp(260))
    content.addView(ImageView(this).apply {
        setImageBitmap(generateQrBitmap(ReleaseInfo.RELEASES_URL, qrSize))
        adjustViewBounds = true
        background = rounded(Color.WHITE, dp(12))
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }, LinearLayout.LayoutParams(qrSize, qrSize))

    content.addView(TextView(this).apply {
        text = ReleaseInfo.displayVersion
        textSize = 12f
        setTextColor(UiColors.Muted)
        gravity = Gravity.CENTER
        setPadding(0, dp(12), 0, dp(8))
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
        setOnClickListener { shareReleaseLink() }
    })

    buttons.addView(TextView(this).apply {
        text = getString(R.string.close)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiColors.Green)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(10), 0, dp(10))
        setOnClickListener { dialog.dismiss() }
    })

    content.addView(buttons, LinearLayout.LayoutParams(-1, -2).apply {
        setMargins(0, dp(8), 0, 0)
    })

    dialog.setContentView(content)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout((resources.displayMetrics.widthPixels * 0.86f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

private fun generateQrBitmap(text: String, sizePx: Int): Bitmap {
    val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val pixels = IntArray(sizePx * sizePx)
    for (y in 0 until sizePx) {
        val offset = y * sizePx
        for (x in 0 until sizePx) {
            pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    }
}
