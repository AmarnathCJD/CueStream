package com.amarnath.cuestream.titles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.amarnath.cuestream.R
import com.amarnath.cuestream.meta.AutoCompleteResult
import com.amarnath.cuestream.meta.IMDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TitleSearchPage(padding: PaddingValues, nav: NavController) {
    val fastSearchResults = remember { mutableStateListOf<AutoCompleteResult>() }
    val showLoading = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .padding(
                top = padding.calculateTopPadding() + 20.dp,
                bottom = padding.calculateBottomPadding() + 30.dp
            ),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(color = Color.Black)
                .padding(horizontal = 2.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SearchBar(fastSearchResults, showLoading)
            if (showLoading.value && fastSearchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .padding(8.dp)
                        .padding(top = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFBB86FC),
                        strokeWidth = 3.dp.plus(1.dp.div(2)),
                        modifier = Modifier.size(40.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            } else if (fastSearchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .padding(8.dp)
                        .padding(top = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search `Movies` and `TV Shows`",
                        style = androidx.compose.ui.text.TextStyle(color = Color.White),
                        fontSize = 16.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight(500),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            fastSearchResults.forEach {
                if (it.poster.isNotEmpty())
                    SearchResultItem(it, nav)
            }
        }
    }
}

@Composable
fun SearchBar(
    fastSearchResults: MutableList<AutoCompleteResult>,
    showLoading: MutableState<Boolean>
) {
    val searchValue = remember { mutableStateOf("") }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val debounceDelay = 500L

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = searchValue.value,
            onValueChange = {
                searchValue.value = it
                searchJob?.cancel()
                if (it.isEmpty()) {
                    showLoading.value = false
                    return@OutlinedTextField
                }
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debounceDelay)
                    showLoading.value = true
                    IMDB().autocomplete(searchValue.value, fastSearchResults, showLoading)
                }
            },
            label = { Text(text = "Search for Movies and TV Shows", fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                cursorColor = Color(0xFFBB86FC),
                focusedIndicatorColor = Color(0xFFBB86FC),
                unfocusedIndicatorColor = Color(0xFF3A3A3A),
                focusedContainerColor = Color(0xFF121212),
                unfocusedContainerColor = Color(0xFF121212),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFB3B3B3),
                focusedLabelColor = Color(0xFFBB86FC),
                unfocusedLabelColor = Color(0xFFB3B3B3)
            ),
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.subscriptions_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                    contentDescription = "Ack Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFFBB86FC)
                    )
                )
            },
            trailingIcon = {
                if (showLoading.value) {
                    CircularProgressIndicator(
                        color = Color(0xFFBB86FC),
                        strokeWidth = 3.dp.plus(1.dp.div(2)),
                        modifier = Modifier.size(24.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.search_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                        contentDescription = "Clear Icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            Color(0xFFBB86FC)
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun SearchResultItem(item: AutoCompleteResult, nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp)
            .clickable {
                ActiveTitleID.value = item.imdbId
                nav.navigate("title") {
                    launchSingleTop = true
                }
            }
            .border(
                width = 1.dp,
                color = Color(0xFF110A0C),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .background(
                color = Color(0xFF111111),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp)
                .padding(bottom = 6.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val showShimmer = remember { mutableStateOf(true) }
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(
                        item.poster.split("@._V1_").first() + "@._V1_QL100_UY430_CR5,0,280,414_.jpg"
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = "Title Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                    .background(
                        shimmerBrush(
                            targetValue = 1300f,
                            showShimmer = showShimmer.value
                        )
                    )
                    .shadow(
                        4.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .background(
                        Color.Transparent
                    ),
                onSuccess = {
                    showShimmer.value = false
                },
            )
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = item.title,
                    style = androidx.compose.ui.text.TextStyle(color = Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(700)
                )
                if (item.year.isNotEmpty()) {
                    Text(
                        text = item.year,
                        style = androidx.compose.ui.text.TextStyle(color = Color.Gray),
                        fontSize = 11.sp,
                        fontWeight = FontWeight(600),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(
                                Color(0xFF31343C),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.padding(top = 1.dp))
                }
                Row {
                    Text(
                        text = "IMDB ${if (item.rating > 0.0) item.rating else "??"}",
                        style = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFD54F)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight(600),
                        modifier = Modifier
                            .padding(top = 1.dp.div(2))
                            .background(
                                Color(0xFF343C2B),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    val (fullStars, halfStars, emptyStars) = numHalfFullAndEmptyStars(item.rating)
                    Row(
                        modifier = Modifier.padding(start = 4.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        repeat(fullStars) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Full Star",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (halfStars == 1) {
                            Image(
                                painter = painterResource(id = R.drawable.star_half_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                contentDescription = "Half Star",
                                modifier = Modifier.size(16.dp),
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
                                modifier = Modifier.size(16.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color(
                                        0xFFFFD54F
                                    )
                                )
                            )
                        }
                    }
                }
                Text(
                    text = listOf(
                        item.duration.ifEmpty { "N/A" },
                        item.mediaType.ifEmpty { "N/A" },
                        item.imdbId
                    ).filter { it.isNotEmpty() && it != "N/A" }.joinToString(" | "),
                    style = androidx.compose.ui.text.TextStyle(color = Color.Gray),
                    fontSize = 12.sp,
                    fontWeight = FontWeight(600),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = trimLongPlot(item.plot.ifEmpty { "......." }),
                    style = androidx.compose.ui.text.TextStyle(color = Color(0xFFBBBBBB)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight(600),
                    modifier = Modifier.padding(top = 2.dp),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

