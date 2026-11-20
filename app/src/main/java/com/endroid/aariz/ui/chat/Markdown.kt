package com.endroid.aariz.ui.chat

/** A run of text with inline styling. [link] is set for hyperlinks. */
data class Span(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val strike: Boolean = false,
    val code: Boolean = false,
    val link: String? = null,
)

enum class Align { LEFT, CENTER, RIGHT }

/** One list entry. [marker] is "•" or "1."; [indent] is the nesting level; [checked] is for task items. */
data class ListItem(
    val marker: String,
    val indent: Int,
    val spans: List<Span>,
    val checked: Boolean? = null,
)

sealed interface MdBlock {
    data class Heading(val level: Int, val spans: List<Span>) : MdBlock
    data class Paragraph(val spans: List<Span>) : MdBlock
    data class Quote(val blocks: List<MdBlock>) : MdBlock
    data class Items(val items: List<ListItem>) : MdBlock
    data class Table(
        val header: List<List<Span>>,
        val aligns: List<Align>,
        val rows: List<List<List<Span>>>,
    ) : MdBlock
    data object Rule : MdBlock
    data class Code(val lang: String, val code: String) : MdBlock
}

/**
 * Markdown for assistant replies. Supports headings, paragraphs, bold, italic, bold-italic,
 * strikethrough, inline code, links and autolinks, block quotes, ordered / unordered / nested /
 * task lists, tables with alignment, horizontal rules and fenced code blocks.
 * Pure Kotlin, so it is unit tested without Android.
 */
object Markdown {
    private val heading = Regex("^ {0,3}(#{1,6})[ \\t]+(.*?)(?:[ \\t]+#+)?[ \\t]*$")
    private val rule = Regex("^ {0,3}([-*_])(?:[ \\t]*\\1){2,}[ \\t]*$")
    private val listItem = Regex("^([ \\t]*)([-*+]|[0-9]{1,9}[.)])[ \\t]+(.*)$")
    private val task = Regex("^\\[([ xX])][ \\t]+(.*)$")
    private val tableDelim = Regex("^[ \\t]*\\|?[ \\t]*:?-+:?[ \\t]*(\\|[ \\t]*:?-+:?[ \\t]*)*\\|?[ \\t]*$")
    private val fenceOpen = Regex("^ {0,3}(`{3,}|~{3,})[ \\t]*([^`\\s]*).*$")

    fun parse(src: String): List<MdBlock> = parseLines(src.replace("\r\n", "\n").replace('\r', '\n').split('\n'))

