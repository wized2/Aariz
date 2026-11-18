package com.endroid.aariz.network

import com.endroid.aariz.BuildConfig
import com.endroid.aariz.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AarizApi {
    private const val SYSTEM = """You are Aariz, a fast, clear AI assistant by Project Endroid (Mudasir Wazir).
Reply in short, useful, mobile-readable paragraphs.
Be direct. Prefer plain language. Use markdown sparingly.
If asked who you are, say you are Aariz by Project Endroid."""

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun chat(
        prompt: String,
        history: List<Message>,
        webSearch: Boolean,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val hist = JSONArray()
            history.takeLast(12).forEach { m ->
                hist.put(
                    JSONObject()
                        .put("role", if (m.role == "assistant") "model" else "user")
                        .put("content", m.text.take(6000))
                )
            }
            val payload = JSONObject()
                .put("prompt", prompt)
                .put("system", SYSTEM)
                .put("system_instruction", SYSTEM)
                .put("instructions", SYSTEM)
                .put("web_search", webSearch)
                .put("history", hist)
                .put("feature", "")
                .put("mode", "")
                .put(
                    "client",
                    JSONObject()
                        .put("name", "Aariz")
                        .put("version", BuildConfig.VERSION_NAME)
                        .put("mobile", true)
                        .put("platform", "android")
                )

            val req = Request.Builder()
                .url(BuildConfig.WORKER_URL)
                .post(payload.toString().toRequestBody(jsonType))
                .header("Accept", "text/plain, application/json")
                .build()

            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    val msg = when (resp.code) {
                        401 -> "Unauthorized — check the API worker."
                        429 -> "Rate limit exceeded — wait a moment and try again."
                        in 500..599 -> "Server error — try again shortly."
                        else -> "Worker error: ${resp.code}"
                    }
                    return@withContext Result.failure(Exception(msg))
                }
                val text = if (body.trimStart().startsWith("{")) {
                    val o = JSONObject(body)
                    o.optString("text")
                        .ifBlank { o.optString("response") }
                        .ifBlank { o.optString("message") }
                        .ifBlank { o.optString("output") }
                        .ifBlank { body }
                } else {
                    body
                }
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Flux image URL (same pattern as web). */
    fun imageUrl(prompt: String, seed: Int? = null): String {
        val q = java.net.URLEncoder.encode(prompt, Charsets.UTF_8.name())
        val extra = if (seed != null) "&seed=$seed" else ""
        return "https://api.omegatech.app/api/ai/flux?prompt=$q$extra"
    }
}
