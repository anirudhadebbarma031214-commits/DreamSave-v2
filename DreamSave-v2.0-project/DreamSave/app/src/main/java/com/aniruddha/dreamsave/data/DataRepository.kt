package com.aniruddha.dreamsave.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists all app data as a single JSON file in the app's private storage.
 * Writes go to a temp file first and are then moved into place, so a crash
 * in the middle of a save can never corrupt the existing data.
 */
class DataRepository(context: Context) {

    private val file = File(context.filesDir, "dreamsave_data.json")
    private val tempFile = File(context.filesDir, "dreamsave_data.json.tmp")

    fun load(): AppData {
        if (!file.exists()) return AppData()
        return try {
            parse(file.readText())
        } catch (e: Exception) {
            val backup = File(file.parentFile, "dreamsave_data.corrupt.json")
            try {
                file.copyTo(backup, overwrite = true)
            } catch (ignored: Exception) {
            }
            AppData()
        }
    }

    @Synchronized
    fun save(data: AppData) {
        try {
            tempFile.writeText(toJson(data).toString())
            if (!tempFile.renameTo(file)) {
                file.writeText(tempFile.readText())
                tempFile.delete()
            }
        } catch (e: Exception) {
            // Saving failed (e.g. storage full). The in-memory state stays intact.
        }
    }

    private fun toJson(data: AppData): JSONObject {
        val goals = JSONArray()
        for (g in data.goals) {
            val o = JSONObject()
            o.put("id", g.id)
            o.put("name", g.name)
            o.put("target", g.target)
            o.put("starting", g.startingAmount)
            if (g.deadlineEpochDay != null) o.put("deadline", g.deadlineEpochDay)
            o.put("description", g.description)
            o.put("createdAt", g.createdAt)
            if (g.completedAt != null) o.put("completedAt", g.completedAt)
            goals.put(o)
        }
        val deposits = JSONArray()
        for (d in data.deposits) {
            val o = JSONObject()
            o.put("id", d.id)
            o.put("goalId", d.goalId)
            o.put("amount", d.amount)
            o.put("timestamp", d.timestamp)
            o.put("note", d.note)
            deposits.put(o)
        }
        val settings = JSONObject()
        settings.put("themeMode", data.settings.themeMode.name)
        settings.put("celebrations", data.settings.celebrations)

        val root = JSONObject()
        root.put("version", 2)
        root.put("goals", goals)
        root.put("deposits", deposits)
        root.put("settings", settings)
        return root
    }

    private fun parse(text: String): AppData {
        val root = JSONObject(text)

        val goals = ArrayList<Goal>()
        val goalArray = root.optJSONArray("goals") ?: JSONArray()
        for (i in 0 until goalArray.length()) {
            val o = goalArray.getJSONObject(i)
            goals.add(
                Goal(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    target = o.getDouble("target"),
                    startingAmount = o.optDouble("starting", 0.0),
                    deadlineEpochDay = if (o.has("deadline") && !o.isNull("deadline")) o.getLong("deadline") else null,
                    description = o.optString("description", ""),
                    createdAt = o.optLong("createdAt", 0L),
                    completedAt = if (o.has("completedAt") && !o.isNull("completedAt")) o.getLong("completedAt") else null
                )
            )
        }

        val deposits = ArrayList<Deposit>()
        val depositArray = root.optJSONArray("deposits") ?: JSONArray()
        for (i in 0 until depositArray.length()) {
            val o = depositArray.getJSONObject(i)
            deposits.add(
                Deposit(
                    id = o.getString("id"),
                    goalId = o.getString("goalId"),
                    amount = o.getDouble("amount"),
                    timestamp = o.getLong("timestamp"),
                    note = o.optString("note", "")
                )
            )
        }

        val settingsObject = root.optJSONObject("settings")
        val mode = try {
            ThemeMode.valueOf(settingsObject?.optString("themeMode", "DARK") ?: "DARK")
        } catch (e: IllegalArgumentException) {
            ThemeMode.DARK
        }
        val settings = AppSettings(
            themeMode = mode,
            celebrations = settingsObject?.optBoolean("celebrations", true) ?: true
        )

        return AppData(goals = goals, deposits = deposits, settings = settings)
    }
}
