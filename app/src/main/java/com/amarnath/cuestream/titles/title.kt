package com.amarnath.cuestream.titles

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.amarnath.cuestream.IMDBInst
import com.amarnath.cuestream.R
import com.amarnath.cuestream.meta.MainTitle

val ActiveTitleID = mutableStateOf<String?>(null)
val ActiveTitleData = mutableStateOf<MainTitle?>(null)
val ActiveTrailerData = mutableStateOf<Pair<String, String>?>(null)

@Composable
fun MainTitlePage(padding: PaddingValues, nav: NavController) {
    LaunchedEffect(ActiveTitleID.value) {
        IMDBInst.getTitle(ActiveTitleID.value!!, ActiveTitleData)
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrailerPlayer()
        TitleDetails()
    }
}

@Composable
fun TrailerPlayer() {
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
    val ctx = androidx.compose.ui.platform.LocalContext.current
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
    if (titleData != null) {
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
                    text = titleData.releaseDateLong, fontSize = 11.sp, color = Color(0xFF8F8F8F),
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
                    text = titleData.countryOfOrigin, fontSize = 11.sp, color = Color(0xFF8F8F8F),
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
                    onClick = { /*TODO*/ },
                    modifier = Modifier.padding(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
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
                    colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
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
                    style = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFD54F)),
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
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
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
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
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



            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .width(120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                model = cast.third,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(100.dp)
                                    .clip(
                                        RoundedCornerShape(64.dp)
                                    ),
                            )
                            Text(
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    titleData.moreLikeThis.forEach { moreLikeThis ->
                        Column(
                            modifier = Modifier
                                .width(120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = moreLikeThis.poster.split("@._V1_")
                                    .first() + "@._V1_QL100_UY414_CR5,0,280,414_.jpg",
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(13.dp)),
                                clipToBounds = true
                            )
                            MarqueeText(
                                text = moreLikeThis.title,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(120.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
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