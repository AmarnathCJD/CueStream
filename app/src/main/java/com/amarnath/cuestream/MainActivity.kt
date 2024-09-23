package com.amarnath.cuestream

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amarnath.cuestream.meta.IMDB
import com.amarnath.cuestream.meta.LoadWatchList
import com.amarnath.cuestream.titles.MainTitlePage
import com.amarnath.cuestream.titles.TitleSearchPage
import com.amarnath.cuestream.titles.WatchListMain
import com.amarnath.cuestream.ui.theme.CueStreamTheme

val IMDBInst = IMDB()

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoadWatchList(this)
            CueStreamTheme {
                val nav = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = { AppTopBar() },
                    bottomBar = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(bottom = 0.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF121212))
                                    .alpha(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        1.dp,
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                    )
                            ) {
                                BottomIconItem(
                                    imageRes = R.drawable.subtitles_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                                    color = Color(0xFFF30000),
                                    name = "Title",
                                    nav = nav
                                )
                                BottomIconItem(
                                    imageRes = R.drawable.devices_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                                    color = Color(0xFFF30000),
                                    name = "WatchList",
                                    nav = nav
                                )
                                BottomIconItem(
                                    imageRes = R.drawable.search_24dp_e8eaed_fill0_wght400_grad0_opsz24,
                                    color = Color(0xFFF30000),
                                    name = "Search",
                                    nav = nav
                                )
                            }
                        }
                    },
                ) {
                    val padd = it

                    NavHost(navController = nav, startDestination = "watchlist") {
                        composable("search") {
                            TitleSearchPage(padding = padd, nav = nav)
                        }
                        composable("title") {
                            MainTitlePage(padding = padd, nav = nav)
                        }
                        composable("watchlist") {
                            WatchListMain(padding = padd, nav = nav)
                        }
                    }

                    // navigae to watchlist after 30 seconds
//                    LaunchedEffect(Unit) {
//                        delay(60000)
//                        nav.navigate("watchlist")
//                    }
                }
            }
        }
    }
}

@Composable
fun AppTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CueStream", modifier = Modifier.padding(16.dp), color = Color(0xFFE8EAED),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun BottomIconItem(imageRes: Int, color: Color, name: String = "Icon", nav: NavController? = null) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    nav?.navigate(name.lowercase())
                }
            )
        ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                painter = painterResource(id = imageRes),
                contentDescription = "Icon",
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Text(
                text = name,
                color = Color(0xB0FFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}