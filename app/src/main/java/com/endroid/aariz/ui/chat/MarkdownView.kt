package com.endroid.aariz.ui.chat

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

/** Remembers, for this app session, that the user agreed to send code to the online runner. */
private object RemoteConsent {
    var granted by mutableStateOf(false)
}

/**
 * Renders an assistant reply as Markdown. Text can be selected and copied, but buttons and
 * labels cannot (they sit inside [DisableSelection]).
 */
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val blocks = remember(text) { Markdown.parse(text) }
    SelectionContainer(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            blocks.forEach { BlockView(it) }
        }
    }
}

@Composable
private fun BlockView(block: MdBlock) {
    val colors = MaterialTheme.colorScheme
    when (block) {
        is MdBlock.Heading -> Text(
            rich(block.spans),
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineSmall
                2 -> MaterialTheme.typography.titleLarge
                3 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            },
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier.padding(top = if (block.level <= 2) 6.dp else 2.dp),
        )
        is MdBlock.Paragraph -> Text(
            rich(block.spans),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
        )
        is MdBlock.Quote -> Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colors.outline, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                block.blocks.forEach { BlockView(it) }
            }
        }
        is MdBlock.Items -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            block.items.forEach { ItemRow(it) }
        }
        is MdBlock.Table -> TableView(block)
        MdBlock.Rule -> HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
        is MdBlock.Code -> CodeBlock(block)
    }
}

@Composable
private fun ItemRow(item: ListItem) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.padding(start = (item.indent * 18).dp)) {
        val checked = item.checked
        if (checked != null) {
            Text(
                if (checked) "☑" else "☐",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.primary,
                modifier = Modifier.width(26.dp),
            )
        } else {
            Text(
                item.marker,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 22.dp),
            )
        }
        Text(
            rich(item.spans),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TableView(t: MdBlock.Table) {
    val border = MaterialTheme.colorScheme.outlineVariant
    val widths = remember(t) {
        List(t.header.size) { c ->
            val longest = (listOf(t.header[c]) + t.rows.map { it[c] })
                .maxOf { cell -> cell.joinToString("") { it.text }.length }
            (longest * 8 + 28).coerceIn(72, 240).dp
        }
    }
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier
            .clip(shape)
            .border(1.dp, border, shape)
            .horizontalScroll(rememberScrollState()),
    ) {
        TableRow(t.header.map { cell -> cell.map { it.copy(bold = true) } }, widths, t.aligns, shaded = true)
        t.rows.forEachIndexed { index, row ->
            HorizontalDivider(color = border)
            TableRow(row, widths, t.aligns, shaded = index % 2 == 1)
        }
    }
}

@Composable
private fun TableRow(
    cells: List<List<Span>>,
    widths: List<androidx.compose.ui.unit.Dp>,
    aligns: List<Align>,
    shaded: Boolean,
) {
    val bg = if (shaded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent
    Row(Modifier.background(bg)) {
        cells.forEachIndexed { c, spans ->
            Text(
                rich(spans),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = when (aligns.getOrElse(c) { Align.LEFT }) {
                    Align.LEFT -> TextAlign.Start
                    Align.CENTER -> TextAlign.Center
                    Align.RIGHT -> TextAlign.End
                },
                modifier = Modifier
                    .width(widths[c])
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun rich(spans: List<Span>): AnnotatedString {
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary
    return remember(spans, codeBg, linkColor) { buildRich(spans, codeBg, linkColor) }
}

private fun buildRich(spans: List<Span>, codeBg: Color, linkColor: Color): AnnotatedString =
    buildAnnotatedString {
        spans.forEach { sp ->
            val style = SpanStyle(
                fontWeight = if (sp.bold) FontWeight.Bold else null,
                fontStyle = if (sp.italic) FontStyle.Italic else null,
                textDecoration = if (sp.strike) TextDecoration.LineThrough else null,
                fontFamily = if (sp.code) FontFamily.Monospace else null,
                fontSize = if (sp.code) 14.sp else TextUnit.Unspecified,
                background = if (sp.code) codeBg else Color.Unspecified,
            )
            val url = sp.link
            if (url != null) {
                val linkStyle = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                withLink(LinkAnnotation.Url(url, TextLinkStyles(linkStyle))) {
                    withStyle(style) { append(sp.text) }
                }
            } else {
                withStyle(style) { append(sp.text) }
            }
        }
    }

@Composable
private fun CodeBlock(block: MdBlock.Code) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kind = remember(block.lang) { RunLogic.kind(block.lang) }
    var copied by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var output by remember(block.code) { mutableStateOf<String?>(null) }
    var preview by remember(block.code) { mutableStateOf(false) }
    var askRemote by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    fun execute() {
        running = true
        scope.launch {
            output = try {
                when (kind) {
                    RunKind.LOCAL_JS -> CodeRunner.runJs(context, block.code)
                    RunKind.JSON -> CodeRunner.formatJson(block.code)
                    RunKind.REMOTE -> RemoteRunner.run(block.lang, block.code)
                    else -> ""
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
            running = false
        }
    }

    if (askRemote) {
        AlertDialog(
            onDismissRequest = { askRemote = false },
            title = { Text("Run online?") },
            text = {
                Text(
                    "${RunLogic.SERVICE} will run this ${block.lang} code on its servers. " +
                        "The code is sent over the internet, so don't run anything private.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askRemote = false
                    RemoteConsent.granted = true
                    execute()
                }) { Text("Run") }
            },
            dismissButton = { TextButton(onClick = { askRemote = false }) { Text("Cancel") } },
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            DisableSelection {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        block.lang.ifBlank { "code" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (kind != RunKind.NONE) {
                        TextButton(
                            enabled = !running,
                            onClick = {
                                when {
                                    kind == RunKind.PREVIEW -> preview = !preview
                                    kind == RunKind.REMOTE && !RemoteConsent.granted -> askRemote = true
                                    else -> execute()
                                }
                            },
                        ) {
                            Icon(Lucide.Play, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                when {
                                    running -> "Running…"
                                    kind == RunKind.PREVIEW -> if (preview) "Hide" else "Preview"
                                    kind == RunKind.JSON -> "Format"
                                    else -> "Run"
                                },
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(block.code))
                            copied = true
                        },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            if (copied) Lucide.Check else Lucide.Copy,
                            if (copied) "Copied" else "Copy code",
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                block.code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            )
            AnimatedVisibility(visible = preview) {
                HtmlPreview(RunLogic.previewHtml(block.lang, block.code))
            }
            AnimatedVisibility(visible = output != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    DisableSelection {
                        Text(
                            "Output",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        output.orEmpty(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/** Sandboxed preview for html / svg blocks: scripts may run, but nothing can load from the network. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlPreview(html: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.blockNetworkLoads = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.domStorageEnabled = false
                setBackgroundColor(android.graphics.Color.WHITE)
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse =
                        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                }
                loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        },
        onRelease = { it.destroy() },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}
