package com.amarnath.cuestream.titles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay

fun numHalfFullAndEmptyStars(rating: Double): Triple<Int, Int, Int> {
    val fullStars = rating.toInt() / 2
    val halfStars = if (rating % 2 == 0.0) 0 else 1
    val emptyStars = 5 - fullStars - halfStars
    return Triple(fullStars, halfStars, emptyStars)
}

fun trimLongPlot(plot: String): String {
    return if (plot.length > 200) {
        plot.substring(0, 200) + "..."
    } else {
        plot
    }
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun ComposableLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    durationMillis: Int = 8000
) {
    val scrollState = rememberScrollState(0)
    LaunchedEffect(Unit) {
        while (true) {
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(durationMillis, easing = LinearEasing)
            )
            delay(1000) // Pause before scrolling back
            scrollState.scrollTo(0)
        }
    }
    Row(
        modifier = modifier
            .horizontalScroll(scrollState, false)
            .fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            modifier = Modifier
        )
    }
}
