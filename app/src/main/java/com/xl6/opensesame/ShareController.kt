package com.xl6.opensesame

import android.content.Intent

internal fun MainActivity.shareReleaseLink() {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ReleaseInfo.SHARE_TEXT)
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_open_sesame)))
}
