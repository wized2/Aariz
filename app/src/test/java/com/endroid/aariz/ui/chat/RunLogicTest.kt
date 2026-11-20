package com.endroid.aariz.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunLogicTest {

    @Test
    fun kindsByLanguage() {
        assertEquals(RunKind.LOCAL_JS, RunLogic.kind("js"))
        assertEquals(RunKind.LOCAL_JS, RunLogic.kind(" JavaScript "))
        assertEquals(RunKind.JSON, RunLogic.kind("json"))
        assertEquals(RunKind.PREVIEW, RunLogic.kind("html"))
        assertEquals(RunKind.PREVIEW, RunLogic.kind("SVG"))
        assertEquals(RunKind.REMOTE, RunLogic.kind("python"))
        assertEquals(RunKind.REMOTE, RunLogic.kind("c++"))
        assertEquals(RunKind.REMOTE, RunLogic.kind("kt"))
        assertEquals(RunKind.NONE, RunLogic.kind("brainfuck"))
        assertEquals(RunKind.NONE, RunLogic.kind(""))
    }

    @Test
    fun languageIds() {
        assertEquals(71, RunLogic.languageId("Python"))
        assertEquals(71, RunLogic.languageId("py"))
        assertEquals(62, RunLogic.languageId("java"))
        assertNull(RunLogic.languageId("cobol"))
    }

    @Test
    fun everyAliasMapsToARunnableKind() {
        for (l in listOf("python", "c", "cpp", "java", "go", "rust", "ruby", "php", "bash", "kotlin", "typescript", "csharp", "swift")) {
            assertEquals(l, RunKind.REMOTE, RunLogic.kind(l))
        }
    }

    @Test
    fun jsStringEscapesEverythingDangerous() {
        val lit = RunLogic.jsString("a\"b\\c\nd</script>\u2028")
        assertEquals("\"a\\\"b\\\\c\\nd\\u003c/script>\\u2028\"", lit)
        assertFalse(lit.contains("</script"))
        assertFalse(lit.contains('\n'))
    }

    @Test
    fun controlCharactersAreEscaped() {
        assertEquals("\"\\u0001\"", RunLogic.jsString("\u0001"))
    }

    @Test
    fun templateGetsTheCodeOnlyAsAString() {
        val html = RunLogic.buildHtml("<script>var c = __CODE__;</script>", "console.log('</script><b>')")
        assertEquals(1, Regex("</script>").findAll(html).count())
        assertTrue(html.startsWith("<script>var c = \""))
    }

    @Test
    fun remoteOutputCombinesStreams() {
        val t = RunLogic.formatRemote("hello\n", "warn", null, null, 3, "Accepted", "0.01")
        assertEquals("hello\n\nstderr:\nwarn\n\n(ran online on Judge0 · 0.01s)", t)
    }

    @Test
    fun remoteCompileErrorAndStatus() {
        val t = RunLogic.formatRemote(null, null, "main.c:1: error", null, 6, "Compilation Error", null)
        assertEquals("Compile error:\nmain.c:1: error\n\nStatus: Compilation Error\n\n(ran online on Judge0)", t)
    }

    @Test
    fun remoteEmptyOutput() {
        assertEquals("(no output)\n\n(ran online on Judge0)", RunLogic.formatRemote(null, "", null, null, 3, "Accepted", null))
    }

    @Test
    fun remoteTimeLimitStatusIsShown() {
        val t = RunLogic.formatRemote("", null, null, null, 5, "Time Limit Exceeded", "5.0")
        assertTrue(t.contains("Status: Time Limit Exceeded"))
    }

    @Test
    fun svgIsWrappedAndHtmlIsKept() {
        assertTrue(RunLogic.previewHtml("svg", "<svg></svg>").contains("<body><svg></svg></body>"))
        assertEquals("<p>x</p>", RunLogic.previewHtml("html", "<p>x</p>"))
    }
}
