package com.winsmediatama.productvideopromptgenerator

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the user's API key encrypted with an AES key held by Android Keystore.
 * The key never belongs in source control or BuildConfig.
 */
object OpenAiKeyStore {
    private const val PREFS = "openai_secure_settings"
    private const val KEY_ENTRY = "openai_api_key"
    private const val KEY_ALIAS = "product_video_prompt_openai_aes"

    fun save(context: Context, apiKey: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENTRY, Base64.encodeToString(payload, Base64.NO_WRAP))
            .apply()
    }

    fun get(context: Context): String? = runCatching {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ENTRY, null) ?: return null
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > 12) { "Data API key tidak valid." }
        val iv = payload.copyOfRange(0, 12)
        val encrypted = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }.getOrNull()

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_ENTRY).apply()
        runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}

object OpenAiLyricClient {
    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"

    fun generate(
        apiKey: String,
        title: String,
        theme: String,
        genre: String,
        mood: String,
        vocal: String,
        structure: String,
        language: String
    ): String {
        require(apiKey.startsWith("sk-")) { "Format API key tidak valid." }
        val systemPrompt = """
            You are a professional songwriter, lyricist, vocal director, and music producer.
            Write original, emotionally compelling, singable song lyrics. Follow the requested language.
            Include clear section headings and concise bracketed cues for emotion, vocal delivery,
            instrumentation, dynamics, and transitions in every section. Every sung stanza must contain
            exactly four lyric lines. Do not explain your process. Do not include copyrighted lyrics
            or imitate a living artist.
        """.trimIndent()
        val userPrompt = """
            Create a complete original song.
            Title: $title
            Story/theme: $theme
            Language: $language
            Genre: $genre
            Emotional mood: $mood
            Vocal: $vocal
            Structure: $structure

            Requirements:
            - Follow the requested structure in order.
            - Write vivid, natural, memorable lyrics with a strong hook.
            - Put performance and emotional cues in [square brackets] before each section.
            - Add instrumental intro/interlude/outro cues where appropriate.
            - Every sung stanza must have exactly four lines.
            - Make the chorus memorable and the bridge emotionally distinctive.
            - Output only the finished lyrics, with no commentary.
        """.trimIndent()

        val body = JSONObject()
            .put("model", "gpt-4o-mini")
            .put("temperature", 0.85)
            .put("messages", JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt))
                .put(JSONObject().put("role", "user").put("content", userPrompt)))
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty()
                throw IllegalStateException(
                    when (status) {
                        401 -> "API key ditolak. Periksa key OpenAI Anda."
                        429 -> "Kuota atau billing API habis, atau batas permintaan tercapai."
                        else -> message.ifBlank { "OpenAI mengembalikan HTTP $status." }
                    }
                )
            }
            val content = JSONObject(response).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content").trim()
            if (content.isBlank()) throw IllegalStateException("GPT mengembalikan lirik kosong.")
            return content
        } finally {
            connection.disconnect()
        }
    }
}
