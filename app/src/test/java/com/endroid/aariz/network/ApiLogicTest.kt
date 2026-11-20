package com.endroid.aariz.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiLogicTest {

    @Test
    fun systemPromptHasDateIdentityAndToolRules() {
        val p = ApiLogic.systemPrompt("2026-11-19")
        assertTrue(p.contains("Today is 2026-11-19."))
        assertTrue(p.contains("Aariz by Project Endroid"))
        assertTrue(p.contains("web_search"))
        assertTrue(p.contains("calculate"))
        assertTrue(p.contains("Markdown"))
        assertTrue(p.contains("```python"))
        assertFalse(p.startsWith("\n"))
    }

    @Test
    fun imageUrlRoundTrip() {
        val url = ApiLogic.imageUrl("a red fox & a cat", 42)
        val (prompt, seed) = ApiLogic.parseImageUrl(url)!!
        assertEquals("a red fox & a cat", prompt)
        assertEquals(42, seed)
    }

    @Test
    fun imageUrlWithoutSeed() {
        val (prompt, seed) = ApiLogic.parseImageUrl(ApiLogic.imageUrl("apple"))!!
        assertEquals("apple", prompt)
        assertNull(seed)
    }

    @Test
    fun fallbackKeepsPromptAndSeed() {
        val fb = ApiLogic.fallbackImageUrl(ApiLogic.imageUrl("red apple", 7))!!
        assertTrue(fb.startsWith("https://image.pollinations.ai/prompt/red%20apple?"))
        assertTrue(fb.endsWith("&seed=7"))
        assertFalse(fb.contains('+'))
    }

    @Test
    fun noFallbackForOtherHostsOrBadUrls() {
        assertNull(ApiLogic.fallbackImageUrl("https://image.pollinations.ai/prompt/x"))
        assertNull(ApiLogic.fallbackImageUrl("not a url"))
        assertNull(ApiLogic.parseImageUrl("https://api.omegatech.app/api/ai/flux"))
    }

    @Test
    fun unicodePromptSurvives() {
        val (prompt, _) = ApiLogic.parseImageUrl(ApiLogic.imageUrl("سیب کا باغ"))!!
        assertEquals("سیب کا باغ", prompt)
    }
}
