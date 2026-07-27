package com.xl6.opensesame

import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    fun hasNewRelease(): Boolean {
        val latest = fetchLatestVersion() ?: return false
        return compareVersions(latest, ReleaseInfo.VERSION_NAME) > 0
    }

    private fun fetchLatestVersion(): String? {
        val conn = (URL(ReleaseInfo.LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", ReleaseInfo.userAgent)
        }

        return try {
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            Regex(""""tag_name"\s*:\s*"([^"]+)"""").find(body)
                ?.groupValues
                ?.getOrNull(1)
                ?.toVersionName()
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun compareVersions(left: String, right: String): Int {
        val leftParts = left.split(".").map { it.toIntOrNull() ?: 0 }
        val rightParts = right.split(".").map { it.toIntOrNull() ?: 0 }
        val size = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until size) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) return leftPart.compareTo(rightPart)
        }
        return 0
    }

    private fun String.toVersionName(): String {
        return trim().removePrefix("v").substringBefore("-")
    }
}
