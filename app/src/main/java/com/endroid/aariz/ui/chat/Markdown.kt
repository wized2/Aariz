package com.endroid.aariz.ui.chat

/** A run of text with inline styling. */
data class Span(val text: String, val bold: Boolean = false, val code: Boolean = false)

/** One visual line made of spans. */
data class MdLine(val spans: List<Span>)

sealed interface MdBlock {
    data class Text(val lines: List<MdLine>) : MdBlock
    data class Code(val lang: String, val code: String) : MdBlock
}

/**
 * Tiny Markdown subset for assistant replies: fenced code blocks, headings, bullet lists,
 * **bold** and `inline code`. Pure Kotlin so it can be unit tested.
 */
object Markdown {
    private val heading = Regex("^\\s{0,3}#{1,6}\\s+(.*)$")
    private val bullet = Regex("^(\\s*)[-*+]\\s+(.*)$")

    fun parse(src: String): List<MdBlock> {
        val out = mutableListOf<MdBlock>()
        val text = mutableListOf<String>()
        var code: MutableList<String>? = null
        var lang = ""

        fun flushText() {
            if (text.isNotEmpty()) {
                out += MdBlock.Text(text.map { line(it) })
                text.clear()
            }
        }

        for (raw in src.lines()) {
            val t = raw.trimStart()
            val open = code
            if (t.startsWith("```")) {
                if (open == null) {
                    flushText()
                    code = mutableListOf()
                    lang = t.removePrefix("```").trim()
                } else {
                    out += MdBlock.Code(lang, open.joinToString("\n"))
                    code = null
                }
            } else if (open != null) {
                open.add(raw)
            } else {
                text.add(raw)
            }
        }
        code?.let { out += MdBlock.Code(lang, it.joinToString("\n")) }
        flushText()
        return out
    }

    fun line(raw: String): MdLine {
        heading.matchEntire(raw)?.let { m ->
            return MdLine(inline(m.groupValues[1]).map { it.copy(bold = true) })
        }
        bullet.matchEntire(raw)?.let { m ->
            return MdLine(listOf(Span(m.groupValues[1] + "• ")) + inline(m.groupValues[2]))
        }
        return MdLine(inline(raw))
    }

    fun inline(s: String): List<Span> {
        val out = mutableListOf<Span>()
        val sb = StringBuilder()
        fun flush() {
            if (sb.isNotEmpty()) {
                out += Span(sb.toString())
                sb.clear()
            }
        }
        var i = 0
        while (i < s.length) {
            if (s.startsWith("**", i)) {
                val end = s.indexOf("**", i + 2)
                if (end > i + 2) {
                    flush()
                    out += Span(s.substring(i + 2, end), bold = true)
                    i = end + 2
                    continue
                }
            } else if (s[i] == '`') {
                val end = s.indexOf('`', i + 1)
                if (end > i + 1) {
                    flush()
                    out += Span(s.substring(i + 1, end), code = true)
                    i = end + 1
                    continue
                }
            }
            sb.append(s[i])
            i++
        }
        flush()
        return out
    }
}
