package com.amarnath.cuestream.titles

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.amarnath.cuestream.R
import com.amarnath.cuestream.meta.DeleteWatchListEntry

val activeIndex = mutableIntStateOf(0)
val activeRowCount = mutableIntStateOf(2)

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

        fun newEmpty(): WLEntry {
            return WLEntry(
                title = "",
                image = "",
                rating = 0.0,
                plot = "",
                duration = "",
                status = "",
                genres = listOf(),
                year = "",
                imdbID = "",
                type = "",
                date = "",
                priority = 0,
                doneTill = 0,
                priorityClass = "",
                comment = ""
            )
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
            .padding(horizontal = 4.dp)
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AllWatchList(nav)
    }
}

@Composable
fun WatchListTopCategories() {
    val categories = listOf(
        "All", "Watching", "Completed", "On Hold", "To Watch",
        "First Priority", "Second Priority", "Third Priority"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(categories) { index, label ->
            FilterChip(
                label = { Text(text = label) },
                selected = activeIndex.intValue == index,
                onClick = {
                    activeIndex.intValue = index
                },
                modifier = Modifier.padding(end = 0.dp),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
fun AllWatchList(nav: NavController) {
    var mapPrior = ActiveWatchListEntries

    val searchValue = remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = searchValue.value,
            onValueChange = { newValue ->
                searchValue.value = newValue
                mapPrior = if (newValue.isEmpty()) {
                    ActiveWatchListEntries
                } else {
                    ActiveWatchListEntries.filter { entry ->
                        entry.title.contains(newValue, ignoreCase = true)
                    }.toMutableList()
                }
            },
            placeholder = { Text(text = "Search among your watchlist") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                cursorColor = Color(0xFFBB86FC),
                focusedIndicatorColor = Color(0xFF64DD17),
                unfocusedIndicatorColor = Color(0xFF5B6C4E),
                focusedContainerColor = Color(0xFF121212),
                unfocusedContainerColor = Color(0xFF121212),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFB3B3B3),
                focusedLabelColor = Color(0xFFBB86FC),
                unfocusedLabelColor = Color(0xFFB3B3B3)
            ),
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.search_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF00BFA5))
                )
            },
        )
    }

    WatchListTopCategories()

    var showModal by remember { mutableStateOf(false) }
    val selectedEntry = remember { mutableStateOf(WLEntry.newEmpty()) }

    if (showModal) {
        EntryModal(selectedEntry.value, nav) { showModal = false }
    }

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(start = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        mapPrior.sortByDescending { it.priority }
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp, start = 8.dp)
            ) {
                if (mapPrior.isNotEmpty()) {
                    Text(
                        text = "Your Watchlist" + if (activeIndex.intValue == 0) " (${mapPrior.size})" else (if (activeIndex.intValue == 5) " (#FP)" else if (activeIndex.intValue == 6) " (#SP)" else if (activeIndex.intValue == 7) " (#TP)" else if (activeIndex.intValue == 1) " (Watching)" else if (activeIndex.intValue == 2) " (Completed)" else if (activeIndex.intValue == 3) " (On Hold)" else " (To Watch)"),
                        fontSize = 17.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF7CB342)),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                }

                if (mapPrior.isEmpty()) {
                    Text(
                        text = "No entries found",
                        fontSize = 16.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF7CB342)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(1f)
                    .padding(end = 8.dp)
            ) {
                listOf("1", "2", "3", "4").forEachIndexed { index, label ->
                    FilterChip(
                        label = { Text(text = label) },
                        selected = activeRowCount.intValue == (index + 1),
                        onClick = {
                            activeRowCount.intValue = (index + 1)
                        },
                        modifier = Modifier,
                        shape = RoundedCornerShape(6.dp),
                    )
                }
            }
        }

        when (activeIndex.intValue) {
            0 -> {
                mapPrior = ActiveWatchListEntries
            }

            1 -> {
                mapPrior = mapPrior.filter { it.status == "Watching" }.toMutableList()
            }

            2 -> {
                mapPrior = mapPrior.filter { it.status == "Completed" }.toMutableList()
            }

            3 -> {
                mapPrior = mapPrior.filter { it.status == "On Hold" }.toMutableList()
            }

            4 -> {
                mapPrior = mapPrior.filter { it.status == "To Watch" }.toMutableList()
            }

            5 -> {
                mapPrior = mapPrior.filter { it.priorityClass == "#FP" }.toMutableList()
            }

            6 -> {
                mapPrior = mapPrior.filter { it.priorityClass == "#SP" }.toMutableList()
            }

            7 -> {
                mapPrior = mapPrior.filter { it.priorityClass == "#TP" }.toMutableList()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(activeRowCount.intValue),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                itemsIndexed(mapPrior) { _, item ->
                    val interactionSource = remember { MutableInteractionSource() }
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clickable(
                                enabled = true,
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    selectedEntry.value = item
                                    showModal = true
                                }
                            )
                            .shadow(4.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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
                                    .size(36.dp)
                                    .padding(6.dp)
                                    .background(
                                        Color(0x54FF9100),
                                        shape = CircleShape
                                    )
                                    .align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = item.priority.toString(),
                                    fontSize = 12.sp,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(6.dp)
                                    .background(
                                        Color(0x9E00C853),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = item.rating.toString(),
                                    fontSize = 12.sp,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
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
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.84f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121212).copy(alpha = 0.9f))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                AsyncImage(
                    model = entry.image,
                    contentDescription = null,
                    modifier = Modifier
                        .height(380.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .border(2.dp, Color(0xFF76FF03), RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = entry.title,
                        fontSize = 17.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB9C8E0)),
                        modifier = Modifier.padding(2.dp)
                    )
                    Text(
                        text = "(${entry.year})",
                        fontSize = 8.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF7CB342)),
                        modifier = Modifier.padding(1.dp, top = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.rating} / 10",
                        style = TextStyle(color = Color(0xFFFFD54F)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFF343C2B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    val (fullStars, halfStars, emptyStars) = numHalfFullAndEmptyStars(entry.rating)
                    Row(modifier = Modifier.padding(start = 4.dp)) {
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
                                colorFilter = ColorFilter.tint(Color(0xFFFFD54F))
                            )
                        }
                        repeat(emptyStars) {
                            Image(
                                painter = painterResource(id = R.drawable.star_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                contentDescription = "Empty Star",
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFFD54F))
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (entry.duration.isNotEmpty()) {
                        Text(
                            text = entry.duration,
                            fontSize = 14.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = "IMdb Title || {${entry.imdbID}}",
                        fontSize = 10.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5)),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "${entry.date} (${sinceDate(entry.date)})",
                    fontSize = 10.sp,
                    style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5)),
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (entry.comment.isNotEmpty()) {
                    Text(
                        text = entry.comment,
                        fontSize = 14.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5)),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        TextWithBackground(
                            text = entry.priorityClassLabel(),
                            backgroundColor = Color(0xFF2A231C),
                            textColor = Color(0xFF7CB342)
                        )
                        TextWithBackground(
                            text = "#${entry.priority}th",
                            backgroundColor = Color(0xFF2A231C),
                            textColor = Color(0xFF2196F3)
                        )
                        TextWithBackground(
                            text = entry.status,
                            backgroundColor = Color(0xFF2A231C),
                            textColor = Color(0xFF7CB342)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButtonWithTextAndIcon(
                            " Info",
                            R.drawable.info_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                            Color(0xFF2196F3)
                        ) {
                            ActiveTitleID.value = entry.imdbID
                            nav.navigate("title")
                        }
                        ElevatedButtonWithTextAndIcon(
                            "Close",
                            R.drawable.star_half_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                            Color(0xFF2196F3),
                            onClick = onClose
                        )
                        ElevatedButtonWithTextAndIcon(
                            "Remove",
                            R.drawable.cancel_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                            Color(0xFFA90D46)
                        ) {
                            ActiveWatchListEntries.remove(entry)
                            DeleteWatchListEntry(entry, ctx)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = entry.plot,
                        fontSize = 14.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5)),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp, top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TextWithBackground(text: String, backgroundColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ElevatedButtonWithTextAndIcon(
    text: String,
    iconRes: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(color = iconTint, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
    }
}

fun WLEntry.priorityClassLabel(): String {
    return when (priorityClass) {
        "#FP" -> "#FirstPriority"
        "#SP" -> "#SecondPriority"
        else -> "#ThirdPriority"
    }
}