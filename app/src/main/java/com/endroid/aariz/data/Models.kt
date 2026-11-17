package com.endroid.aariz.data

import java.util.UUID

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Active generation model — chat assistant or image model. */
enum class AiModel { CHAT, IMAGE }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" | "assistant"
    val text: String,
    /** Optional image URL when this message is an image result. */
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New chat",
    val messages: List<Message> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
)
