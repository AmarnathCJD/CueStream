package com.amarnath.cuestream.titles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amarnath.cuestream.R

val activeIndex = mutableIntStateOf(0)

data class WLEntry(
    val title: String,
    val image: String,
    val rating: Double,
    val plot: String,
    val duration: String,
    val status: String,
    val genres: List<String>,
    val year: String,
    val imdbID: String,
    val type: String,
    val date: String,
    val priority: Int,
    val doneTill: Int,
    val priorityClass: String,
    val color: Color,
    val comment: String,
)

val dummyEntries = mutableListOf(
    WLEntry(
        title = "Lovely Runner",
        image = "https://m.media-amazon.com/images/M/MV5BNjYwM2RkMmUtOGU2OS00ZjdlLWE5Y2UtYzRkMzExYjdhNjVlXkEyXkFqcGc@._V1_QL75_UY562_CR7,0,380,562_.jpg",
        rating = 7.5,
        plot = "A heartwarming story of love and endurance, featuring a marathon runner on a quest for self-discovery.",
        duration = "2h 10m",
        status = "Watching",
        genres = listOf("Romance", "Drama"),
        year = "2022",
        imdbID = "tt1234567",
        type = "Movie",
        date = "2024-09-21",
        priority = 1,
        doneTill = 80,
        priorityClass = "#FP",
        color = Color.Red,
        comment = "Inspiring!",
    ),
    WLEntry(
        title = "Twinkling Watermelon",
        image = "https://m.media-amazon.com/images/M/MV5BNjYwM2RkMmUtOGU2OS00ZjdlLWE5Y2UtYzRkMzExYjdhNjVlXkEyXkFqcGc@._V1_QL75_UY562_CR7,0,380,562_.jpg",
        rating = 8.3,
        plot = "A coming-of-age fantasy where a young boy discovers a magical watermelon that transports him to a mystical land.",
        duration = "1h 45m",
        status = "Completed",
        genres = listOf("Fantasy", "Adventure"),
        year = "2023",
        imdbID = "tt2345678",
        type = "Movie",
        date = "2024-09-20",
        priority = 2,
        doneTill = 100,
        priorityClass = "#SP",
        color = Color.Blue,
        comment = "Loved the magical elements!",
    ),
    WLEntry(
        title = "Business Proposal",
        image = "https://m.media-amazon.com/images/M/MV5BNjYwM2RkMmUtOGU2OS00ZjdlLWE5Y2UtYzRkMzExYjdhNjVlXkEyXkFqcGc@._V1_QL75_UY562_CR7,0,380,562_.jpg",
        rating = 8.1,
        plot = "A lighthearted romantic comedy where an employee ends up on a blind date with her boss, with hilarious consequences.",
        duration = "12 episodes",
        status = "Plan to Watch",
        genres = listOf("Romance", "Comedy"),
        year = "2022",
        imdbID = "tt3456789",
        type = "TV Series",
        date = "2024-09-15",
        priority = 3,
        doneTill = 0,
        priorityClass = "#TP",
        color = Color.Green,
        comment = "Heard good things!",
    ),
    WLEntry(
        title = "Lock & Key",
        image = "https://m.media-amazon.com/images/M/MV5BNjYwM2RkMmUtOGU2OS00ZjdlLWE5Y2UtYzRkMzExYjdhNjVlXkEyXkFqcGc@._V1_QL75_UY562_CR7,0,380,562_.jpg",
        rating = 7.2,
        plot = "A family moves into an old mansion full of mysterious keys that open doors to unimaginable powers.",
        duration = "3 seasons",
        status = "Watching",
        genres = listOf("Horror", "Fantasy", "Mystery"),
        year = "2020",
        imdbID = "tt4567890",
        type = "TV Series",
        date = "2024-09-18",
        priority = 2,
        doneTill = 65,
        priorityClass = "#SP",
        color = Color.Green,
        comment = "Great suspense!",
    ),
    WLEntry(
        title = "Money Heist",
        image = "https://m.media-amazon.com/images/M/MV5BNjYwM2RkMmUtOGU2OS00ZjdlLWE5Y2UtYzRkMzExYjdhNjVlXkEyXkFqcGc@._V1_QL75_UY562_CR7,0,380,562_.jpg",
        rating = 9.0,
        plot = "A criminal mastermind leads a group of robbers in executing the biggest heist in history.",
        duration = "5 seasons",
        status = "Completed",
        genres = listOf("Action", "Crime", "Thriller"),
        year = "2017",
        imdbID = "tt5678901",
        type = "TV Series",
        date = "2024-09-10",
        priority = 1,
        doneTill = 100,
        priorityClass = "#FP",
        color = Color.Black,
        comment = "Unbelievably thrilling!",
    )
)


