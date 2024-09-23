package com.amarnath.cuestream.titles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
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
    val comment: String,
) {
    companion object {
        fun fromString(data: String): WLEntry {
            val regex =
                """WLEntry\(title=(.*?), image=(.*?), rating=(.*?), plot=(.*?), duration=(.*?), status=(.*?), genres=\[(.*?)\], year=(.*?), imdbID=(.*?), type=(.*?), date=(.*?), priority=(.*?), doneTill=(.*?), priorityClass=(.*?), comment=(.*?)\)""".toRegex()
            val matchResult = regex.find(data)

            return if (matchResult != null) {
                val title = matchResult.groups[1]?.value ?: ""
                val image = matchResult.groups[2]?.value ?: ""
                val rating = matchResult.groups[3]?.value?.toDouble() ?: 0.0
                val plot = matchResult.groups[4]?.value ?: ""
                val duration = matchResult.groups[5]?.value ?: ""
                val status = matchResult.groups[6]?.value ?: ""
                val genresStr = matchResult.groups[7]?.value ?: ""
                val year = matchResult.groups[8]?.value ?: ""
                val imdbID = matchResult.groups[9]?.value ?: ""
                val type = matchResult.groups[10]?.value ?: ""
                val date = matchResult.groups[11]?.value ?: ""
                val priority = matchResult.groups[12]?.value?.toInt() ?: 0
                val doneTill = matchResult.groups[13]?.value?.toInt() ?: 0
                val priorityClass = matchResult.groups[14]?.value ?: ""
                val comment = matchResult.groups[15]?.value ?: ""

                val genres = genresStr.split(", ").map { it.trim() }

                WLEntry(
                    title = title,
                    image = image,
                    rating = rating,
                    plot = plot,
                    duration = duration,
                    status = status,
                    genres = genres,
                    year = year,
                    imdbID = imdbID,
                    type = type,
                    date = date,
                    priority = priority,
                    doneTill = doneTill,
                    priorityClass = priorityClass,
                    comment = comment
                )
            } else {
                throw IllegalArgumentException("Invalid WLEntry string format")
            }
        }
    }
}

val ActiveWatchListEntries = mutableListOf<WLEntry>()

