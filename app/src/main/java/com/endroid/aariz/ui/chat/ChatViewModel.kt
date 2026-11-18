package com.endroid.aariz.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.endroid.aariz.data.AiModel
import com.endroid.aariz.data.ChatRepository
import com.endroid.aariz.data.Conversation
import com.endroid.aariz.data.Message
import com.endroid.aariz.data.ThemeMode
import com.endroid.aariz.network.AarizApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val activeId: String? = null,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val model: AiModel = AiModel.CHAT,
    val drawerOpen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChatRepository(app)

    private val _input = MutableStateFlow("")
    private val _sending = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _model = MutableStateFlow(AiModel.CHAT)
    private val _drawer = MutableStateFlow(false)
    private val _localChats = MutableStateFlow<List<Conversation>>(emptyList())
    private val _active = MutableStateFlow<String?>(null)
    private var requestJob: Job? = null

    val themeMode: StateFlow<ThemeMode> = repo.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    private val core = combine(_localChats, _active, _input, _sending, _error) {
            chats, activeId, input, sending, error ->
        Triple(chats to activeId, input to sending, error)
    }

    val uiState: StateFlow<ChatUiState> = combine(core, _model, _drawer, themeMode) {
            coreVal, model, drawer, theme ->
        val (chatsActive, inputSending, error) = coreVal
        val (chats, activeId) = chatsActive
        val (input, sending) = inputSending
        val active = chats.find { it.id == activeId } ?: chats.firstOrNull()
        ChatUiState(
            conversations = chats.sortedByDescending { it.updatedAt },
            activeId = active?.id,
            messages = active?.messages.orEmpty(),
            input = input,
            sending = sending,
            error = error,
            model = model,
            drawerOpen = drawer,
            themeMode = theme,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    init {
        viewModelScope.launch {
            repo.chats.collect { list ->
                if (list.isEmpty() && _localChats.value.isEmpty()) {
                    val c = Conversation(title = "New chat")
                    _localChats.value = listOf(c)
                    _active.value = c.id
                    repo.saveChats(listOf(c))
                    repo.setActive(c.id)
                } else if (list.isNotEmpty()) {
                    _localChats.value = list
                }
            }
        }
        viewModelScope.launch {
            repo.activeChatId.collect { id ->
                if (id != null) _active.value = id
            }
        }
    }

    fun onInput(s: String) { _input.value = s }
    fun clearError() { _error.value = null }
    fun setDrawer(open: Boolean) { _drawer.value = open }
    fun setModel(model: AiModel) { _model.value = model }

    fun cycleTheme() {
        viewModelScope.launch {
            val next = when (themeMode.value) {
                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.SYSTEM
            }
            repo.setTheme(next)
        }
    }

    fun newChat() {
        val c = Conversation(title = "New chat")
        val list = listOf(c) + _localChats.value
        _localChats.value = list
        _active.value = c.id
        _input.value = ""
        viewModelScope.launch {
            repo.saveChats(list)
            repo.setActive(c.id)
        }
        _drawer.value = false
    }

    fun selectChat(id: String) {
        _active.value = id
        viewModelScope.launch { repo.setActive(id) }
        _drawer.value = false
    }

    fun deleteChat(id: String) {
        val list = _localChats.value.filterNot { it.id == id }.ifEmpty {
            listOf(Conversation(title = "New chat"))
        }
        _localChats.value = list
        if (_active.value == id) _active.value = list.first().id
        viewModelScope.launch {
            repo.saveChats(list)
            repo.setActive(_active.value!!)
        }
    }

    fun send() {
        val text = _input.value.trim()
        if (text.isEmpty() || _sending.value) return
        val activeId = _active.value ?: return
        _input.value = ""
        appendMessage(activeId, Message(role = "user", text = text), titleFrom(text))
        request(activeId, text)
    }

    /** Cancel the reply that is being generated. */
    fun stop() {
        requestJob?.cancel()
    }

    /** Ask again for the last user message after a failure (does not add a new message). */
    fun retry() {
        if (_sending.value) return
        val activeId = _active.value ?: return
        val last = _localChats.value.find { it.id == activeId }?.messages?.lastOrNull() ?: return
        if (last.role != "user") return
        request(activeId, last.text)
    }

    /** The newest message of [activeId] must be the user message that [text] came from. */
    private fun request(activeId: String, text: String) {
        requestJob = viewModelScope.launch {
            _sending.value = true
            _error.value = null
            try {
                if (_model.value == AiModel.IMAGE) {
                    val url = AarizApi.imageUrl(text)
                    appendMessage(
                        activeId,
                        Message(role = "assistant", text = "Generated image", imageUrl = url),
                    )
                } else {
                    val history = _localChats.value.find { it.id == activeId }?.messages.orEmpty()
                    AarizApi.chat(prompt = text, history = history.dropLast(1), webSearch = false).fold(
                        onSuccess = { reply ->
                            appendMessage(activeId, Message(role = "assistant", text = reply.ifBlank { "…" }))
                        },
                        onFailure = { e ->
                            if (e !is kotlinx.coroutines.CancellationException) {
                                _error.value = e.message ?: "Something went wrong"
                            }
                        },
                    )
                }
            } finally {
                _sending.value = false
            }
        }
    }

    private fun titleFrom(text: String): String =
        text.lineSequence().first().trim().take(40).ifBlank { "New chat" }

    private fun appendMessage(chatId: String, msg: Message, newTitle: String? = null) {
        val list = _localChats.value.map { c ->
            if (c.id != chatId) c
            else c.copy(
                messages = c.messages + msg,
                title = when {
                    c.messages.isEmpty() && newTitle != null -> newTitle
                    c.title == "New chat" && newTitle != null -> newTitle
                    else -> c.title
                },
                updatedAt = System.currentTimeMillis(),
            )
        }
        _localChats.value = list
        viewModelScope.launch { repo.saveChats(list) }
    }
}
