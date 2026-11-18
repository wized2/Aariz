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
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume

/**
 * Runs small JavaScript snippets from code blocks inside a locked-down WebView:
 * no network, no file access, no access to the app, killed after a few seconds.
 */
object CodeRunner {
    private const val TIMEOUT_MS = 5_000L
    private const val MAX_OUTPUT = 4_000

    private val runnable = setOf("js", "javascript", "mjs", "node")

    fun isRunnable(lang: String): Boolean = lang.trim().lowercase() in runnable

    /** Quote [s] as a JavaScript string literal that is safe inside an inline <script>. */
    internal fun jsString(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '<' -> sb.append("\\u003c")
                ' ' -> sb.append("\\u2028")
                ' ' -> sb.append("\\u2029")
                else -> if (c < ' ') sb.append(String.format("\\u%04x", c.code)) else sb.append(c)
            }
        }
        return sb.append('"').toString()
    }

    internal fun buildHtml(code: String): String =
        "<!doctype html><html><body><script>" +
            "var __o=[];" +
            "function __f(a){return Array.prototype.map.call(a,function(x){" +
            "if(typeof x==='string')return x;" +
            "try{return JSON.stringify(x)}catch(e){return String(x)}}).join(' ')}" +
            "console.log=console.info=console.warn=console.debug=function(){__o.push(__f(arguments))};" +
            "console.error=function(){__o.push('Error: '+__f(arguments))};" +
            "window.onerror=function(m){__o.push('Error: '+m)};" +
            "try{var __r=(0,eval)(" + jsString(code) + ");" +
            "if(__r!==undefined)__o.push('\\u2192 '+__f([__r]))}catch(e){__o.push('Error: '+e)}" +
            "setTimeout(function(){Aariz.done(__o.join('\\n'))},400);" +
            "</script></body></html>"

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun run(context: Context, code: String): String = withContext(Dispatchers.Main) {
        var web: WebView? = null
        try {
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
                    view.loadDataWithBaseURL(null, buildHtml(code), "text/html", "utf-8", null)
                }
            }
            val text = out ?: "Timed out after ${TIMEOUT_MS / 1000} s"
            text.ifBlank { "(no output)" }.take(MAX_OUTPUT)
        } finally {
            web?.destroy()
        }
    }
}
