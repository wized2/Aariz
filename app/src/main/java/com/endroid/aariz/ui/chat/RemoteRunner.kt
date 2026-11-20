package com.endroid.aariz.ui.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Runs languages the device cannot run itself on the public Judge0 CE service
 * (https://ce.judge0.com). The code is sent over HTTPS, so the UI asks the user first.
 */
object RemoteRunner {
    private const val ENDPOINT = "https://ce.judge0.com/submissions?base64_encoded=false&wait=true"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun JSONObject.str(key: String): String? = if (isNull(key)) null else optString(key)

    suspend fun run(lang: String, code: String): String = withContext(Dispatchers.IO) {
        val id = RunLogic.languageId(lang) ?: return@withContext "Unsupported language: $lang"
        try {
            val body = JSONObject()
                .put("source_code", code)
                .put("language_id", id)
                .put("stdin", "")
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder().url(ENDPOINT).post(body).header("Accept", "application/json").build()
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@use when (resp.code) {
                        429 -> "The online runner is busy (rate limit). Try again in a moment."
                        in 500..599 -> "The online runner had a server error. Try again shortly."
                        else -> "The online runner refused the request (HTTP ${resp.code})."
                    }
                }
                val o = JSONObject(text)
                val st = o.optJSONObject("status")
                RunLogic.formatRemote(
                    stdout = o.str("stdout"),
                    stderr = o.str("stderr"),
                    compileOutput = o.str("compile_output"),
                    message = o.str("message"),
                    statusId = st?.optInt("id", 0) ?: 0,
                    status = st?.optString("description"),
                    time = o.str("time"),
                )
            }
        } catch (e: Exception) {
            "Couldn't reach the online runner: ${e.message ?: e.javaClass.simpleName}"
        }
    }
}
