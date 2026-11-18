package com.endroid.aariz.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun inlineBoldAndCode() {
        val s = Markdown.inline("use **fast** `fun main` now")
        assertEquals(
            listOf(
                Span("use "),
                Span("fast", bold = true),
                Span(" "),
                Span("fun main", code = true),
                Span(" now"),
            ),
            s,
        )
    }

    @Test
    fun unmatchedMarkersStayLiteral() {
        assertEquals(listOf(Span("a ** b ` c")), Markdown.inline("a ** b ` c"))
    }

    @Test
    fun codeFenceBecomesCodeBlock() {
        val blocks = Markdown.parse("Intro\n```kotlin\nval x = 1\nval y = 2\n```\nOutro")
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Text)
        assertEquals(MdBlock.Code("kotlin", "val x = 1\nval y = 2"), blocks[1])
        assertTrue(blocks[2] is MdBlock.Text)
    }

    @Test
    fun unclosedFenceStillRendersAsCode() {
        val blocks = Markdown.parse("```\nstreaming...")
        assertEquals(listOf<MdBlock>(MdBlock.Code("", "streaming...")), blocks)
    }

    @Test
    fun headingIsBoldAndBulletGetsDot() {
        val h = Markdown.line("## Title")
        assertEquals(listOf(Span("Title", bold = true)), h.spans)
        val b = Markdown.line("- item **one**")
        assertEquals(listOf(Span("• "), Span("item "), Span("one", bold = true)), b.spans)
    }

    @Test
    fun plainTextIsOneBlock() {
        val blocks = Markdown.parse("hello\nworld")
        assertEquals(1, blocks.size)
        assertEquals(2, (blocks[0] as MdBlock.Text).lines.size)
    }
}
