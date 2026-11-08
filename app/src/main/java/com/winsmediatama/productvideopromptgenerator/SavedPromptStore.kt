package com.winsmediatama.productvideopromptgenerator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedPrompt(
    val id: Long,
    val name: String,
    val category: String,
    val title: String,
    val prompt: String,
    val negativePrompt: String,
    val savedAt: Long
)

object SavedPromptStore {
    private const val PREFS = "saved_prompt_projects"
    private const val KEY_ITEMS = "items"

    fun getAll(context: Context): List<SavedPrompt> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                SavedPrompt(
                    id = item.optLong("id"),
                    name = item.optString("name"),
                    category = item.optString("category"),
                    title = item.optString("title"),
                    prompt = item.optString("prompt"),
                    negativePrompt = item.optString("negativePrompt"),
                    savedAt = item.optLong("savedAt")
                )
            }.sortedByDescending { it.savedAt }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, item: SavedPrompt) {
        val items = getAll(context).filterNot { it.id == item.id }.toMutableList()
        items.add(item)
        val array = JSONArray()
        items.sortedByDescending { it.savedAt }.forEach { saved ->
            array.put(JSONObject().apply {
                put("id", saved.id)
                put("name", saved.name)
                put("category", saved.category)
                put("title", saved.title)
                put("prompt", saved.prompt)
                put("negativePrompt", saved.negativePrompt)
                put("savedAt", saved.savedAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    fun delete(context: Context, id: Long) {
        val array = JSONArray()
        getAll(context).filterNot { it.id == id }.forEach { saved ->
            array.put(JSONObject().apply {
                put("id", saved.id)
                put("name", saved.name)
                put("category", saved.category)
                put("title", saved.title)
                put("prompt", saved.prompt)
                put("negativePrompt", saved.negativePrompt)
                put("savedAt", saved.savedAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }
}
