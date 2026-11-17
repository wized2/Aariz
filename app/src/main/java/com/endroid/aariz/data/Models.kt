package com.endroid.aariz.data

import java.util.UUID

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ChatMode { CHAT, WEB_SEARCH, IMAGE }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" | "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New chat",
    val messages: List<Message> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
)
