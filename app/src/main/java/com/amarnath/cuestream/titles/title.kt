package com.amarnath.cuestream.titles

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.amarnath.cuestream.IMDBInst
import com.amarnath.cuestream.R
import com.amarnath.cuestream.meta.JustWatchTitle
import com.amarnath.cuestream.meta.MainTitle
import com.amarnath.cuestream.meta.SaveWatchListEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.max

val ActiveTitleID = mutableStateOf<String?>("tt2442560")
val JwCountry = mutableStateOf<String?>("IN")
val ActiveTitleData = mutableStateOf<MainTitle?>(null)
val JustWatchTitle = mutableStateOf<JustWatchTitle?>(null)
val ActiveTrailerData = mutableStateOf<Pair<String, String>?>(null)
val isActiveLoading = mutableStateOf(true)

@Composable
fun MainTitlePage(padding: PaddingValues, nav: NavController) {
    LaunchedEffect(ActiveTitleID.value) {
        isActiveLoading.value = true
        IMDBInst.getTitle(ActiveTitleID.value!!, ActiveTitleData, isActiveLoading, JustWatchTitle)
    }

    if (isActiveLoading.value) {
        LottieLoading(isActiveLoading)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrailerPlayer()
        if (ActiveTitleID.value?.isNotEmpty() == true && ActiveTitleData.value?.id?.isNotEmpty() == true && ActiveTitleID.value === ActiveTitleData.value!!.id) {
            TitleDetails()
        }
    }
}

@Composable
fun TrailerPlayer() {
    return
    LaunchedEffect(ActiveTitleData.value) {
        if (ActiveTitleData.value != null) {
            val trailer = ActiveTitleData.value!!.trailer
            IMDBInst.getTrailerSource(trailer, ActiveTrailerData)
        }
    }

    val trailerData = ActiveTrailerData.value
    if (trailerData != null) {
        val (thumbnail, source) = trailerData
        VideoPlayer(source, thumbnail)
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black),
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                strokeCap = StrokeCap.Butt,
                trackColor = Color.White
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(source: String, thumbnail: String) {
    val ctx = LocalContext.current
    val trackSelector = DefaultTrackSelector(ctx)
    val isThumbnailVisible = remember { mutableStateOf(true) }

    Column {
        val player: ExoPlayer = remember {
            ExoPlayer.Builder(ctx).setTrackSelector(trackSelector)
                .build()
                .apply {
                    prepare()
                }
        }

        androidx.compose.runtime.rememberUpdatedState(player)
        val hlsMediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(source))

        ComposableLifecycle { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    player.play()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                }

                else -> {}
            }
        }

        LaunchedEffect(player) {
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        isThumbnailVisible.value = false
                    }
                }
            })
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        player.setMediaSource(hlsMediaSource)
                        player.playWhenReady = true
                        setShowSubtitleButton(false)
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowRewindButton(false)
                        setShowFastForwardButton(false)
                        setShutterBackgroundColor(255)

                        useController = true
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            )

            if (isThumbnailVisible.value) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@kotlin.OptIn(ExperimentalLayoutApi::class)
