package com.xl6.opensesame

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class AutoparkkiOpener {
    private val cookies = mutableMapOf<String, String>()

    fun openDoor(door: DoorProfile, plate: PlateProfile): OpenResult {
        val accessUrl = door.accessUrl.trim()
        val plateNumber = plate.plateNumber.trim().uppercase(Locale.ROOT)

        if (!accessUrl.startsWith("https://")) {
            return OpenResult(false, "Garage access URL must start with https://.")
        }

        if (plateNumber.isBlank()) {
            return OpenResult(false, "License plate is missing.")
        }

        val get = request("GET", accessUrl, null, null)
        if (!get.okHttp) {
            return OpenResult(false, "Access page GET failed with HTTP ${get.status}.", get.status, get.body.take(500))
        }

        val form = parseForm(get.body)
            ?: return OpenResult(false, "No parseable form found on access page.", get.status, get.body.take(500))

        val plateField = form.controls.firstOrNull { it.looksLikePlateField() }
            ?: form.controls.firstOrNull { it.isTextInput() }
            ?: return OpenResult(false, "Could not find plate input field.", get.status, get.body.take(500))

        val params = linkedMapOf<String, String>()
        form.controls.forEach { c ->
            if (!c.name.isNullOrBlank() && !c.isSubmit()) {
                params[c.name] = c.value
            }
        }

        params[plateField.name!!] = plateNumber

        val submit = form.controls.firstOrNull { it.isSubmit() && it.submitLooksLikeOpen() }
            ?: form.controls.firstOrNull { it.isSubmit() }

        if (!submit?.name.isNullOrBlank()) {
            params[submit!!.name!!] = submit.value.ifBlank { "1" }
        }

        val target = absoluteUrl(form.action, get.finalUrl ?: accessUrl)
        val body = encodeForm(params)

        val submitResult =
            if (form.method.equals("GET", ignoreCase = true)) {
                val separator = if (target.contains("?")) "&" else "?"
                request("GET", "$target$separator$body", null, get.finalUrl ?: accessUrl)
            } else {
                request("POST", target, body, get.finalUrl ?: accessUrl)
            }

        val semantic = responseLooksSuccessful(submitResult.body)
        val ok = submitResult.okHttp && semantic != false

        val message = when {
            semantic == true -> "Door request accepted for $plateNumber."
            submitResult.okHttp -> "Door request sent for $plateNumber. Please verify the door."
            else -> "Door request failed with HTTP ${submitResult.status}."
        }

        return OpenResult(ok, message, submitResult.status, stripHtml(submitResult.body).take(500))
    }

    fun suggestDoorName(accessUrl: String): String {
        return try {
            val res = request("GET", accessUrl.trim(), null, null)
            val plain = stripHtml(res.body)
            extractDoorNameFromPlainText(plain) ?: fallbackName(accessUrl)
        } catch (_: Exception) {
            fallbackName(accessUrl)
        }
    }

    fun debugAccessInfo(accessUrl: String): String {
        return try {
            val res = request("GET", accessUrl.trim(), null, null)
            val plain = stripHtml(res.body)
            val form = parseForm(res.body)
            val suggestedName = extractDoorNameFromPlainText(plain) ?: fallbackName(accessUrl)
            val title = Regex("""<title\b[^>]*>([\s\S]*?)</title>""", RegexOption.IGNORE_CASE)
                .find(res.body)
                ?.groupValues
                ?.getOrNull(1)
                ?.let { stripHtml(it) }
                ?.ifBlank { null }

            buildString {
                appendLine("Current door webpage info")
                appendLine("GET status: HTTP ${res.status}")
                appendLine("GET ok: ${if (res.okHttp) "yes" else "no"}")
                appendLine("Final URL: ${res.finalUrl ?: accessUrl.trim()}")
                appendLine("Suggested door name: $suggestedName")
                if (!title.isNullOrBlank()) appendLine("Page title: $title")

                if (form == null) {
                    appendLine("Form: not found")
                } else {
                    appendLine("Form method: ${form.method.uppercase(Locale.ROOT)}")
                    appendLine("Form action: ${form.action ?: "same page"}")
                    appendLine("Form controls: ${form.controls.size}")

                    val plateField = form.controls.firstOrNull { it.looksLikePlateField() }
                        ?: form.controls.firstOrNull { it.isTextInput() }
                    appendLine("Plate field: ${plateField?.name ?: "not found"}")

                    val submit = form.controls.firstOrNull { it.isSubmit() && it.submitLooksLikeOpen() }
                        ?: form.controls.firstOrNull { it.isSubmit() }
                    val submitLabel = listOfNotNull(submit?.text, submit?.value).firstOrNull { it.isNotBlank() }
                    appendLine("Submit control: ${submitLabel ?: submit?.name ?: "not found"}")
                }

                val preview = plain.take(700).ifBlank { "No readable text returned." }
                appendLine()
                appendLine("Readable page text preview:")
                append(preview)
            }
        } catch (e: Exception) {
            "Current door webpage info\nGET failed: ${e.message ?: "unknown error"}"
        }
    }

    private data class HttpResult(
        val status: Int,
        val finalUrl: String?,
        val body: String,
        val okHttp: Boolean
    )

    private data class Control(
        val tag: String,
        val name: String?,
        val value: String,
        val type: String?,
        val id: String?,
        val placeholder: String?,
        val text: String?
    ) {
        fun identity(): String = listOfNotNull(name, id, placeholder, text).joinToString(" ").lowercase(Locale.ROOT)

        fun looksLikePlateField(): Boolean {
            val s = identity()
            return name != null && (
                s.contains("plate") ||
                s.contains("license") ||
                s.contains("licence") ||
                s.contains("registration") ||
                s.contains("register") ||
                s.contains("regno") ||
                s.contains("rekister") ||
                s.contains("vehicle") ||
                s.contains("car")
            )
        }

        fun isTextInput(): Boolean {
            val t = (type ?: "text").lowercase(Locale.ROOT)
            return !name.isNullOrBlank() && tag != "button" && t !in setOf("hidden", "submit", "password")
        }

        fun isSubmit(): Boolean {
            val t = (type ?: "").lowercase(Locale.ROOT)
            return tag == "button" || t == "submit"
        }

        fun submitLooksLikeOpen(): Boolean {
            val s = "$value $text".lowercase(Locale.ROOT)
            return s.contains("avaa") || s.contains("open") || s.contains("door") || s.contains("ovi") || s.contains("submit")
        }
    }

    private data class Form(
        val action: String?,
        val method: String,
        val controls: List<Control>
    )

    private fun request(method: String, urlText: String, body: String?, referer: String?): HttpResult {
        val conn = (URL(urlText).openConnection() as HttpURLConnection)
        conn.requestMethod = method
        conn.instanceFollowRedirects = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        conn.setRequestProperty("User-Agent", "Open-Sesame-Native-Lite/0.3.0")
        referer?.let { conn.setRequestProperty("Referer", it) }

        if (cookies.isNotEmpty()) {
            conn.setRequestProperty("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
        }

        if (method == "POST") {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            conn.outputStream.use { it.write((body ?: "").toByteArray(Charsets.UTF_8)) }
        }

        val status = conn.responseCode
        conn.headerFields["Set-Cookie"]?.forEach { raw ->
            HttpCookie.parse(raw).forEach { cookies[it.name] = it.value }
        }

        val stream = if (status in 200..399) conn.inputStream else conn.errorStream
        val text = stream?.use {
            BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText()
        } ?: ""

        return HttpResult(status, conn.url?.toString(), text, status in 200..299)
    }

    private fun parseForm(html: String): Form? {
        val formRegex = Regex("<form\\b([^>]*)>([\\s\\S]*?)</form>", RegexOption.IGNORE_CASE)
        val match = formRegex.find(html) ?: return null
        val attrs = parseAttrs(match.groupValues[1])
        val formHtml = match.groupValues[2]
        val controls = mutableListOf<Control>()

        Regex("<input\\b[^>]*>", RegexOption.IGNORE_CASE).findAll(formHtml).forEach {
            val a = parseAttrs(it.value)
            val type = (a["type"] ?: "text").lowercase(Locale.ROOT)
            if (type !in setOf("button", "image", "file", "reset")) {
                controls += Control("input", a["name"], a["value"] ?: "", type, a["id"], a["placeholder"], null)
            }
        }

        Regex("<button\\b([^>]*)>([\\s\\S]*?)</button>", RegexOption.IGNORE_CASE).findAll(formHtml).forEach {
            val a = parseAttrs(it.groupValues[1])
            val type = (a["type"] ?: "submit").lowercase(Locale.ROOT)
            if (type == "submit") {
                controls += Control("button", a["name"], a["value"] ?: stripHtml(it.groupValues[2]), type, a["id"], null, stripHtml(it.groupValues[2]))
            }
        }

        return Form(attrs["action"], attrs["method"] ?: "POST", controls)
    }

    private fun parseAttrs(tag: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        val regex = Regex("""([:\w-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+)))?""")
        regex.findAll(tag).forEach {
            val key = it.groupValues[1].lowercase(Locale.ROOT)
            val value = listOf(2, 3, 4).map { idx -> it.groupValues[idx] }.firstOrNull { v -> v.isNotEmpty() } ?: ""
            out[key] = decodeHtml(value)
        }
        return out
    }

    private fun absoluteUrl(action: String?, base: String): String {
        if (action.isNullOrBlank()) return base
        return URI(base).resolve(action).toString()
    }

    private fun encodeForm(params: Map<String, String>): String {
        return params.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
    }

    private fun responseLooksSuccessful(html: String): Boolean? {
        val t = stripHtml(html).lowercase(Locale.ROOT)
        if (listOf("virhe", "error", "failed", "invalid", "väär").any { t.contains(it) }) return false
        if (listOf("avattu", "ovi avautuu", "ovi on avattu", "opened", "success").any { t.contains(it) }) return true
        return null
    }

    private fun stripHtml(input: String): String {
        return decodeHtml(
            input
                .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
        ).trim()
    }

    private fun decodeHtml(input: String): String {
        return input
            .replace("&nbsp;", " ", ignoreCase = true)
            .replace("&amp;", "&", ignoreCase = true)
            .replace("&lt;", "<", ignoreCase = true)
            .replace("&gt;", ">", ignoreCase = true)
            .replace("&#39;", "'", ignoreCase = true)
            .replace("&quot;", "\"", ignoreCase = true)
            .trim()
    }

    private fun extractDoorNameFromPlainText(text: String): String? {
        val cleaned = text.replace(Regex("\\s+"), " ").trim()
        val beforePrompt = cleaned.split(Regex("Syötä rekisterinumerosi", RegexOption.IGNORE_CASE)).firstOrNull()?.trim()
        if (!beforePrompt.isNullOrBlank()) {
            val name = beforePrompt
                .replace("EuroPark Finland - ADC", "")
                .replace(Regex("^[-–—|\\s]+"), "")
                .trim()
            if (name.length in 3..80) return name
        }

        val pName = Regex("\\bP-[A-Za-zÅÄÖåäö0-9][A-Za-zÅÄÖåäö0-9\\- ]{2,80}").find(cleaned)?.value?.trim()
        return pName
    }

    private fun fallbackName(accessUrl: String): String {
        return try {
            val path = URL(accessUrl).path.split("/").filter { it.isNotBlank() }
            val token = path.lastOrNull()
            if (!token.isNullOrBlank() && token.length >= 8) "EuroPark (autoparkki) ${token.take(8)}" else "EuroPark (autoparkki) door"
        } catch (_: Exception) {
            "EuroPark (autoparkki) door"
        }
    }
}
