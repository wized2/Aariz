package com.endroid.aariz.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Trash2
import com.endroid.aariz.data.ChatMode
import com.endroid.aariz.data.Message
import com.endroid.aariz.data.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel) {
    val state by vm.uiState.collectAsState()
    val drawerState = rememberDrawerState(
        if (state.drawerOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.drawerOpen) {
        if (state.drawerOpen) drawerState.open() else drawerState.close()
    }
    LaunchedEffect(drawerState.currentValue) {
        vm.setDrawer(drawerState.currentValue == DrawerValue.Open)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Aariz",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "by Project Endroid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text("New chat") },
                        selected = false,
                        onClick = { vm.newChat() },
                        icon = { Icon(Lucide.Plus, contentDescription = null) },
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Chats",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                LazyColumn {
                    items(state.conversations, key = { it.id }) { c ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    c.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            selected = c.id == state.activeId,
                            onClick = { vm.selectChat(c.id) },
                            icon = { Icon(Lucide.MessageSquare, contentDescription = null) },
                            badge = {
                                IconButton(onClick = { vm.deleteChat(c.id) }) {
                                    Icon(
                                        Lucide.Trash2,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                Surface(tonalElevation = 2.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Lucide.Menu, contentDescription = "Menu")
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Aariz",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                when (state.mode) {
                                    ChatMode.CHAT -> "Chat"
                                    ChatMode.WEB_SEARCH -> "Web search"
                                    ChatMode.IMAGE -> "Image"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { vm.cycleTheme() }) {
                            val icon: ImageVector = when (state.themeMode) {
                                ThemeMode.DARK -> Lucide.Moon
                                ThemeMode.LIGHT -> Lucide.Sun
                                ThemeMode.SYSTEM -> Lucide.Sun
                            }
                            Icon(icon, contentDescription = "Theme")
                        }
                        IconButton(onClick = { vm.newChat() }) {
                            Icon(Lucide.Plus, contentDescription = "New chat")
                        }
                    }
                }
            },
            bottomBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    state.error?.let { err ->
                        Text(
                            err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.mode == ChatMode.CHAT,
                            onClick = { vm.setMode(ChatMode.CHAT) },
                            label = { Text("Chat") },
                            leadingIcon = {
                                Icon(Lucide.MessageSquare, null, Modifier.size(16.dp))
                            },
                        )
                        FilterChip(
                            selected = state.mode == ChatMode.WEB_SEARCH,
                            onClick = { vm.setMode(ChatMode.WEB_SEARCH) },
                            label = { Text("Search") },
                            leadingIcon = {
                                Icon(Lucide.Globe, null, Modifier.size(16.dp))
                            },
                        )
                        FilterChip(
                            selected = state.mode == ChatMode.IMAGE,
                            onClick = { vm.setMode(ChatMode.IMAGE) },
                            label = { Text("Image") },
                            leadingIcon = {
                                Icon(Lucide.Image, null, Modifier.size(16.dp))
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        TextField(
                            value = state.input,
                            onValueChange = vm::onInput,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message Aariz…") },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                            maxLines = 5,
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { vm.send() },
                            enabled = state.input.isNotBlank() && !state.sending,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(
                                Lucide.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            MessageList(
                messages = state.messages,
                sending = state.sending,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun MessageList(
    messages: List<Message>,
    sending: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, sending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex + if (sending) 1 else 0)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (messages.isEmpty() && !sending) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Aariz",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Fast, clear answers — by Project Endroid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(messages, key = { it.id }) { msg ->
            MessageBubble(msg)
        }
        if (sending) {
            item {
                Text(
                    "Aariz is thinking…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: Message) {
    val isUser = msg.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp,
            ),
            modifier = Modifier.fillMaxWidth(0.88f),
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
