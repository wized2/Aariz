package com.endroid.aariz.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    private fun spans(s: String) = Markdown.inline(s)

    // ------------------------------------------------------------ inline

    @Test
    fun boldItalicStrikeAndCode() {
        assertEquals(
            listOf(
                Span("use "),
                Span("fast", bold = true),
                Span(" and "),
                Span("calm", italic = true),
                Span(" and "),
                Span("gone", strike = true),
                Span(" with "),
                Span("fun main", code = true),
            ),
            spans("use **fast** and *calm* and ~~gone~~ with `fun main`"),
        )
    }

    @Test
    fun underscoresWork() {
        assertEquals(listOf(Span("a "), Span("b", bold = true), Span(" "), Span("c", italic = true)), spans("a __b__ _c_"))
    }

    @Test
    fun boldItalicTripleStars() {
        assertEquals(listOf(Span("x", bold = true, italic = true)), spans("***x***"))
    }

    @Test
    fun nestedItalicInsideBold() {
        assertEquals(
            listOf(Span("a ", bold = true), Span("b", bold = true, italic = true), Span(" c", bold = true)),
            spans("**a *b* c**"),
        )
    }

    @Test
    fun snakeCaseAndMathStayLiteral() {
        assertEquals(listOf(Span("my_var_name")), spans("my_var_name"))
        assertEquals(listOf(Span("2 * 3 * 4")), spans("2 * 3 * 4"))
    }

    @Test
    fun unmatchedMarkersStayLiteral() {
        assertEquals(listOf(Span("a ** b ` c")), spans("a ** b ` c"))
    }

    @Test
    fun backslashEscapes() {
        assertEquals(listOf(Span("*not italic*")), spans("\\*not italic\\*"))
    }

    @Test
    fun codeIsNotParsedForEmphasis() {
        assertEquals(listOf(Span("**x**", code = true)), spans("`**x**`"))
    }

    @Test
    fun linksAndAutolinks() {
        val s = spans("see [docs](https://a.dev/x) or https://b.dev/y. ok")
        assertEquals(Span("docs", link = "https://a.dev/x"), s[1])
        assertEquals(Span("https://b.dev/y", link = "https://b.dev/y"), s[3])
        assertEquals(Span(". ok"), s[4])
    }

    @Test
    fun boldInsideLinkText() {
        val s = spans("[**hi**](https://a.dev)")
        assertEquals(listOf(Span("hi", bold = true, link = "https://a.dev")), s)
    }

    @Test
    fun javascriptLinksAreRejected() {
        val s = spans("[x](javascript:alert(1))")
        assertTrue(s.all { it.link == null })
    }

    // ------------------------------------------------------------ blocks

    @Test
    fun headingsLevels() {
        val b = Markdown.parse("# One\n### Three ###")
        assertEquals(MdBlock.Heading(1, listOf(Span("One"))), b[0])
        assertEquals(MdBlock.Heading(3, listOf(Span("Three"))), b[1])
    }

    @Test
    fun codeFenceBecomesCodeBlock() {
        val blocks = Markdown.parse("Intro\n```kotlin\nval x = 1\nval y = 2\n```\nOutro")
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
        assertEquals(MdBlock.Code("kotlin", "val x = 1\nval y = 2"), blocks[1])
        assertTrue(blocks[2] is MdBlock.Paragraph)
    }

    @Test
    fun tildeFenceAndMarkdownInsideCodeIsLiteral() {
        val blocks = Markdown.parse("~~~js\n# not a heading\n**x**\n~~~")
        assertEquals(listOf<MdBlock>(MdBlock.Code("js", "# not a heading\n**x**")), blocks)
    }

    @Test
    fun unclosedFenceStillRendersAsCode() {
        assertEquals(listOf<MdBlock>(MdBlock.Code("", "streaming...")), Markdown.parse("```\nstreaming..."))
    }

    @Test
    fun unorderedAndOrderedLists() {
        val b = Markdown.parse("- one\n- two **b**\n\n1. first\n2. second")
        val u = b[0] as MdBlock.Items
        assertEquals(listOf("•", "•"), u.items.map { it.marker })
        assertEquals(Span("b", bold = true), u.items[1].spans[1])
        val o = b[1] as MdBlock.Items
        assertEquals(listOf("1.", "2."), o.items.map { it.marker })
    }

    @Test
    fun nestedListUsesIndent() {
        val u = Markdown.parse("- a\n  - b\n    - c") [0] as MdBlock.Items
        assertEquals(listOf(0, 1, 2), u.items.map { it.indent })
        assertEquals(listOf("•", "◦", "◦"), u.items.map { it.marker })
    }

    @Test
    fun taskList() {
        val u = Markdown.parse("- [x] done\n- [ ] todo")[0] as MdBlock.Items
        assertEquals(listOf<Boolean?>(true, false), u.items.map { it.checked })
        assertEquals(listOf(Span("done")), u.items[0].spans)
    }

    @Test
    fun blockQuoteIsParsedRecursively() {
        val q = Markdown.parse("> **note**\n> - item")[0] as MdBlock.Quote
        assertEquals(2, q.blocks.size)
        assertTrue(q.blocks[0] is MdBlock.Paragraph)
        assertTrue(q.blocks[1] is MdBlock.Items)
    }

    @Test
    fun horizontalRule() {
        val b = Markdown.parse("a\n\n---\n\nb")
        assertEquals(listOf("P", "R", "P"), b.map { if (it is MdBlock.Rule) "R" else "P" })
    }

    @Test
    fun tableWithAlignmentAndInlineMarkup() {
        val md = "| Name | Qty | Note |\n|:-----|----:|:----:|\n| **Apple** | 3 | `ok` |\n| Pear | 12 |"
        val t = Markdown.parse(md)[0] as MdBlock.Table
        assertEquals(listOf(Align.LEFT, Align.RIGHT, Align.CENTER), t.aligns)
        assertEquals(listOf(Span("Name")), t.header[0])
        assertEquals(2, t.rows.size)
        assertEquals(Span("Apple", bold = true), t.rows[0][0][0])
        assertEquals(Span("ok", code = true), t.rows[0][2][0])
        // short rows are padded to the header width
        assertEquals(3, t.rows[1].size)
        assertTrue(t.rows[1][2].isEmpty())
    }

    @Test
    fun tableWithoutOuterPipes() {
        val t = Markdown.parse("a | b\n--|--\n1 | 2")[0] as MdBlock.Table
        assertEquals(2, t.header.size)
        assertEquals(1, t.rows.size)
    }

    @Test
    fun escapedPipeAndCodePipeStayInCell() {
        assertEquals(listOf("a|b", "`x|y`"), Markdown.splitRow("| a\\|b | `x|y` |"))
    }

    @Test
    fun pipeLineWithoutDelimiterIsAParagraph() {
        val b = Markdown.parse("this | that")
        assertTrue(b[0] is MdBlock.Paragraph)
    }

    @Test
    fun paragraphKeepsLineBreaks() {
        val p = Markdown.parse("hello\nworld")[0] as MdBlock.Paragraph
        assertEquals(listOf(Span("hello\nworld")), p.spans)
    }

    @Test
    fun boldLineIsNotAList() {
        val b = Markdown.parse("**Title**: text")
        assertTrue(b[0] is MdBlock.Paragraph)
    }

    @Test
    fun windowsLineEndings() {
        val b = Markdown.parse("# A\r\n\r\n- x\r\n- y")
        assertTrue(b[0] is MdBlock.Heading)
        assertEquals(2, (b[1] as MdBlock.Items).items.size)
    }

    @Test
    fun emptyInputGivesNoBlocks() {
        assertTrue(Markdown.parse("").isEmpty())
        assertNull(Markdown.parse("   \n\n").firstOrNull())
    }
}
