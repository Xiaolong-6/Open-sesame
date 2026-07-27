package com.xl6.opensesame

object ReleaseInfo {
    const val VERSION_NAME = "0.4.8"
    const val RELEASES_URL = "https://github.com/Xiaolong-6/Open-sesame/releases"
    const val SHARE_TEXT = "Open-Sesame AndroidNative Lite\n$RELEASES_URL"

    val displayVersion: String
        get() = "v$VERSION_NAME"

    val userAgent: String
        get() = "Open-Sesame/$VERSION_NAME"
}
