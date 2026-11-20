package com.endroid.aariz.ui.chat

/** What the Run button does for a code block. */
enum class RunKind {
    /** JavaScript, run on the device in a sandboxed WebView. */
    LOCAL_JS,

    /** JSON, validated and pretty printed on the device. */
    JSON,

    /** HTML / SVG, shown in a sandboxed preview. */
    PREVIEW,

    /** Other languages, run on the online Judge0 service after the user agrees. */
    REMOTE,

    NONE,
}

/** Pure helpers behind the code runner (no Android classes, so they are unit tested). */
object RunLogic {
    const val SERVICE = "Judge0"
    private const val MAX_OUTPUT = 6_000

    private val js = setOf("js", "javascript", "mjs", "node")
    private val preview = setOf("html", "htm", "svg")

    /** Judge0 CE language ids. */
    private val ids = mapOf(
        "python" to 71, "py" to 71, "python3" to 71,
        "c" to 50,
        "cpp" to 54, "c++" to 54, "cc" to 54,
        "java" to 62,
        "go" to 60, "golang" to 60,
        "rust" to 73, "rs" to 73,
        "ruby" to 72, "rb" to 72,
        "php" to 68,
        "bash" to 46, "sh" to 46, "shell" to 46,
        "kotlin" to 78, "kt" to 78,
        "typescript" to 74, "ts" to 74,
        "csharp" to 51, "cs" to 51, "c#" to 51,
        "swift" to 83,
        "r" to 80,
        "perl" to 85,
        "lua" to 64,
        "haskell" to 61, "hs" to 61,
        "scala" to 81,
        "sql" to 82, "sqlite" to 82,
    )

    fun languageId(lang: String): Int? = ids[lang.trim().lowercase()]

    fun kind(lang: String): RunKind {
        val l = lang.trim().lowercase()
        return when {
            l in js -> RunKind.LOCAL_JS
            l == "json" -> RunKind.JSON
            l in preview -> RunKind.PREVIEW
            ids.containsKey(l) -> RunKind.REMOTE
            else -> RunKind.NONE
        }
    }

    /** Quote [s] as a JavaScript string literal that is safe inside an inline <script>. */
    fun jsString(s: String): String {
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

    fun buildHtml(template: String, code: String): String = template.replace("__CODE__", jsString(code))

    /** Turn a Judge0 result into readable text. */
    fun formatRemote(
        stdout: String?,
        stderr: String?,
        compileOutput: String?,
        message: String?,
        statusId: Int,
        status: String?,
        time: String?,
    ): String {
        val parts = mutableListOf<String>()
        if (!stdout.isNullOrBlank()) parts += stdout.trimEnd()
        if (!compileOutput.isNullOrBlank()) parts += "Compile error:\n" + compileOutput.trimEnd()
        if (!stderr.isNullOrBlank()) parts += "stderr:\n" + stderr.trimEnd()
        if (!message.isNullOrBlank()) parts += message.trimEnd()
        // 3 = Accepted, 4 = Wrong Answer (meaningless without an expected output)
        if (statusId != 3 && statusId != 4 && !status.isNullOrBlank()) parts += "Status: $status"
        if (parts.isEmpty()) parts += "(no output)"
        val text = parts.joinToString("\n\n").take(MAX_OUTPUT)
        val footer = if (!time.isNullOrBlank()) "(ran online on $SERVICE · ${time}s)" else "(ran online on $SERVICE)"
        return "$text\n\n$footer"
    }

    /** Wrap an SVG in a page, or use an HTML document as it is. */
    fun previewHtml(lang: String, code: String): String =
        if (lang.trim().equals("svg", ignoreCase = true)) {
            "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
                "<style>body{margin:0;display:flex;align-items:center;justify-content:center;min-height:100vh;background:#fff}" +
                "svg{max-width:100%;height:auto}</style></head><body>$code</body></html>"
        } else {
            code
        }
}
