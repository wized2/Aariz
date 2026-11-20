package com.endroid.aariz.ui.chat

import android.content.Intent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Square
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Trash2
import com.endroid.aariz.data.AiModel
import com.endroid.aariz.data.Message
import com.endroid.aariz.network.ApiLogic
import com.endroid.aariz.data.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            var query by remember { mutableStateOf("") }
            val shown = remember(state.conversations, query) {
                state.conversations.filter { query.isBlank() || it.title.contains(query.trim(), ignoreCase = true) }
            }
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Aariz",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "by Project Endroid",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                    Surface(
                        onClick = { vm.newChat() },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Lucide.Plus, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("New chat", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Search chats") },
                        leadingIcon = { Icon(Lucide.Search, null, Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (shown.isEmpty()) "No chats found" else "History",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyColumn(Modifier.weight(1f)) {
                    items(shown, key = { it.id }) { c ->
                        NavigationDrawerItem(
                            label = {
                                Text(c.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            selected = c.id == state.activeId,
                            onClick = { vm.selectChat(c.id) },
                            icon = {
                                Icon(Lucide.MessageSquare, null, Modifier.size(17.dp))
                            },
                            badge = {
                                IconButton(
                                    onClick = { vm.deleteChat(c.id) },
                                    modifier = Modifier.size(30.dp),
                                ) {
                                    Icon(Lucide.Trash2, "Delete", Modifier.size(14.dp))
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp),
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }) {
                        Icon(Lucide.Menu, "Menu")
                    }
                    Text(
                        "Aariz",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(onClick = { vm.cycleTheme() }) {
                        Icon(
                            when (state.themeMode) {
                                ThemeMode.DARK -> Lucide.Moon
                                else -> Lucide.Sun
                            },
                            "Theme",
                        )
                    }
                }
            },
            bottomBar = {
                ComposerBar(
                    input = state.input,
                    model = state.model,
                    sending = state.sending,
                    error = state.error,
                    onInput = vm::onInput,
                    onModel = vm::setModel,
                    onSend = vm::send,
                    onStop = vm::stop,
                    onRetry = vm::retry,
                )
            },
        ) { padding ->
            MessageList(
                messages = state.messages,
                sending = state.sending,
                model = state.model,
                onPick = vm::onInput,
                onRegenerate = vm::regenerate,
                onEdit = vm::editAndResend,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun ComposerBar(
    input: String,
    model: AiModel,
    sending: Boolean,
    error: String?,
    onInput: (String) -> Unit,
    onModel: (AiModel) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
) {
    val canSend = input.isNotBlank() && !sending

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }

        // Single composed input card (Claude-style)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(22.dp),
                ),
        ) {
            Column(Modifier.padding(bottom = 8.dp)) {
                TextField(
                    value = input,
                    onValueChange = onInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp, max = 140.dp),
                    placeholder = {
                        Text(
                            when (model) {
                                AiModel.IMAGE -> "Describe an image to generate…"
                                AiModel.CHAT -> "How can I help you today?"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    maxLines = 6,
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModeToggle(model = model, onModel = onModel)

                    Spacer(Modifier.weight(1f))

                    SendButton(canSend = canSend, sending = sending, onSend = onSend, onStop = onStop)
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<Message>,
    sending: Boolean,
    model: AiModel,
    onPick: (String) -> Unit,
    onRegenerate: () -> Unit,
    onEdit: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val total = messages.size + if (sending) 1 else 0
    LaunchedEffect(messages.size, sending) {
        if (total > 0) listState.animateScrollToItem(total - 1)
    }
    Box(modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (messages.isEmpty() && !sending) {
                item { EmptyHero(model, onPick) }
            }
            itemsIndexed(messages, key = { _, m -> m.id }) { index, msg ->
                MessageBlock(
                    msg = msg,
                    canRegenerate = index == messages.lastIndex && !sending && msg.role == "assistant",
                    onRegenerate = onRegenerate,
                    canEdit = !sending,
                    editing = editingId == msg.id,
                    onStartEdit = { editingId = msg.id },
                    onCancelEdit = { editingId = null },
                    onSaveEdit = { text ->
                        editingId = null
                        onEdit(msg.id, text)
                    },
                    modifier = Modifier.animateItem(),
                )
            }
            if (sending) {
                item(key = "thinking") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .animateItem()
                            .padding(start = 2.dp, top = 4.dp),
                    ) {
                        ThinkingDots()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (model == AiModel.IMAGE) "Creating image…" else "Thinking…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = listState.canScrollForward && total > 0,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        ) {
            Surface(
                onClick = { scope.launch { listState.animateScrollToItem((total - 1).coerceAtLeast(0)) } },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            ) {
                Icon(
                    Lucide.ArrowDown,
                    "Scroll to latest",
                    Modifier
                        .padding(10.dp)
                        .size(18.dp),
                )
            }
        }
    }
}

private val chatStarters = listOf(
    "Explain how vaccines work in simple words",
    "Write a polite follow-up email",
    "Plan a 3-day beginner workout",
    "Help me understand a Kotlin error",
)

private val imageStarters = listOf(
    "A cozy cabin in a snowy forest at dusk",
    "A watercolor fox reading a book",
    "A minimal poster of a mountain sunrise",
)

@Composable
private fun EmptyHero(model: AiModel, onPick: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Aariz",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "A calm space for clear answers.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (if (model == AiModel.IMAGE) imageStarters else chatStarters).forEach { prompt ->
                Surface(
                    onClick = { onPick(prompt) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                ) {
                    Text(
                        prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBlock(
    msg: Message,
    canRegenerate: Boolean,
    onRegenerate: () -> Unit,
    canEdit: Boolean,
    editing: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUser = msg.role == "user"
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        if (!isUser) {
            Text(
                "Aariz",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        if (isUser && editing) {
            EditBox(msg.text, onCancelEdit, onSaveEdit)
        } else if (isUser) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.widthIn(max = 340.dp),
            ) {
                SelectionContainer {
                    Text(
                        msg.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
            if (canEdit) {
                Row(Modifier.padding(top = 2.dp)) {
                    ActionIcon(icon = Lucide.Pencil, description = "Edit message", onClick = onStartEdit)
                }
            }
        } else {
            Column(Modifier.widthIn(max = 520.dp)) {
                if (msg.imageUrl != null) {
                    GeneratedImage(msg.imageUrl, msg.text)
                    if (msg.text.isNotBlank() && msg.text != "Generated image") {
                        Spacer(Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    MarkdownText(msg.text)
                }
                ReplyActions(msg, canRegenerate, onRegenerate)
            }
        }
    }
}

@Composable
private fun ReplyActions(msg: Message, canRegenerate: Boolean, onRegenerate: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    Row(Modifier.padding(top = 2.dp)) {
        if (msg.imageUrl == null) {
            ActionIcon(
                icon = if (copied) Lucide.Check else Lucide.Copy,
                description = if (copied) "Copied" else "Copy reply",
            ) {
                clipboard.setText(AnnotatedString(msg.text))
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                copied = true
            }
        }
        ActionIcon(icon = Lucide.Share2, description = "Share") {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg.imageUrl ?: msg.text)
            }
            context.startActivity(Intent.createChooser(send, null))
        }
        if (canRegenerate) {
            ActionIcon(icon = Lucide.RefreshCw, description = "Regenerate", onClick = onRegenerate)
        }
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            icon,
            description,
            Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Inline editor for one of your own messages. Saving cuts the chat here and asks again. */
@Composable
private fun EditBox(initial: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 1,
            maxLines = 8,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Button(
                onClick = { onSave(text) },
                enabled = text.isNotBlank(),
            ) { Text("Save & resend") }
        }
    }
}

/**
 * Generated image with a shimmering placeholder while it loads. If the first source fails the
 * backup source is tried, then tap-to-retry. A download button appears once it has loaded.
 */
@Composable
private fun GeneratedImage(url: String, description: String) {
    var attempt by remember { mutableIntStateOf(0) }
    var sourceIndex by remember(url) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val sources = remember(url) { listOfNotNull(url, ApiLogic.fallbackImageUrl(url)) }
    val current = sources[sourceIndex.coerceIn(0, sources.lastIndex)]
    var loaded by remember(attempt, current) { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape),
    ) {
        key(attempt, current) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(current).crossfade(350).build(),
                contentDescription = description,
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                loading = { ImagePlaceholder("Creating your image…", loading = true) },
                error = {
                    if (sourceIndex < sources.lastIndex) {
                        LaunchedEffect(Unit) { sourceIndex += 1 }
                        ImagePlaceholder("Trying another source…", loading = true)
                    } else {
                        ImagePlaceholder("Couldn’t load the image. Tap to retry", loading = false) {
                            sourceIndex = 0
                            attempt++
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (loaded) {
            Surface(
                onClick = { ImageSaver.save(context, current) },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            ) {
                Icon(
                    Lucide.Download,
                    "Download image",
                    Modifier
                        .padding(10.dp)
                        .size(20.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholder(label: String, loading: Boolean, onClick: (() -> Unit)? = null) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "shift",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(-700f + 2000f * shift, 0f),
                    end = Offset(2000f * shift, 700f),
                ),
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.5.dp)
            } else {
                Icon(Lucide.RefreshCw, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/** Sliding two-option switch between the chat assistant and image generation. */
@Composable
private fun ModeToggle(model: AiModel, onModel: (AiModel) -> Unit) {
    val segment = 88.dp
    val height = 34.dp
    val x by animateDpAsState(
        targetValue = if (model == AiModel.CHAT) 0.dp else segment,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "modeIndicator",
    )
    Box(
        Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(3.dp),
    ) {
        Box(
            Modifier
                .offset { IntOffset(x.roundToPx(), 0) }
                .size(segment, height)
                .shadow(2.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        )
        Row {
            ModeSegment("Chat", Lucide.Sparkles, model == AiModel.CHAT, segment, height) { onModel(AiModel.CHAT) }
            ModeSegment("Image", Lucide.Image, model == AiModel.IMAGE, segment, height) { onModel(AiModel.IMAGE) }
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "segmentTint",
    )
    Row(
        Modifier
            .size(width, height)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(15.dp), tint = tint)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

/** Round up-arrow send button; turns into a stop square while a reply is generating. */
@Composable
private fun SendButton(canSend: Boolean, sending: Boolean, onSend: () -> Unit, onStop: () -> Unit) {
    val active = canSend || sending
    val haptics = LocalHapticFeedback.current
    val bg by animateColorAsState(
        if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        label = "sendBg",
    )
    val scale by animateFloatAsState(if (active) 1f else 0.9f, label = "sendScale")
    val tint = if (active) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    Box(
        Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = active) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (sending) onStop() else onSend()
            },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = sending,
            transitionSpec = {
                (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f)) togetherWith
                    (fadeOut(tween(100)) + scaleOut(targetScale = 0.6f))
            },
            label = "sendIcon",
        ) { isSending ->
            Icon(
                if (isSending) Lucide.Square else Lucide.ArrowUp,
                if (isSending) "Stop" else "Send",
                Modifier.size(20.dp),
                tint = tint,
            )
        }
    }
}
