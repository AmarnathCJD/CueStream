package com.amarnath.cuestream.titles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.amarnath.cuestream.R
import com.amarnath.cuestream.meta.IMDB
import com.amarnath.cuestream.meta.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TitleSearchPage(padding: PaddingValues, nav: NavController) {
    val searchResults = remember { mutableStateListOf<SearchResult>() }
    LaunchedEffect(Unit) {
        //IMDB().search("Lovely Runner", searchResults)
//        IMDB().getTitle("tt1630029")
//        IMDB().getTrailerSource("https://www.imdb.com/video/vi529579545/?ref_=ttvi_vi_imdb_2")
    }
    Column (
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
            SearchBar(searchResults)
            searchResults.forEach {
                if (it.poster.isNotEmpty())
                    SearchResultItem(it, nav)
            }
        }
    }
}

@Composable
fun SearchBar(searchResults: MutableList<SearchResult>) {
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
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debounceDelay)
                    IMDB().search(searchValue.value, searchResults)
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
                Image(
                    painter = painterResource(id = R.drawable.search_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                    contentDescription = "Clear Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFFBB86FC)
                    )
                )
            }
        )
    }
}

@Composable
fun SearchResultItem(item: SearchResult, nav: NavController) {
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
                        item.poster.split("@._V1_").first() + "@._V1_QL100_UY414_CR5,0,280,414_.jpg"
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
                Row {
                    Text(
                        text = "${item.rating} / 10",
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
                        item.duration,
                        item.viewerClass,
                        item.mediaType.ifEmpty { "N/A" },
                        item.imdbId
                    ).filter { it.isNotEmpty() && it != "N/A" }.joinToString(" | "),
                    style = androidx.compose.ui.text.TextStyle(color = Color.Gray),
                    fontSize = 12.sp,
                    fontWeight = FontWeight(600),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = trimLongPlot(item.plot),
                    style = androidx.compose.ui.text.TextStyle(color = Color(0xFFBBBBBB)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight(600),
                    modifier = Modifier.padding(top = 2.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )
            }
        }
    }
}

