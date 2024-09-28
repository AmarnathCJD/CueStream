package com.amarnath.cuestream.titles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.amarnath.cuestream.R
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
        horizontalArrangement = Arrangement.Center
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

fun dateToTimestamp(date: String): Long {
    val dateParts = date.split("-")
    return if (dateParts.size == 3) {
        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val day = dateParts[2].toInt()
        val cal = java.util.Calendar.getInstance()
        cal.set(year, month - 1, day)
        cal.timeInMillis
    } else {
        0
    }
}

fun sinceDate(date: String): String {
    val dateParts = date.split(" ")
    val curr = java.util.Calendar.getInstance()
    val cal = java.util.Calendar.getInstance()
    println(dateParts)
    cal.set(dateParts[5].toInt(), monthToNum(dateParts[1]), dateParts[2].toInt())

    val diff = curr.timeInMillis - cal.timeInMillis
    return when (val days = diff / (1000 * 60 * 60 * 24)) {
        0L -> {
            "Today"
        }
        1L -> {
            "Yesterday"
        }
        else -> {
            "$days days ago"
        }
    }
}

fun monthToNum(month: String): Int {
    return when (month) {
        "Jan" -> 0
        "Feb" -> 1
        "Mar" -> 2
        "Apr" -> 3
        "May" -> 4
        "Jun" -> 5
        "Jul" -> 6
        "Aug" -> 7
        "Sep" -> 8
        "Oct" -> 9
        "Nov" -> 10
        "Dec" -> 11
        else -> 0
    }
}

fun ottToImage(ott: String): Pair<Int, Color> {
    return when (ott) {
        "Netflix" -> Pair(R.drawable.icons8_netflix_96, Color(0xFFE50914))
        "Amazon Video" -> Pair(R.drawable.icons8_amazon_prime_96, Color(0xFF00A8E1))
        "Disney Plus" -> Pair(R.drawable.icons8_disney_plus_96, Color(0xFF2962FF))
        "Apple TV" -> Pair(R.drawable.icons8_apple_tv_100, Color(0xFFFFFFFF))
        "Hotstar" -> Pair(R.drawable.icons8_hotstar_96, Color(0xFFFFFFFF))
        "YouTube" -> Pair(R.drawable.icons8_youtube_96, Color(0xFFFF0000))
        "Google Play Movies" -> Pair(R.drawable.icons8_google_play_96, Color(0xFF4285F4))
        else -> {
            Pair(R.drawable.popcorn_image, Color(0xFF000000))
        }
    }
}



@Composable
fun LottieLoading(isPlaying: MutableState<Boolean>) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec
            .RawRes(R.raw.load)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying.value,

        speed = 1f,
        restartOnPlay = true
    )

    Column(
        Modifier
            .background(Color.Transparent)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.size(200.dp)
        )
    }
}