package com.amarnath.cuestream

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amarnath.cuestream.meta.IMDB
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
            CueStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = { AppTopBar() }) {
                    val padd = it
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "watchlist") {
                        composable("search") {
                            TitleSearchPage(padding = padd, nav = nav)
                        }
                        composable("title") {
                            MainTitlePage(padding = padd, nav = nav)
                        }
                        composable("watchlist") {
                            WatchListMain(padding = padd)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppTopBar() {
    Text(text = "CueStream", modifier = Modifier.padding(16.dp))
}