@Composable
fun TitleDetails() {
    val titleData = ActiveTitleData.value
    val ctx = LocalContext.current
    if (titleData != null) {
        val showModalOfImage = remember { mutableStateOf(false) }
        val showModalOfPlot = remember { mutableStateOf(false) }
        val showModalOfAddToWatchlist = remember { mutableStateOf(false) }

        Box {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x08DCDCDC),
                                Color(0xFF0B0C0B),
                                Color(0xFF0B0C0B),
                                Color(0xFF0B0C0B),
                                Color(0xFF0B0C0B),
                                Color(0x08DCDCDC),
                            )
                        )
                    )
                    .padding(bottom = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val showShimmer = remember { mutableStateOf(true) }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 16.dp)
                ) {
                    AsyncImage(
                        model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(titleData.poster)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Movie Poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                shimmerBrush(
                                    targetValue = 1300f,
                                    showShimmer = showShimmer.value
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showModalOfImage.value = true }
                            )
                            .background(
                                Color(0xFF1F1F1F)
                            )
                            .height(210.dp)
                            .width(130.dp),
                        onSuccess = {
                            showShimmer.value = false
                        }
                    )

                    if (showModalOfImage.value) {
                        Dialog(onDismissRequest = { showModalOfImage.value = false }) {
                            Box(
                                modifier = Modifier
                                    .clickable { showModalOfImage.value },
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(titleData.poster)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Movie Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            shimmerBrush(
                                                targetValue = 1300f,
                                                showShimmer = showShimmer.value
                                            )
                                        )
                                        .background(
                                            Color(0xFF1F1F1F)
                                        )
                                        .height(210.dp.times(2))
                                        .width(130.dp.times(2)),
                                    onSuccess = {
                                        showShimmer.value = false
                                    }
                                )
                            }
                        }
                    }

                    if (showModalOfAddToWatchlist.value) {
                        Dialog(onDismissRequest = { showModalOfAddToWatchlist.value = false }) {
                            Box(
                                modifier = Modifier
                                    .clickable { showModalOfAddToWatchlist.value = false }
                                    .background(
                                        Color(0xFF1F1F1F),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Add to Watchlist",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.15.sp
                                    )

                                    Text(
                                        text = "${titleData.title} to your watchlist?",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight(600),
                                        letterSpacing = 0.15.sp
                                    )

                                    val priorities = listOf("#FP", "#SP", "#TP")
                                    val statuses =
                                        listOf("Watching", "Completed", "On Hold", "To Watch")
                                    val statusesColors = listOf(
                                        Color(0xFF8BC34A),
                                        Color(0xFF8BC34A),
                                        Color(0xFF8BC34A),
                                        Color(0xFF8BC34A)
                                    )


                                    var fpcheck by remember { mutableStateOf(false) }
                                    var spcheck by remember { mutableStateOf(false) }
                                    var tpcheck by remember { mutableStateOf(false) }

                                    var watchingCheck by remember { mutableStateOf(false) }
                                    var completedCheck by remember { mutableStateOf(false) }
                                    var onHoldCheck by remember { mutableStateOf(false) }
                                    var toWatchCheck by remember { mutableStateOf(false) }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        priorities.forEachIndexed { i, priority ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                                            ) {
                                                Checkbox(
                                                    checked = when (i) {
                                                        0 -> fpcheck
                                                        1 -> spcheck
                                                        2 -> tpcheck
                                                        else -> false
                                                    },
                                                    onCheckedChange = {
                                                        when (i) {
                                                            0 -> {
                                                                fpcheck = it
                                                                if (it) {
                                                                    spcheck = false
                                                                    tpcheck = false
                                                                }
                                                            }

                                                            1 -> {
                                                                spcheck = it
                                                                if (it) {
                                                                    fpcheck = false
                                                                    tpcheck = false
                                                                }
                                                            }

                                                            2 -> {
                                                                tpcheck = it
                                                                if (it) {
                                                                    fpcheck = false
                                                                    spcheck = false
                                                                }
                                                            }
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = Color(0xFF8BC34A),
                                                        uncheckedColor = Color(0xFF8BC34A),
                                                        checkmarkColor = Color.White
                                                    )
                                                )
                                                Text(
                                                    text = priority,
                                                    color = Color(0xFF8BC34A),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.15.sp
                                                )
                                            }
                                        }
                                    }

                                    statuses.forEachIndexed { i, status ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Checkbox(
                                                checked = when (i) {
                                                    0 -> watchingCheck
                                                    1 -> completedCheck
                                                    2 -> onHoldCheck
                                                    3 -> toWatchCheck
                                                    else -> false
                                                },
                                                onCheckedChange = {
                                                    when (i) {
                                                        0 -> {
                                                            watchingCheck = it
                                                            if (it) {
                                                                completedCheck = false
                                                                onHoldCheck = false
                                                                toWatchCheck = false
                                                            }
                                                        }

                                                        1 -> {
                                                            completedCheck = it
                                                            if (it) {
                                                                watchingCheck = false
                                                                onHoldCheck = false
                                                                toWatchCheck = false
                                                            }
                                                        }

                                                        2 -> {
                                                            onHoldCheck = it
                                                            if (it) {
                                                                watchingCheck = false
                                                                completedCheck = false
                                                                toWatchCheck = false
                                                            }
                                                        }

                                                        3 -> {
                                                            toWatchCheck = it
                                                            if (it) {
                                                                watchingCheck = false
                                                                completedCheck = false
                                                                onHoldCheck = false
                                                            }
                                                        }
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = statusesColors[i],
                                                    uncheckedColor = statusesColors[i],
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = status,
                                                color = Color(0xFF8BC34A),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.15.sp
                                            )
                                        }
                                    }

                                    var comment by remember { mutableStateOf("") }
                                    OutlinedTextField(
                                        value = comment,
                                        onValueChange = { comment = it },
                                        label = { Text("Enter a comment") },
                                        textStyle = TextStyle(color = Color.White),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            cursorColor = Color(0xFF8BC34A),
                                        )
                                    )

                                    var priorityValue by remember { mutableStateOf("") }
                                    OutlinedTextField(
                                        value = priorityValue,
                                        onValueChange = {
                                            if (it.all { char -> char.isDigit() } && it.toIntOrNull() in 0..10) {
                                                priorityValue = it
                                            } else if (it.isEmpty()) {
                                                priorityValue = it
                                            }
                                        },
                                        label = { Text("Enter priority (0 - 10)") },
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(color = Color.White),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            cursorColor = Color(0xFF8BC34A),
                                        )
                                    )

                                    ElevatedButton(
                                        onClick = {
                                            val newWLObj = WLEntry(
                                                title = titleData.title,
                                                imdbID = titleData.id,
                                                image = titleData.poster,
                                                rating = titleData.rating,
                                                plot = titleData.description,
                                                duration = titleData.duration,
                                                comment = comment,
                                                date = Date().toString(),
                                                year = titleData.releaseDate,
                                                priority = when {
                                                    priorityValue.isEmpty() -> 0
                                                    else -> priorityValue.toInt()
                                                },
                                                doneTill = 0,
                                                priorityClass = when {
                                                    fpcheck -> "#FP"
                                                    spcheck -> "#SP"
                                                    tpcheck -> "#TP"
                                                    else -> "#TP"
                                                },
                                                status = when {
                                                    watchingCheck -> "Watching"
                                                    completedCheck -> "Completed"
                                                    onHoldCheck -> "On Hold"
                                                    toWatchCheck -> "To Watch"
                                                    else -> "To Watch"
                                                },
                                                genres = titleData.genres.split(", "),
                                                type = "Movie"
                                            )

                                            showModalOfAddToWatchlist.value = false
                                            ActiveWatchListEntries.add(newWLObj)
                                            SaveWatchListEntry(newWLObj, ctx)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8BC34A)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Add to Watchlist",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = titleData.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.15.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = titleData.releaseDate,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight(500),
                                letterSpacing = 0.15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = titleData.viewerClass.ifEmpty { "HD" },
                                style = TextStyle(color = Color(0xFFE0E0E0)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight(600),
                                modifier = Modifier
                                    .background(
                                        Color(0xFF525047),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp)
                            )
                        }
                        Text(
                            text = titleData.genres,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight(700)
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 3.dp)
                        ) {
                            Text(
                                text = "${titleData.rating} / 10",
                                style = TextStyle(color = Color(0xFFFFD54F)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight(600),
                                modifier = Modifier
                                    .padding(top = 1.dp.div(2))
                                    .background(
                                        Color(0xFF343C2B),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            if (titleData.ratingCount != "0") {
                                Text(
                                    text = "(${titleData.ratingCount})",
                                    style = TextStyle(
                                        color = Color(
                                            0xFFFFD54F
                                        )
                                    ),
                                    color = Color(0xFFA0A0A0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight(600),
                                    modifier = Modifier
                                        .padding(start = 4.dp, top = 4.dp)

                                )
                            }
                            val (fullStars, halfStars, emptyStars) = numHalfFullAndEmptyStars(
                                titleData.rating
                            )
                            Row(
                                modifier = Modifier.padding(start = 4.dp, bottom = 0.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                repeat(fullStars) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Full Star",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (halfStars == 1) {
                                    Image(
                                        painter = painterResource(id = R.drawable.star_half_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                        contentDescription = "Half Star",
                                        modifier = Modifier.size(18.dp),
                                        colorFilter = ColorFilter.tint(
                                            Color(
                                                0xFFFFD54F
                                            )
                                        )
                                    )
                                }
                                repeat(emptyStars) {
                                    Image(
                                        painter = painterResource(id = R.drawable.star_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                        contentDescription = "Half Star",
                                        modifier = Modifier.size(18.dp),
                                        colorFilter = ColorFilter.tint(
                                            Color(
                                                0xFFFFD54F
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        if (titleData.rottenMeter.critic > 0) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (titleData.metaScore > 0) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(
                                                Color(0xFF212121),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        Text(
                                            text = "Metascore",
                                            color = Color(0xFF8BC34A),
                                            style = TextStyle(
                                                color = Color(
                                                    0xFFFFD54F
                                                )
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight(600),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .padding(
                                                    start = 8.dp,
                                                    end = 0.dp,
                                                    top = 1.dp.div(4),
                                                    bottom = 1.dp.div(4)
                                                )
                                        )
                                        Text(
                                            text = "${titleData.metaScore}",
                                            color = Color(0xFF642F28),
                                            style = TextStyle(
                                                color = Color(
                                                    0xFFFFD54F
                                                )
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight(600),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                                .background(
                                                    Color.Yellow.copy(0.7f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(
                                                    end = 4.dp,
                                                    start = 4.dp,
                                                    top = 1.dp.div(4),
                                                    bottom = 1.dp.div(4)
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp, start = 2.dp)
                                        .background(
                                            Color(0xFF212121),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = if (titleData.rottenMeter.critic > 59) {
                                                R.drawable.tomato_image
                                            } else {
                                                R.drawable.tomato_low
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(22.dp)
                                            .size(22.dp)
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                            .clickable { },
                                        contentScale = ContentScale.Crop,
                                    )
                                    Text(
                                        text = "${titleData.rottenMeter.critic}%",
                                        color = Color(0xFF8BC34A),
                                        style = TextStyle(
                                            color = Color(
                                                0xFFFFD54F
                                            )
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                    )
                                    Image(
                                        painter = painterResource(
                                            id = if (titleData.rottenMeter.audience > 59) {
                                                R.drawable.popcorn_image
                                            } else {
                                                R.drawable.popcorn_low
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(22.dp)
                                            .size(22.dp)
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                            .clickable { },
                                        contentScale = ContentScale.Crop,
                                    )
                                    Text(
                                        text = "${titleData.rottenMeter.audience}%",
                                        color = Color(0xFF8BC34A),
                                        style = TextStyle(
                                            color = Color(
                                                0xFFFFD54F
                                            )
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .padding(end = 4.dp)
                                    )
                                }
                            }
                        } else if (JustWatchTitle.value != null && JustWatchTitle.value!!.imdbId == ActiveTitleData.value!!.id && JustWatchTitle.value!!.tomatoMeter > 0) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp)

                            ) {
                                if (titleData.metaScore > 0) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(
                                                Color(0xFF212121),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        Text(
                                            text = "M",
                                            color = Color(0xFF8BC34A),
                                            style = TextStyle(
                                                color = Color(
                                                    0xFFFFD54F
                                                )
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight(600),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .padding(
                                                    start = 4.dp,
                                                    end = 0.dp,
                                                    top = 1.dp.div(4),
                                                    bottom = 1.dp.div(4)
                                                )
                                        )
                                        Text(
                                            text = "${titleData.metaScore}",
                                            color = Color(0xFF642F28),
                                            style = TextStyle(
                                                color = Color(
                                                    0xFFFFD54F
                                                )
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight(600),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                                .background(
                                                    Color.Yellow.copy(0.7f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(
                                                    end = 4.dp,
                                                    start = 4.dp,
                                                    top = 1.dp.div(4),
                                                    bottom = 1.dp.div(4)
                                                )
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp, start = 2.dp)
                                        .background(
                                            Color(0xFF212121),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = if (JustWatchTitle.value!!.tomatoMeter > 59) {
                                                R.drawable.tomato_image
                                            } else {
                                                R.drawable.tomato_low
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(22.dp)
                                            .size(22.dp)
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                            .clickable { },
                                        contentScale = ContentScale.Crop,
                                    )
                                    Text(
                                        text = "${JustWatchTitle.value!!.tomatoMeter}%",
                                        color = Color(0xFF8BC34A),
                                        style = TextStyle(
                                            color = Color(
                                                0xFFFFD54F
                                            )
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp, start = 2.dp)
                                        .background(
                                            Color(0xFF212121),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Text(
                                        text = "JW-${
                                            JustWatchTitle.value!!.jwRating.times(100).toInt()
                                        }%",
                                        color = Color(0xFF8BC34A),
                                        style = TextStyle(
                                            color = Color(
                                                0xFFFFD54F
                                            )
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .padding(
                                                start = 4.dp,
                                                end = 0.dp,
                                                top = 1.dp.div(4),
                                                bottom = 1.dp.div(4)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }

                        Text(
                            text = trimLongPlot(titleData.description),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight(600),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { showModalOfPlot.value = true }
                        )

                        if (showModalOfPlot.value) {
                            Dialog(onDismissRequest = { showModalOfPlot.value = false }) {
                                Box(
                                    modifier = Modifier
                                        .clickable { showModalOfPlot.value }
                                        .border(
                                            1.dp,
                                            Color(0xFFBB86FC),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            Color(0xFF1F1F1F),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column {
                                        Text(
                                            text = titleData.title,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.15.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = titleData.description,
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            lineHeight = 19.sp,
                                            fontWeight = FontWeight(600),
                                            modifier = Modifier

                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 11.dp),
                    thickness = 1.dp,
                    color = Color(0xFF2E2B2B)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 15.dp,
                            vertical = 6.dp
                        )
                        .padding(
                            bottom = 4.dp
                        )
                        .background(Color.Transparent),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(4.dp)
                            .clickable(
                                enabled = true,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    showModalOfAddToWatchlist.value = true
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.playlist_add_check_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier
                                .height(52.dp)
                                .padding(horizontal = 8.dp, vertical = 15.dp),
                            contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color.White,
                                multiply = Color(0xFFFF5722)
                            )
                        )
                        Text(
                            text = "My List",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(Color.Transparent),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier
                                .height(52.dp)
                                .padding(horizontal = 8.dp, vertical = 15.dp)
                                .clickable { },
                            contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color.White,
                                multiply = Color.White
                            )
                        )
                        Text(
                            text = "Rate",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(Color.Transparent),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier
                                .height(52.dp)
                                .padding(horizontal = 8.dp, vertical = 15.dp)
                                .clickable { },
                            contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color.White,
                                multiply = Color.White
                            )
                        )
                        Text(
                            text = "Share",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                if (JustWatchTitle.value != null && JustWatchTitle.value!!.imdbId == ActiveTitleData.value!!.id) {
                    if (JustWatchTitle.value!!.backDrops.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFF2E2B2B)
                        )

                        val scrollState = rememberLazyListState()
                        val coroutineScope = rememberCoroutineScope()
                        val currIndex = remember { mutableIntStateOf(0) }

                        LaunchedEffect(Unit) {
                            coroutineScope.launch {
                                delay(5000)
                                if (currIndex.intValue < JustWatchTitle.value!!.backDrops.size - 1) {
                                    scrollState.animateScrollToItem(currIndex.intValue + 1)
                                    currIndex.intValue += 1
                                } else {
                                    scrollState.animateScrollToItem(0)
                                    currIndex.intValue = 0
                                }
                            }
                        }

                        LazyRow(
                            state = scrollState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .padding(start = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(JustWatchTitle.value!!.backDrops) { backDrop ->
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data("https://www.justwatch.com/images" + backDrop.replace("{profile}", "s1440").replace("{format}", "webp"))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Backdrop",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    onSuccess = {
                                        showShimmer.value = false
                                    }
                                )
                            }
                        }
                    }

                    if (JustWatchTitle.value!!.offers.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp),
                            thickness = 1.dp,
                            color = Color(0xFF2E2B2B)
                        )
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Watch Now on (" + JwCountry.value + ")",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight(600),
                                    letterSpacing = 0.15.sp
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 10.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                JustWatchTitle.value!!.offers.forEach { platform ->
                                    Column(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF131313),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(
                                                enabled = true,
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(),
                                                onClick = {
//                                                ctx.startActivity(
//                                                    Intent(
//                                                        Intent.ACTION_VIEW,
//                                                        Uri.parse(platform.url)
//                                                    )
//                                                )

                                                    // TODO: Learn this.
                                                }
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .width(80.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val image = ottToImage(platform.name)
                                            Row {
                                                Image(
                                                    painter = painterResource(id = image.first),
                                                    contentDescription = platform.name,
                                                    modifier = Modifier
                                                        .height(45.dp)
                                                        .width(45.dp)
                                                        .padding(4.dp),
                                                    contentScale = ContentScale.Crop,
                                                    colorFilter = if (image.first != R.drawable.popcorn_image && image.first != R.drawable.icons8_youtube_96) ColorFilter.tint(
                                                        image.second
                                                    ) else null
                                                )
                                            }
                                            Row {
                                                MarqueeText(
                                                    text = platform.name,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight(600),
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .width(80.dp)
                                                        .height(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFF2E2B2B)
                        )

                        if (JustWatchTitle.value!!.seasons.isNotEmpty()) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Season & Episodes",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight(600),
                                        letterSpacing = 0.15.sp
                                    )
                                }

                                val selectedChip = remember { mutableIntStateOf(0) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model =
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(
                                                "https://images.justwatch.com" + JustWatchTitle.value!!.seasons[selectedChip.intValue].poster.replace(
                                                    "{profile}",
                                                    "s332"
                                                ).replace("{format}", "webp")
                                            )
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Season Poster",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1F1F1F))
                                            .height(200.dp)
                                            .width(130.dp),
                                        onSuccess = {
                                            showShimmer.value = false
                                        }
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 0.dp, bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            JustWatchTitle.value!!.seasons.reversed()
                                                .forEachIndexed { i, season ->
                                                    FilterChip(
                                                        selected = selectedChip.intValue == i,
                                                        label = {
                                                            Text(
                                                                text = season.seasonName,
                                                                color = Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight(600),
                                                                letterSpacing = 0.15.sp
                                                            )
                                                        },
                                                        onClick = {
                                                            selectedChip.intValue = i
                                                        },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedLabelColor = Color(0xFF8BC34A),
                                                            labelColor = Color.White,
                                                        ),
                                                        border = BorderStroke(
                                                            width = 1.dp,
                                                            color = Color(0xFF8BC34A)
                                                        )
                                                    )
                                                }
                                        }

                                        val count =
                                            JustWatchTitle.value!!.seasons[selectedChip.intValue].episodeCount
                                        val factor = max(count / 2, 4)
                                        val rows = count / factor
                                        val remaining = count % factor

                                        Column (
                                            modifier = Modifier
                                                .height(140.dp)
                                        ){
                                            for (i in 0 until rows) {
                                                Row (
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp)
                                                ){
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.95f)
                                                            .padding(top = 8.dp)
                                                            .horizontalScroll(rememberScrollState()),
                                                    ) {
                                                        for (j in 0 until factor) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .padding(4.dp)
                                                                    .background(
                                                                        Color(0xFF212121),
                                                                        shape = RoundedCornerShape(4.dp)
                                                                    )
                                                                    .width(40.dp)
                                                                    .height(30.dp),
                                                                verticalArrangement = Arrangement.Center,
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                Text(
                                                                    text = "S${selectedChip.intValue + 1}E${(i * factor) + j + 1}",
                                                                    color = Color.White,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight(600),
                                                                    letterSpacing = 0.15.sp
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (factor > 4) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                            contentDescription = "Scroll Right",
                                                            tint = Color(0xFF8BC34A),
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .align(Alignment.CenterVertically)
                                                                .padding(top = 8.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (remaining > 0) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.95f)
                                                        .padding(top = 8.dp),
                                                ) {
                                                    for (i in 0 until remaining) {
                                                        Column(
                                                            modifier = Modifier
                                                                .padding(4.dp)
                                                                .background(
                                                                    Color(0xFF212121),
                                                                    shape = RoundedCornerShape(4.dp)
                                                                )
                                                                .width(40.dp)
                                                                .height(30.dp),
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text(
                                                                text = "S${selectedChip.intValue + 1}E${(rows * factor) + i + 1}",
                                                                color = Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight(600),
                                                                letterSpacing = 0.15.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color(0xFF2E2B2B)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFF111111), shape = RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF212121),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Column(
                        modifier = if (titleData.countryOfOrigin.split(" ").size > 3) {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .height(60.dp)
                                .verticalScroll(rememberScrollState())
                                .weight(1f)
                        } else {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .weight(1f)
                        },
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Country of Origin",
                            fontSize = 11.sp,
                            color = Color(0xFF009688),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = titleData.countryOfOrigin,
                            fontSize = 10.sp,
                            color = Color(0xFFA0A0A0),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }

                    Column(
                        modifier = if (titleData.productionCompanies.split(" ").size > 3) {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .height(60.dp)
                                .verticalScroll(rememberScrollState())
                                .weight(1f)
                        } else {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .weight(1f)
                        },
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Production",
                            fontSize = 11.sp,
                            color = Color(0xFF8BC34A),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = titleData.productionCompanies,
                            fontSize = 10.sp,
                            color = Color(0xFFA0A0A0),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }

                    Column(
                        modifier = if (titleData.languages.split(" ").size > 3) {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .height(60.dp)
                                .verticalScroll(rememberScrollState())
                                .weight(1f)
                        } else {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 4.dp, top = 4.dp)
                                .weight(1f)
                        },
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Languages",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = titleData.languages,
                            fontSize = 10.sp,
                            color = Color(0xFFA0A0A0),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 11.dp),
                    thickness = 1.dp,
                    color = Color(0xFF2E2B2B)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (titleData.titleCasts.any { it.third.isNotEmpty() }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Top Cast",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            titleData.titleCasts.forEach { cast ->
                                if (cast.third.isEmpty()) return@forEach
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .width(120.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = cast.first,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    AsyncImage(
                                        model = cast.third.split("@._V1_")
                                            .first() + "@._V1_QL75_UX280_CR0,25,280,280_.jpg",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .size(100.dp)
                                            .clip(
                                                RoundedCornerShape(64.dp)
                                            ),
                                    )
                                    MarqueeText(
                                        text = "(As ${cast.second})",
                                        fontSize = 8.sp,
                                        color = Color(0xFFA0A0A0),
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFF2E2B2B)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "More Like This",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(titleData.moreLikeThis) { moreLikeThis ->
                            if (moreLikeThis.poster.isEmpty()) return@items
                            Column(
                                modifier = Modifier
                                    .width(120.dp)
                                    .background(Color.Transparent)
                                    .clickable(
                                        enabled = true,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                        onClick = {
                                            ActiveTitleID.value = moreLikeThis.imdbId
                                            ActiveTitleData.value = null
                                            isActiveLoading.value = true
                                            IMDBInst.getTitle(
                                                ActiveTitleID.value!!,
                                                ActiveTitleData,
                                                isActiveLoading,
                                                JustWatchTitle
                                            )
                                        }
                                    ),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(165.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = moreLikeThis.poster.split("@._V1_")
                                            .first() + "@._V1_QL100_UY430_CR5,0_.jpg",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .zIndex(1f),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        text = moreLikeThis.rating.toString(),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                Color(0xFFFFC107).copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 0.dp)
                                            .zIndex(2f),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }


                                MarqueeText(
                                    text = moreLikeThis.title,
                                    fontSize = 10.sp,
                                    color = Color(0xFFA0A0A0),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .width(120.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = titleData.title,
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (titleData.altTitle.isNotEmpty() || titleData.alsoKnownAs.isNotEmpty()) {
                Text(
                    text = titleData.alsoKnownAs.ifEmpty { titleData.altTitle },
                    fontSize = 14.sp,
                    color = Color(0xFFA0A0A0),
                    fontWeight = FontWeight(600),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = titleData.releaseDateLong,
                    fontSize = 11.sp,
                    color = Color(0xFF8F8F8F),
                    fontWeight = FontWeight(600),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            Color(0xFF2E2B2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                )
                Text(
                    text = titleData.countryOfOrigin,
                    fontSize = 11.sp,
                    color = Color(0xFF8F8F8F),
                    fontWeight = FontWeight(600),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            Color(0xFF2E2B2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                )
                Text(
                    text = titleData.languages, fontSize = 11.sp, color = Color(0xFF8F8F8F),
                    fontWeight = FontWeight(600),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            Color(0xFF2E2B2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                )
            }

            Row {
                ElevatedButton(
                    onClick = {
                        showModalOfAddToWatchlist.value = true
                    },
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF973636),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add to Watchlist")
                }
                ElevatedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF206B64),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Text("Get OTT Links")
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 2.dp),
            ) {
                Text(
                    text = "${titleData.rating} / 10",
                    style = TextStyle(color = Color(0xFFFFD54F)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight(600),
                    modifier = Modifier
                        .padding(top = 1.dp.div(2))
                        .background(
                            Color(0xFF343C2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                val (fullStars, halfStars, emptyStars) = numHalfFullAndEmptyStars(titleData.rating)
                Row(
                    modifier = Modifier.padding(start = 4.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    repeat(fullStars) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Full Star",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (halfStars == 1) {
                        Image(
                            painter = painterResource(id = R.drawable.star_half_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                            contentDescription = "Half Star",
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(
                                Color(
                                    0xFFFFD54F
                                )
                            )
                        )
                    }
                    repeat(emptyStars) {
                        Image(
                            painter = painterResource(id = R.drawable.star_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                            contentDescription = "Half Star",
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(
                                Color(
                                    0xFFFFD54F
                                )
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                        .shadow(
                            6.dp,
                            spotColor = Color.Red.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    AsyncImage(
                        model = titleData.poster,
                        contentDescription = null,
                        modifier = Modifier
                            .height(250.dp)
                            .clip(
                                RoundedCornerShape(8.dp)
                            ),
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = titleData.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCFCFCF),
                        fontWeight = FontWeight(700),
                        lineHeight = 18.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    titleData.genres.split(", ").forEach { genre ->
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF303030),
                                            Color(0xFF1B1A1A),
                                            Color(0xFF303030)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre,
                                fontSize = 11.sp,
                                color = Color(0xFFE0E0E0),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }







            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeText(
                    text = "Filming Locations : ${titleData.filmingLocations}",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            Color(0xFF2E2B2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeText(
                    text = "Production Companies : ${titleData.productionCompanies}",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            Color(0xFF2E2B2B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                )
            }
        }
    }
}