package com.endroid.aariz.ui.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated thinking indicator:
 * cycles 3 dots → merge to 1 → expand to 3 → expand to 4 orbiting → back to 3.
 */
@Composable
fun ThinkingDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    var phase by remember { mutableIntStateOf(0) }
    // 0: three pulse, 1: merge, 2: three, 3: four orbit, 4: back
    LaunchedEffect(Unit) {
        while (true) {
            phase = 0
            delay(900)
            phase = 1
            delay(500)
            phase = 2
            delay(700)
            phase = 3
            delay(1100)
            phase = 4
            delay(500)
        }
    }

    val spin = rememberInfiniteTransition(label = "spin")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val merge = remember { Animatable(0f) }
    LaunchedEffect(phase) {
        when (phase) {
            1 -> merge.animateTo(1f, tween(450))
            4 -> merge.animateTo(1f, tween(400))
            else -> merge.snapTo(0f)
        }
    }

    Box(modifier = modifier.size(width = 48.dp, height = 20.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(48.dp, 20.dp)) {
            val cy = size.height / 2f
            val r = 3.2.dp.toPx()
            val spread = 9.dp.toPx()
            val m = merge.value

            when (phase) {
                0, 2 -> {
                    // three horizontal dots with pulse
                    for (i in 0..2) {
                        val x = size.width / 2f + (i - 1) * spread
                        val pr = r * if (i == 1) pulseScale else (0.9f + pulseScale * 0.1f)
                        drawCircle(color = color.copy(alpha = 0.85f), radius = pr, center = Offset(x, cy))
                    }
                }
                1, 4 -> {
                    // merge toward center
                    for (i in 0..2) {
                        val fromX = size.width / 2f + (i - 1) * spread
                        val x = fromX + (size.width / 2f - fromX) * m
                        val pr = r * (1f - m * 0.35f)
                        drawCircle(color = color.copy(alpha = 0.9f - m * 0.2f), radius = pr, center = Offset(x, cy))
                    }
                    if (m > 0.6f) {
                        drawCircle(
                            color = color,
                            radius = r * (0.8f + (m - 0.6f) * 1.5f),
                            center = Offset(size.width / 2f, cy),
                        )
                    }
                }
                3 -> {
                    // four dots orbiting
                    val rad = 7.dp.toPx()
                    for (i in 0..3) {
                        val a = Math.toRadians((angle + i * 90.0))
                        val x = size.width / 2f + cos(a).toFloat() * rad
                        val y = cy + sin(a).toFloat() * rad * 0.55f
                        drawCircle(color = color.copy(alpha = 0.9f), radius = r * 0.95f, center = Offset(x, y))
                    }
                }
            }
        }
    }
}
