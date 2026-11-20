package com.endroid.aariz.network

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/** Pure helpers for the network layer (no Android classes, so they are unit tested). */
object ApiLogic {

    /** System instructions. [today] is an ISO date such as 2026-11-19. */
    fun systemPrompt(today: String): String = """
You are Aariz, a fast, clear AI assistant by Project Endroid (Mudasir Wazir).
Today is $today.

Style:
- Answer directly in short, mobile-readable replies. Plain language, no filler.
- The app renders full Markdown: **bold**, *italic*, ~~strike~~, lists, task lists, tables, > quotes, links and fenced code blocks. Use a table only for a real comparison. Keep headings small and rare.
- Always put code in fenced blocks with a language tag (for example ```python). Give complete, runnable snippets that print their result: users can tap Run on js, json, html and svg blocks (on the device) and on python, c, cpp, java, go, rust, ruby, php, bash, kotlin and typescript blocks (online).
- Reply in the user's language.

Tools and honesty:
- Use calculate for exact math and logic. Use web_search (Wikipedia) for facts, people, places and anything that may have changed.
- If a search finds nothing useful, or you are unsure, say so instead of guessing. Never invent sources, quotes or numbers.
- If asked who you are, say you are Aariz by Project Endroid.
""".trim()

    /** Flux image URL (same pattern as the web app). */
    fun imageUrl(prompt: String, seed: Int? = null): String {
        val q = URLEncoder.encode(prompt, Charsets.UTF_8.name())
        val extra = if (seed != null) "&seed=$seed" else ""
        return "https://api.omegatech.app/api/ai/flux?prompt=$q$extra"
    }

    /** Second keyless image source, used when the first one fails to load. */
    fun pollinationsUrl(prompt: String, seed: Int? = null): String {
        val q = URLEncoder.encode(prompt, Charsets.UTF_8.name()).replace("+", "%20")
        val extra = if (seed != null) "&seed=$seed" else ""
        return "https://image.pollinations.ai/prompt/$q?width=1024&height=1024&nologo=true$extra"
    }

    /** The prompt and seed that [url] (made by [imageUrl]) was built from. */
    fun parseImageUrl(url: String): Pair<String, Int?>? = try {
        val query = URI(url).rawQuery ?: return null
        val params = query.split('&').mapNotNull {
            val i = it.indexOf('=')
            if (i <= 0) null else it.substring(0, i) to URLDecoder.decode(it.substring(i + 1), Charsets.UTF_8.name())
        }.toMap()
        val prompt = params["prompt"] ?: return null
        prompt to params["seed"]?.toIntOrNull()
    } catch (e: Exception) {
        null
    }

    /** Alternative URL for an image that failed to load, or null when there is none. */
    fun fallbackImageUrl(url: String): String? {
        if (!url.startsWith("https://api.omegatech.app/")) return null
        val (prompt, seed) = parseImageUrl(url) ?: return null
        return pollinationsUrl(prompt, seed)
    }
}
