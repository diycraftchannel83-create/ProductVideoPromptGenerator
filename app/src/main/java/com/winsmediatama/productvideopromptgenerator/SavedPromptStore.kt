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
    val savedAt: Long,
    val productDetails: String = "",
    val style: String = "Cinematic",
    val camera: String = "Slow dolly-in",
    val lighting: String = "Soft studio lighting",
    val background: String = "Minimal premium studio",
    val duration: String = "8 seconds",
    val aspectRatio: String = "9:16",
    val language: String = "English",
    val multiScene: Boolean = false,
    val imagePath: String = ""
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
                    savedAt = item.optLong("savedAt"),
                    productDetails = item.optString("productDetails"),
                    style = item.optString("style", "Cinematic"),
                    camera = item.optString("camera", "Slow dolly-in"),
                    lighting = item.optString("lighting", "Soft studio lighting"),
                    background = item.optString("background", "Minimal premium studio"),
                    duration = item.optString("duration", "8 seconds"),
                    aspectRatio = item.optString("aspectRatio", "9:16"),
                    language = item.optString("language", "English"),
                    multiScene = item.optBoolean("multiScene", false),
                    imagePath = item.optString("imagePath")
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
                put("productDetails", saved.productDetails)
                put("style", saved.style)
                put("camera", saved.camera)
                put("lighting", saved.lighting)
                put("background", saved.background)
                put("duration", saved.duration)
                put("aspectRatio", saved.aspectRatio)
                put("language", saved.language)
                put("multiScene", saved.multiScene)
                put("imagePath", saved.imagePath)
        put("imagePath", saved.imagePath)
                put("imagePath", saved.imagePath)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    fun exportJson(context: Context): String {
        val projects = JSONArray()
        getAll(context).forEach { projects.put(toJson(it)) }
        return JSONObject().apply {
            put("format", "wins-product-video-prompt-projects")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("projects", projects)
        }.toString(2)
    }

    fun importJson(context: Context, raw: String): Int {
        val root = JSONObject(raw)
        require(root.optString("format") == "wins-product-video-prompt-projects") {
            "File format is not a ProductVideoPromptGenerator backup."
        }
        require(root.optInt("version", -1) == 1) { "Unsupported backup version." }
        val projects = root.optJSONArray("projects") ?: throw IllegalArgumentException("Backup contains no project list.")
        val merged = getAll(context).toMutableList()
        var imported = 0
        for (index in 0 until projects.length()) {
            val obj = projects.optJSONObject(index) ?: continue
            val item = fromJson(obj) ?: continue
            val uniqueId = if (merged.any { it.id == item.id }) System.currentTimeMillis() + index else item.id
            merged.removeAll { it.id == uniqueId }
            merged.add(item.copy(id = uniqueId))
            imported++
        }
        val array = JSONArray()
        merged.sortedByDescending { it.savedAt }.forEach { array.put(toJson(it)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
        return imported
    }

    private fun fromJson(item: JSONObject): SavedPrompt? {
        val prompt = item.optString("prompt")
        if (prompt.isBlank()) return null
        return SavedPrompt(
            id = item.optLong("id", System.currentTimeMillis()),
            name = item.optString("name"),
            category = item.optString("category", "Product Commercial"),
            title = item.optString("title", "Imported Prompt"),
            prompt = prompt,
            negativePrompt = item.optString("negativePrompt"),
            savedAt = item.optLong("savedAt", System.currentTimeMillis()),
            productDetails = item.optString("productDetails"),
            style = item.optString("style", "Cinematic"),
            camera = item.optString("camera", "Slow dolly-in"),
            lighting = item.optString("lighting", "Soft studio lighting"),
            background = item.optString("background", "Minimal premium studio"),
            duration = item.optString("duration", "8 seconds"),
            aspectRatio = item.optString("aspectRatio", "9:16"),
            language = item.optString("language", "English"),
            multiScene = item.optBoolean("multiScene", false),
            imagePath = item.optString("imagePath")
        )
    }

    private fun toJson(saved: SavedPrompt) = JSONObject().apply {
        put("id", saved.id)
        put("name", saved.name)
        put("category", saved.category)
        put("title", saved.title)
        put("prompt", saved.prompt)
        put("negativePrompt", saved.negativePrompt)
        put("savedAt", saved.savedAt)
        put("productDetails", saved.productDetails)
        put("style", saved.style)
        put("camera", saved.camera)
        put("lighting", saved.lighting)
        put("background", saved.background)
        put("duration", saved.duration)
        put("aspectRatio", saved.aspectRatio)
        put("language", saved.language)
        put("multiScene", saved.multiScene)
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
                put("productDetails", saved.productDetails)
                put("style", saved.style)
                put("camera", saved.camera)
                put("lighting", saved.lighting)
                put("background", saved.background)
                put("duration", saved.duration)
                put("aspectRatio", saved.aspectRatio)
                put("language", saved.language)
                put("multiScene", saved.multiScene)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }
}