@Composable
fun WatchListMain(padding: PaddingValues = PaddingValues(0.dp)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Watchlist",
            fontSize = 20.sp,
            style = TextStyle(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        WatchListTopSearchBar()
        WatchListTopCategories()
        when (activeIndex.intValue) {
            1 -> {
                //CurrentlyWatching()
            }

            2 -> {
                //NotStarted()
            }

            3 -> {
                //Completed()
            }

            else -> {
                AllWatchList()
            }
        }
    }
}

@Composable
fun WatchListTopSearchBar() {
    val searchValue = remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = searchValue.value,
            onValueChange = {
                searchValue.value = it
            },
            placeholder = { Text(text = "Search among your watchlist") },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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
        )
    }
}

@Composable
fun WatchListTopCategories() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ElevatedButton(
            onClick = {
                if (activeIndex.intValue != 1) activeIndex.intValue = 1 else activeIndex.intValue =
                    0
            },
            modifier = Modifier.padding(end = 0.dp),
            shape = RoundedCornerShape(12.dp),
            border = if (activeIndex.intValue == 1) BorderStroke(1.dp, Color(0xFFBB86FC)) else null,
        ) {
            Text(
                text = "On Track",
                color = if (activeIndex.intValue == 1) Color(0xFFBB86FC) else Color.White
            )
        }

        ElevatedButton(
            onClick = {
                if (activeIndex.intValue != 2) activeIndex.intValue = 2 else activeIndex.intValue =
                    0
            },
            modifier = Modifier.padding(end = 0.dp),
            shape = RoundedCornerShape(12.dp),
            border = if (activeIndex.intValue == 2) BorderStroke(1.dp, Color(0xFFBB86FC)) else null,
        ) {
            Text(
                text = "Not Started",
                color = if (activeIndex.intValue == 2) Color(0xFFBB86FC) else Color.White
            )
        }

        ElevatedButton(
            onClick = {
                if (activeIndex.intValue != 3) activeIndex.intValue = 3 else activeIndex.intValue =
                    0
            },
            modifier = Modifier.padding(end = 0.dp),
            shape = RoundedCornerShape(12.dp),
            border = if (activeIndex.intValue == 3) BorderStroke(1.dp, Color(0xFFBB86FC)) else null,
        ) {
            Text(
                text = "Completed",
                color = if (activeIndex.intValue == 3) Color(0xFFBB86FC) else Color.White
            )
        }
    }
}

@Composable
fun AllWatchList() {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(start = 4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currDate = System.currentTimeMillis()
        val recentEntries =
            dummyEntries.filter { (currDate - dateToTimestamp(it.date)) < 345600000 }

        if (recentEntries.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Recently Added (Last 4 days)",
                    fontSize = 14.sp,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(recentEntries) { index, item ->
                        ElevatedCard(
                            modifier = Modifier.size(180.dp, 270.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit,
                                    clipToBounds = true,
                                    alignment = Alignment.Center,
                                )
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    style = TextStyle(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(8.dp)
                                )
                                Text(
                                    text = item.plot,
                                    fontSize = 12.sp,
                                    style = TextStyle(fontWeight = FontWeight.Normal),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        val mapPrior = dummyEntries.groupBy { it.priorityClass }
        mapPrior.forEach { (key, value) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = key,
                    fontSize = 15.sp,
                    style = TextStyle(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    color = Color(0xFF2174B7)
                )
                LazyRow(
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(value) { index, item ->
                        ElevatedCard(
                            modifier = Modifier.size(160.dp, 240.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = item.image,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center,
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.5f),
                                                        Color.Black.copy(alpha = 0.8f),
                                                        Color.Black.copy(alpha = 0.9f),
                                                    )
                                                )
                                            )
                                            .align(Alignment.BottomStart)
                                    ) {
                                        Column (
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                                .verticalScroll(rememberScrollState())
                                        ){
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .padding(top = 8.dp)
                                            )

                                            Text(
                                                text = item.plot,
                                                fontSize = 12.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                ),
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}