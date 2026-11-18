package com.endroid.aariz.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeRunnerTest {

    @Test
    fun onlyJavascriptIsRunnable() {
        assertTrue(CodeRunner.isRunnable("js"))
        assertTrue(CodeRunner.isRunnable(" JavaScript "))
        assertFalse(CodeRunner.isRunnable("python"))
        assertFalse(CodeRunner.isRunnable(""))
    }

    @Test
    fun jsStringEscapesEverythingDangerous() {
        val lit = CodeRunner.jsString("a\"b\\c\nd</script> ")
        assertEquals("\"a\\\"b\\\\c\\nd\\u003c/script>\\u2028\"", lit)
        assertFalse(lit.contains("</script"))
        assertFalse(lit.contains('\n'))
    }

    @Test
    fun htmlEmbedsCodeOnlyAsAString() {
        val html = CodeRunner.buildHtml("console.log('</script><b>')")
        // exactly one closing script tag: the real one
        assertEquals(1, Regex("</script>").findAll(html).count())
        assertTrue(html.contains("Aariz.done"))
    }
}