    private fun parseLines(lines: List<String>): List<MdBlock> {
        val out = mutableListOf<MdBlock>()
        val para = mutableListOf<String>()

        fun flushPara() {
            if (para.isNotEmpty()) {
                out += MdBlock.Paragraph(inline(para.joinToString("\n")))
                para.clear()
            }
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            val fence = fenceOpen.matchEntire(line)
            if (fence != null) {
                flushPara()
                val mark = fence.groupValues[1]
                val lang = fence.groupValues[2]
                val body = mutableListOf<String>()
                i++
                while (i < lines.size) {
                    val t = lines[i].trim()
                    if (t.length >= mark.length && t.all { it == mark[0] }) break
                    body.add(lines[i])
                    i++
                }
                out += MdBlock.Code(lang, body.joinToString("\n"))
                i++ // skip the closing fence (or run off the end for an unclosed block)
                continue
            }

            if (line.isBlank()) {
                flushPara()
                i++
                continue
            }

            val h = heading.matchEntire(line)
            if (h != null) {
                flushPara()
                out += MdBlock.Heading(h.groupValues[1].length, inline(h.groupValues[2]))
                i++
                continue
            }

            if (rule.matches(line)) {
                flushPara()
                out += MdBlock.Rule
                i++
                continue
            }

            if (line.trimStart().startsWith(">")) {
                flushPara()
                val inner = mutableListOf<String>()
                while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                    inner.add(lines[i].trimStart().removePrefix(">").removePrefix(" "))
                    i++
                }
                out += MdBlock.Quote(parseLines(inner))
                continue
            }

            if (line.contains('|') && i + 1 < lines.size && tableDelim.matches(lines[i + 1]) && lines[i + 1].contains('-')) {
                val header = splitRow(line)
                val delim = splitRow(lines[i + 1])
                if (header.size == delim.size && header.isNotEmpty()) {
                    flushPara()
                    val aligns = delim.map { d ->
                        val t = d.trim()
                        when {
                            t.startsWith(":") && t.endsWith(":") -> Align.CENTER
                            t.endsWith(":") -> Align.RIGHT
                            else -> Align.LEFT
                        }
                    }
                    val rows = mutableListOf<List<List<Span>>>()
                    i += 2
                    while (i < lines.size && lines[i].isNotBlank() && lines[i].contains('|')) {
                        val cells = splitRow(lines[i])
                        rows += List(header.size) { c -> inline(cells.getOrElse(c) { "" }) }
                        i++
                    }
                    out += MdBlock.Table(header.map { inline(it) }, aligns, rows)
                    continue
                }
            }

            val li = listItem.matchEntire(line)
            if (li != null && !rule.matches(line)) {
                flushPara()
                val items = mutableListOf<ListItem>()
                while (i < lines.size) {
                    val m = listItem.matchEntire(lines[i])
                    if (m != null && !rule.matches(lines[i])) {
                        val level = indentLevel(m.groupValues[1])
                        val rawMarker = m.groupValues[2]
                        var content = m.groupValues[3]
                        var checked: Boolean? = null
                        task.matchEntire(content)?.let { t ->
                            checked = t.groupValues[1] != " "
                            content = t.groupValues[2]
                        }
                        val marker = if (rawMarker[0].isDigit()) rawMarker else if (level > 0) "◦" else "•"
                        items += ListItem(marker, level, inline(content), checked)
                        i++
                    } else if (lines[i].isNotBlank() && lines[i].startsWith(" ") && items.isNotEmpty() &&
                        !lines[i].trimStart().startsWith(">")
                    ) {
                        // continuation line of the previous item
                        val last = items.removeAt(items.lastIndex)
                        items += last.copy(spans = last.spans + inline("\n" + lines[i].trim()))
                        i++
                    } else {
                        break
                    }
                }
                out += MdBlock.Items(items)
                continue
            }

            para.add(line)
            i++
        }
        flushPara()
        return out
    }

    private fun indentLevel(ws: String): Int {
        var width = 0
        for (ch in ws) width += if (ch == '\t') 4 else 1
        return (width / 2).coerceAtMost(4)
    }

    /** Split a table row on unescaped pipes that are not inside inline code. */
    internal fun splitRow(row: String): List<String> {
        var s = row.trim()
        if (s.startsWith("|")) s = s.substring(1)
        if (s.endsWith("|") && !s.endsWith("\\|")) s = s.dropLast(1)
        val cells = mutableListOf<String>()
        val sb = StringBuilder()
        var inCode = false
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '\\' && i + 1 < s.length && s[i + 1] == '|' -> {
                    sb.append('|')
                    i++
                }
                c == '`' -> {
                    inCode = !inCode
                    sb.append(c)
                }
                c == '|' && !inCode -> {
                    cells.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        cells.add(sb.toString().trim())
        return cells
    }

    // ---------------------------------------------------------------- inline

    private data class Style(
        val bold: Boolean = false,
        val italic: Boolean = false,
        val strike: Boolean = false,
        val link: String? = null,
    )

    fun inline(s: String): List<Span> {
        val out = mutableListOf<Span>()
        parseInline(s, Style(), out)
        return merge(out)
    }

    private fun merge(spans: List<Span>): List<Span> {
        val out = mutableListOf<Span>()
        for (sp in spans) {
            if (sp.text.isEmpty()) continue
            val last = out.lastOrNull()
            if (last != null && !last.code && !sp.code &&
                last.bold == sp.bold && last.italic == sp.italic && last.strike == sp.strike && last.link == sp.link
            ) {
                out[out.lastIndex] = last.copy(text = last.text + sp.text)
            } else {
                out += sp
            }
        }
        return out
    }

    private fun safeUrl(u: String): Boolean {
        val l = u.trim().lowercase()
        return l.startsWith("http://") || l.startsWith("https://") || l.startsWith("mailto:")
    }

    private fun parseInline(s: String, st: Style, out: MutableList<Span>) {
        val sb = StringBuilder()
        fun flush() {
            if (sb.isNotEmpty()) {
                out += Span(sb.toString(), st.bold, st.italic, st.strike, false, st.link)
                sb.clear()
            }
        }

        var i = 0
        while (i < s.length) {
            val c = s[i]

            // backslash escape
            if (c == '\\' && i + 1 < s.length && s[i + 1] in "\\`*_{}[]()#+-.!|~<>") {
                sb.append(s[i + 1])
                i += 2
                continue
            }

            // inline code (any run length of backticks)
            if (c == '`') {
                var n = 0
                while (i + n < s.length && s[i + n] == '`') n++
                val fence = "`".repeat(n)
                val end = s.indexOf(fence, i + n)
                if (end > i + n) {
                    flush()
                    out += Span(s.substring(i + n, end).trim(), code = true, link = st.link)
                    i = end + n
                    continue
                }
                sb.append(fence)
                i += n
                continue
            }

            // [text](url)
            if (c == '[') {
                val close = findBracketEnd(s, i)
                if (close > 0 && close + 1 < s.length && s[close + 1] == '(') {
                    val endParen = s.indexOf(')', close + 2)
                    if (endParen > 0) {
                        val url = s.substring(close + 2, endParen).trim().substringBefore(' ')
                        if (safeUrl(url)) {
                            flush()
                            parseInline(s.substring(i + 1, close), st.copy(link = url), out)
                            i = endParen + 1
                            continue
                        }
                    }
                }
            }

            // <https://autolink>
            if (c == '<') {
                val end = s.indexOf('>', i + 1)
                if (end > i + 1) {
                    val url = s.substring(i + 1, end)
                    if (safeUrl(url) && !url.contains(' ')) {
                        flush()
                        out += Span(url, bold = st.bold, italic = st.italic, strike = st.strike, link = url)
                        i = end + 1
                        continue
                    }
                }
            }

            // bare http(s) URL
            if ((c == 'h') && st.link == null && (s.startsWith("http://", i) || s.startsWith("https://", i)) &&
                (i == 0 || !s[i - 1].isLetterOrDigit())
            ) {
                var end = i
                while (end < s.length && !s[end].isWhitespace() && s[end] != '<' && s[end] != '>') end++
                while (end > i && s[end - 1] in ".,;:!?)]}'\"") end--
                if (end > i + 8) {
                    flush()
                    val url = s.substring(i, end)
                    out += Span(url, bold = st.bold, italic = st.italic, strike = st.strike, link = url)
                    i = end
                    continue
                }
            }

            // emphasis: ***, **, __, *, _, ~~
            val delim = when {
                s.startsWith("***", i) -> "***"
                s.startsWith("**", i) -> "**"
                s.startsWith("__", i) -> "__"
                s.startsWith("~~", i) -> "~~"
                c == '*' -> "*"
                c == '_' -> "_"
                else -> null
            }
            if (delim != null && canOpen(s, i, delim)) {
                val close = findClose(s, i + delim.length, delim)
                if (close > 0) {
                    flush()
                    val inner = s.substring(i + delim.length, close)
                    val next = when (delim) {
                        "***" -> st.copy(bold = true, italic = true)
                        "**", "__" -> st.copy(bold = true)
                        "~~" -> st.copy(strike = true)
                        else -> st.copy(italic = true)
                    }
                    parseInline(inner, next, out)
                    i = close + delim.length
                    continue
                }
            }

            sb.append(c)
            i++
        }
        flush()
    }

    private fun findBracketEnd(s: String, open: Int): Int {
        var depth = 0
        var i = open
        while (i < s.length) {
            when (s[i]) {
                '\\' -> i++
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) return i
                }
                '`' -> {
                    val end = s.indexOf('`', i + 1)
                    if (end > 0) i = end
                }
            }
            i++
        }
        return -1
    }

    private fun canOpen(s: String, i: Int, delim: String): Boolean {
        val after = i + delim.length
        if (after >= s.length || s[after].isWhitespace()) return false
        if (delim[0] == '_') {
            // no intraword underscores: snake_case stays literal
            if (i > 0 && s[i - 1].isLetterOrDigit()) return false
        }
        if (delim == "*" || delim == "_") {
            // a lone marker must not be the start of a longer run
            if (after < s.length && s[after] == delim[0]) return false
        }
        return true
    }

    private fun findClose(s: String, from: Int, delim: String): Int {
        var j = from
        while (j < s.length) {
            val c = s[j]
            if (c == '\\') {
                j += 2
                continue
            }
            if (c == '`') {
                val end = s.indexOf('`', j + 1)
                if (end > 0) {
                    j = end + 1
                    continue
                }
            }
            if (s.startsWith(delim, j) && j > from && !s[j - 1].isWhitespace()) {
                val after = j + delim.length
                val single = delim == "*" || delim == "_"
                val blocked = single && (
                    (after < s.length && s[after] == delim[0]) ||
                        (s[j - 1] == delim[0])
                    )
                val intraword = delim[0] == '_' && after < s.length && s[after].isLetterOrDigit()
                if (!blocked && !intraword) return j
            }
            j++
        }
        return -1
    }
}
