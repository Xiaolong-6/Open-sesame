package com.xldev.opensesame

import android.content.Context
import android.content.res.Configuration

object ThemeController {
    const val THEME_SYSTEM = ""
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private const val PREFS_NAME = "open_sesame_native_lite"
    private const val KEY_THEME = "themeOverride"

    fun getThemeOverride(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_SYSTEM)
            ?: THEME_SYSTEM
    }

    fun setThemeOverride(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (theme.isBlank()) {
            prefs.edit().remove(KEY_THEME).apply()
        } else {
            prefs.edit().putString(KEY_THEME, theme).apply()
        }
    }

    fun isDark(context: Context): Boolean {
        return when (getThemeOverride(context)) {
            THEME_LIGHT -> false
            THEME_DARK -> true
            else -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
