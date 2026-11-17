package com.endroid.aariz

import com.endroid.aariz.data.Conversation
import com.endroid.aariz.data.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    @Test
    fun conversationStartsEmpty() {
        val c = Conversation(title = "Test")
        assertTrue(c.messages.isEmpty())
        assertEquals("Test", c.title)
    }

    @Test
    fun messageRoles() {
        val u = Message(role = "user", text = "hi")
        val a = Message(role = "assistant", text = "hello")
        assertEquals("user", u.role)
        assertEquals("assistant", a.role)
    }
}
