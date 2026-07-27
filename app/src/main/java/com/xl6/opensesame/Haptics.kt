package com.xl6.opensesame

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity

fun ComponentActivity.vibrateLight() {
    window.decorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    vibratePattern(longArrayOf(0, 35), -1)
}

fun ComponentActivity.vibrateSuccess() {
    window.decorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    vibratePattern(longArrayOf(0, 60), -1)
}

fun ComponentActivity.vibrateFailure() {
    window.decorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    vibratePattern(longArrayOf(0, 45, 80, 45), -1)
}

fun ComponentActivity.vibrateSelectionMode() {
    window.decorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}

private fun ComponentActivity.vibratePattern(pattern: LongArray, repeat: Int) {
    val vibrator = getSystemService(Vibrator::class.java) ?: return
    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, repeat)
    }
}