@Composable
fun WatchListMain(padding: PaddingValues = PaddingValues(0.dp), nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF121212),
                        Color(0xFF121212),
                    )
                )
            ),
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

            4 -> {
                PriorityList(nav, "#FP")
            }

            5 -> {
                PriorityList(nav, "#SP")
            }

            6 -> {
                PriorityList(nav, "#TP")
            }

            else -> {
                AllWatchList(nav)
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
                    colorFilter = ColorFilter.tint(
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
        listOf("Watching", "Completed", "On Hold", "To Watch").forEachIndexed { index, label ->
            FilterChip(
                label = { Text(text = label) },
                selected = activeIndex.intValue == index + 1,
                onClick = {
                    activeIndex.intValue = if (activeIndex.intValue == index + 1) 0 else index + 1
                },
                modifier = Modifier.padding(end = 0.dp),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
fun AllWatchList(nav: NavController) {
    var showModal by remember { mutableStateOf(false) }
    val selectedEntry = remember {
        mutableStateOf(
            WLEntry(
                "",
                "",
                0.0,
                "",
                "",
                "",
                listOf(),
                "",
                "",
                "",
                "",
                0,
                0,
                "",
                ""
            )
        )
    }

    if (showModal) {
        EntryModal(selectedEntry.value, nav) { showModal = false }
    }

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(start = 4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currDate = System.currentTimeMillis()
        val recentEntries =
            ActiveWatchListEntries.filter { (currDate - dateToTimestamp(it.date)) < 345600000 }

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
                    itemsIndexed(recentEntries) { _, item ->
                        ElevatedCard(
                            modifier = Modifier
                                .size(140.dp, 210.dp)
                                .clickable(
                                    enabled = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        selectedEntry.value = item
                                        showModal = true
                                    }
                                ),
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
                                        .height(210.dp)
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
                                            .height(24.dp)
                                            .width(24.dp)
                                            .padding(
                                                top = 6.dp,
                                                end = 6.dp
                                            )
                                            .background(
                                                Color(0x638E24AA),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = item.rating.toString(),
                                                fontSize = 9.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xC13949AB),
                                                ),
                                                modifier = Modifier
                                                    .padding(1.dp)

                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.5f),
                                                        Color.Black.copy(alpha = 0.6f),
                                                        Color.Black.copy(alpha = 0.8f),
                                                        Color.Black.copy(alpha = 0.9f),
                                                    )
                                                )
                                            )
                                            .align(Alignment.BottomStart)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .padding(top = 2.dp)
                                            )

                                            Text(
                                                text = item.plot,
                                                fontSize = 10.sp,
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

        val mapPrior = ActiveWatchListEntries.groupBy { it.priorityClass }
        mapPrior.forEach { (key, value) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(),
                            onClick = {
                                activeIndex.intValue = when (key) {
                                    "#FP" -> 4
                                    "#SP" -> 5
                                    else -> 6
                                }
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key + (if (key == "#FP") " (First Priority)" else if (key == "#SP") " (Second Priority)" else " (Third Priority)"),
                        fontSize = 15.sp,
                        style = TextStyle(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp),
                        color = Color(0xFF7CB342)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Star Icon",
                        tint = Color(0xB5C0CA33),
                        modifier = Modifier.size(24.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(value) { _, item ->
                        ElevatedCard(
                            modifier = Modifier
                                .size(140.dp, 210.dp)
                                .clickable(
                                    enabled = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        selectedEntry.value = item
                                        showModal = true
                                    }
                                ),
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
                                        .height(210.dp)
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
                                            .height(24.dp)
                                            .width(24.dp)
                                            .padding(
                                                top = 6.dp,
                                                end = 6.dp
                                            )
                                            .background(
                                                Color(0x638E24AA),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = item.rating.toString(),
                                                fontSize = 9.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xC13949AB),
                                                ),
                                                modifier = Modifier
                                                    .padding(1.dp)

                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.5f),
                                                        Color.Black.copy(alpha = 0.6f),
                                                        Color.Black.copy(alpha = 0.8f),
                                                        Color.Black.copy(alpha = 0.9f),
                                                    )
                                                )
                                            )
                                            .align(Alignment.BottomStart)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .padding(top = 2.dp)
                                            )

                                            Text(
                                                text = item.plot,
                                                fontSize = 10.sp,
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

@Composable
fun PriorityList(nav: NavController, prio: String) {
    var showModal by remember { mutableStateOf(false) }
    val selectedEntry = remember {
        mutableStateOf(
            WLEntry(
                "",
                "",
                0.0,
                "",
                "",
                "",
                listOf(),
                "",
                "",
                "",
                "",
                0,
                0,
                "",
                ""
            )
        )
    }

    if (showModal) {
        EntryModal(selectedEntry.value, nav) { showModal = false }
    }

    val entries = ActiveWatchListEntries.filter { it.priorityClass == prio }

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(start = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = if (prio == "#FP") "#First Priority" else if (prio == "#SP") "#Second Priority" else "#Third Priority",
                fontSize = 17.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color(0xFF7CB342)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(entries) { _, item ->
                ElevatedCard(
                    modifier = Modifier
                        .size(140.dp, 210.dp)
                        .clickable(
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                selectedEntry.value = item
                                showModal = true
                            }
                        ),
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
                                .height(210.dp)
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
                                    .height(25.dp)
                                    .width(25.dp)
                                    .padding(
                                        top = 8.dp,
                                        end = 6.dp
                                    )
                                    .background(
                                        Color(0x9ED3C638),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = item.rating.toString(),
                                    fontSize = 9.sp,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    ),
                                    modifier = Modifier
                                        .padding(1.dp)
                                        .padding(top = 2.dp, start = 2.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Black.copy(alpha = 0.6f),
                                                Color.Black.copy(alpha = 0.8f),
                                                Color.Black.copy(alpha = 0.9f),
                                            )
                                        )
                                    )
                                    .align(Alignment.BottomStart)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(top = 2.dp)
                                    )

                                    Text(
                                        text = item.plot,
                                        fontSize = 10.sp,
                                        style = TextStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.padding(8.dp)
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


@Composable
fun EntryModal(entry: WLEntry, nav: NavController, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121212).copy(alpha = 0.9f))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row {
                            Text(
                                text = entry.title,
                                fontSize = 16.sp,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB9C8E0)
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            Text(
                                text = "(${entry.year})",
                                fontSize = 9.sp,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7CB342)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .padding(top = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AsyncImage(
                        model = entry.image,
                        contentDescription = null,
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                BorderStroke(1.dp, Color(0xFF121212)),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "${entry.rating} / 10",
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
                        val (fullStars, halfStars, emptyStars) = numHalfFullAndEmptyStars(
                            entry.rating
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (entry.duration != "") {
                            Text(
                                text = entry.duration,
                                fontSize = 14.sp,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB0BEC5)
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text(
                            text = entry.type + " ||" + " " + '{' + entry.imdbID + '}',
                            fontSize = 10.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Text(
                        text = entry.date,
                        fontSize = 10.sp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0BEC5)
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    if (entry.comment != "") {
                        Text(
                            text = entry.comment,
                            fontSize = 14.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .background(
                                        Color(0xFF2A231C),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 4.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (entry.priorityClass == "#FP") "#FirstPriority" else if (entry.priorityClass == "#SP") "#SecondPriority" else "#ThirdPriority",
                                    style = TextStyle(color = Color(0xFF7CB342)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight(600),
                                    modifier = Modifier
                                )
                                Text(
                                    text = " #${entry.priority}th",
                                    style = TextStyle(color = Color(0xFF2196F3)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight(600),
                                    modifier = Modifier
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .background(
                                        Color(0xFF2A231C),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 4.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = entry.status,
                                    style = TextStyle(color = Color(0xFF7CB342)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight(600),
                                    modifier = Modifier
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ElevatedButton(
                                onClick = {
                                    ActiveTitleID.value = entry.imdbID
                                    nav.navigate("title")
                                },
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Info",
                                        style = TextStyle(color = Color(0xFF2196F3)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier.padding(8.dp)
                                    )

                                    Image(
                                        painter = painterResource(R.drawable.info_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                        contentDescription = "Info Icon",
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = ColorFilter.tint(
                                            Color(0xFF2196F3)
                                        )
                                    )
                                }
                            }
                            ElevatedButton(
                                onClick = {
                                    ActiveWatchListEntries.remove(entry)
                                    onClose()
                                },
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Remove",
                                        style = TextStyle(color = Color(0xFF2196F3)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight(600),
                                        modifier = Modifier.padding(8.dp)
                                    )

                                    Image(
                                        painter = painterResource(R.drawable.cancel_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                        contentDescription = "Info Icon",
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = ColorFilter.tint(
                                            Color(0xFFA90D46)
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = entry.plot,
                            fontSize = 14.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
