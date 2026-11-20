package com.endroid.aariz.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume

/**
 * Runs code blocks. JavaScript runs locally inside a locked-down WebView (no network, no file
 * access, no access to the app, killed after a few seconds). The sandbox page lives in
 * assets/runner.html and supports await, timers, console.table / group, and prints the value
 * of the last expression.
 */
object CodeRunner {
    private const val TIMEOUT_MS = 5_000L
    private const val MAX_OUTPUT = 6_000

    @Volatile
    private var cachedTemplate: String? = null

    private fun template(context: Context): String =
        cachedTemplate ?: context.assets.open("runner.html").bufferedReader().use { it.readText() }
            .also { cachedTemplate = it }

    /** Pretty print JSON, or say what is wrong with it. */
    fun formatJson(code: String): String = try {
        when (val v = JSONTokener(code.trim()).nextValue()) {
            is JSONObject -> v.toString(2)
            is JSONArray -> v.toString(2)
            else -> v.toString()
        }
    } catch (e: Exception) {
        "Invalid JSON: ${e.message}"
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun runJs(context: Context, code: String): String = withContext(Dispatchers.Main) {
        var web: WebView? = null
        try {
            val html = RunLogic.buildHtml(template(context), code)
            val out = withTimeoutOrNull(TIMEOUT_MS) {
                suspendCancellableCoroutine<String> { cont ->
                    val view = WebView(context.applicationContext)
                    web = view
                    view.settings.apply {
                        javaScriptEnabled = true
                        blockNetworkLoads = true
                        allowFileAccess = false
                        allowContentAccess = false
                        domStorageEnabled = false
                    }
                    view.webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            v: WebView?,
                            request: WebResourceRequest?,
                        ): WebResourceResponse =
                            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                    }
                    view.addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun done(text: String) {
                                if (cont.isActive) cont.resume(text)
                            }
                        },
                        "Aariz",
                    )
                    view.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                }
            }
            (out ?: "Timed out after ${TIMEOUT_MS / 1000} s").ifBlank { "(no output)" }.take(MAX_OUTPUT)
        } finally {
            web?.destroy()
        }
    }
}
