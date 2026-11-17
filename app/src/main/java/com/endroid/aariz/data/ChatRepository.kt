package com.endroid.aariz.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore("aariz_prefs")

class ChatRepository(private val context: Context) {
    private val chatsKey = stringPreferencesKey("chats_json")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val activeKey = stringPreferencesKey("active_chat_id")

    val chats: Flow<List<Conversation>> = context.dataStore.data.map { prefs ->
        parseChats(prefs[chatsKey] ?: "[]")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val activeChatId: Flow<String?> = context.dataStore.data.map { it[activeKey] }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit {
            it[themeKey] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

    suspend fun setActive(id: String) {
        context.dataStore.edit { it[activeKey] = id }
    }

    suspend fun saveChats(list: List<Conversation>) {
        context.dataStore.edit { it[chatsKey] = serializeChats(list) }
    }

    private fun parseChats(raw: String): List<Conversation> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val msgs = o.optJSONArray("messages") ?: JSONArray()
                    val messageList = buildList {
                        for (j in 0 until msgs.length()) {
                            val m = msgs.getJSONObject(j)
                            add(
                                Message(
                                    id = m.optString("id"),
                                    role = m.optString("role"),
                                    text = m.optString("text"),
                                    timestamp = m.optLong("timestamp"),
                                )
                            )
                        }
                    }
                    add(
                        Conversation(
                            id = o.optString("id"),
                            title = o.optString("title", "New chat"),
                            messages = messageList,
                            updatedAt = o.optLong("updatedAt"),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeChats(list: List<Conversation>): String {
        val arr = JSONArray()
        for (c in list) {
            val o = JSONObject()
            o.put("id", c.id)
            o.put("title", c.title)
            o.put("updatedAt", c.updatedAt)
            val msgs = JSONArray()
            for (m in c.messages) {
                msgs.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("role", m.role)
                        .put("text", m.text)
                        .put("timestamp", m.timestamp)
                )
            }
            o.put("messages", msgs)
            arr.put(o)
        }
        return arr.toString()
    }
}
