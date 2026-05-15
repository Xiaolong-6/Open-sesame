package com.xl6.opensesame

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("open_sesame_native_lite", Context.MODE_PRIVATE)

    fun loadDoors(): MutableList<DoorProfile> {
        val raw = prefs.getString("doors", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<DoorProfile>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out += DoorProfile(
                id = obj.optString("id"),
                name = obj.optString("name"),
                accessUrl = obj.optString("accessUrl")
            )
        }
        return out
    }

    fun saveDoors(doors: List<DoorProfile>) {
        val arr = JSONArray()
        doors.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("accessUrl", it.accessUrl)
            })
        }
        prefs.edit().putString("doors", arr.toString()).apply()
    }

    fun loadPlates(): MutableList<PlateProfile> {
        val raw = prefs.getString("plates", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<PlateProfile>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out += PlateProfile(
                id = obj.optString("id"),
                plateNumber = obj.optString("plateNumber")
            )
        }
        return out
    }

    fun savePlates(plates: List<PlateProfile>) {
        val arr = JSONArray()
        plates.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("plateNumber", it.plateNumber)
            })
        }
        prefs.edit().putString("plates", arr.toString()).apply()
    }

    fun getActiveDoorId(): String? = prefs.getString("activeDoorId", null)
    fun setActiveDoorId(id: String?) = prefs.edit().putString("activeDoorId", id).apply()

    fun getActivePlateId(): String? = prefs.getString("activePlateId", null)
    fun setActivePlateId(id: String?) = prefs.edit().putString("activePlateId", id).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun newId(prefix: String): String = "${prefix}_${UUID.randomUUID()}"
}
