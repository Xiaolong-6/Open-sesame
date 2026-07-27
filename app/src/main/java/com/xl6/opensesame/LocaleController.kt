package com.xl6.opensesame

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleController {
    const val LANGUAGE_SYSTEM = ""
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_FINNISH = "fi"
    const val LANGUAGE_CHINESE = "zh"

    private const val PREFS_NAME = "open_sesame_native_lite"
    private const val KEY_LANGUAGE = "languageOverride"

    fun wrap(base: Context): Context {
        val language = getLanguageOverride(base)
        if (language.isBlank()) return base

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    fun getLanguageOverride(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, LANGUAGE_SYSTEM)
            ?: LANGUAGE_SYSTEM
    }

    fun setLanguageOverride(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (language.isBlank()) {
            prefs.edit().remove(KEY_LANGUAGE).apply()
        } else {
            prefs.edit().putString(KEY_LANGUAGE, language).apply()
        }
    }
}
