package com.amarnath.cuestream.meta

import android.util.Log
import androidx.compose.runtime.MutableState
import org.json.JSONObject

data class SearchResult(
    val imdbId: String,
    val title: String,
    val year: String,
    val rating: Double,
    val duration: String,
    val plot: String,
    val poster: String,
    val mediaType: String,
    val viewerClass: String
)

data class AutoCompleteResult(
    val imdbId: String,
    val title: String,
    val year: String,
    val poster: String,
    val mediaType: String,
    val rating: Double,
    val plot: String = "",
    val duration: String = "-/-",
)

data class MoreLikeThisResult(
    val imdbId: String,
    val title: String,
    val rating: Double,
    val poster: String
)

data class MainTitle(
    val id: String, //
    val title: String, //
    val poster: String, //
    val altTitle: String, //
    val description: String, //
    val rating: Double, //
    val viewerClass: String, //
    val duration: String,
    val genres: String, //
    val releaseDate: String, //
    val actors: String, //
    val trailer: String, //
    val titleCasts: List<Triple<String, String, String>>, //
    val moreLikeThis: List<MoreLikeThisResult>, //
    val releaseDateLong: String, //
    val countryOfOrigin: String,
    val languages: String,
    val alsoKnownAs: String, //
    val filmingLocations: String,
    val productionCompanies: String,
    val ratingCount: String = "0",
    val metaScore: Int = 0
)

class IMDB {
    private val client = okhttp3.OkHttpClient()
    fun search(query: String, searchResults: MutableList<SearchResult>, fastRes: MutableList<AutoCompleteResult>) {
        val startTimer = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url("https://www.imdb.com/search/title/?title=${query.replace(" ", "+")}")
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                )
            )
            .build()
        val response = client.newCall(request)
        response.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("IMDB", "Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d(
                    "IMDB",
                    "MainSearch - Fetch - Took ${System.currentTimeMillis() - startTimer}ms"
                )
                if (!response.isSuccessful) {
                    Log.e(
                        "IMDB",
                        "Failed to execute request: ${response.code}, ${response.message}"
                    )
                    return
                }
                try {
                    val soup = response.body?.string()?.let { org.jsoup.Jsoup.parse(it) }
                    val elements = soup?.getElementsByClass("dli-parent")
                    val temp = mutableListOf<SearchResult>()
                    elements?.forEach {
                        try {
                            val title = it.select("h3.ipc-title__text").text().split(". ")[1]
                            val meta = it.select("span.dli-title-metadata-item")
                            if (meta.size == 0) {
                                return@forEach
                            }
                            val year = meta[0].text()
                            val (duration, viewerClass) = parseDurationAndViewerClass(meta)
                            val rating = if (it.select("span.ipc-rating-star--rating").size == 0) {
                                0.0
                            } else {
                                it.select("span.ipc-rating-star--rating").text().toDouble()
                            }
                            val poster = it.select("img.ipc-image").attr("src")
                            val mediaType = it.select("span.dli-title-type-data").text() ?: "N/A"
                            val plot = tOrN(it.select("div.dli-plot-container").text())
                            temp.add(
                                SearchResult(
                                    title = title,
                                    year = year,
                                    rating = rating,
                                    duration = duration,
                                    plot = plot,
                                    poster = poster,
                                    mediaType = mediaType,
                                    viewerClass = viewerClass,
                                    imdbId = it.select("a.ipc-title-link-wrapper").attr("href")
                                        .substringAfter("title/").substringBefore("/")
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("IMDB", "Failed to parse element: ${e.message}")
                        }
                    }

                    searchResults.clear()
                    searchResults.addAll(temp)

                    for (i in 0 until temp.size) {
                        val fastResItem = fastRes.find { it.imdbId == temp[i].imdbId }
                        if (fastResItem != null) {
                            println("IMDB: Found fastResItem: ${fastResItem.title}")
                            fastRes[fastRes.indexOf(fastResItem)] = fastResItem.copy(rating = temp[i].rating, plot = temp[i].plot, duration = temp[i].duration)
                        }
                    }

                    Log.d("IMDB", "MainSearch - Took ${System.currentTimeMillis() - startTimer}ms")
                } catch (e: Exception) {
                    Log.e("IMDB", "Failed to parse response")
                }
            }
        })
    }

    fun autocomplete(
        query: String,
        fastSearchResults: MutableList<AutoCompleteResult>,
        showLoading: MutableState<Boolean>
    ) {
        if (query.isEmpty())
            return
        val q = query.replace(" ", "+").trim()
        val startTimer = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url("https://v2.sg.media-imdb.com/suggestion/${q[0]}/$q.json")
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                )
            )
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("IMDB", "Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e(
                        "IMDB",
                        "Failed to execute request: ${response.code}, ${response.message}, url: ${request.url}"
                    )
                    return
                }
                try {
                    if (response.body == null) {
                        Log.e(
                            "IMDB",
                            "Failed to execute request: ${response.code}, ${response.message}, url: ${request.url}"
                        )
                        return
                    }
                    val soup = response.body?.string()?.let { JSONObject(it) }
                    val results = soup?.getJSONArray("d")
                    val temp = mutableListOf<AutoCompleteResult>()
                    for (i in 0 until results!!.length()) {
                        val result = results.getJSONObject(i)
                        if (result.optJSONObject("i") === null) {
                            continue
                        }
                        temp.add(
                            AutoCompleteResult(
                                imdbId = result.optString("id"),
                                title = result.optString("l"),
                                year = result.optString("y"),
                                poster = result.getJSONObject("i").getString("imageUrl"),
                                mediaType = result.optString("q"),
                                0.0
                            )
                        )
                    }

                    // print all title and  poster
                    for (i in 0 until temp.size) {
                        println("IMDB: Title: ${temp[i].title}, Poster: ${temp[i].poster}")
                    }

                    fastSearchResults.clear()
                    fastSearchResults.addAll(temp)

                    showLoading.value = false

                    val slowBgSearch = mutableListOf<SearchResult>()
                    search(query, slowBgSearch, fastSearchResults)

                    Log.d(
                        "IMDB",
                        "Autocomplete - Took ${System.currentTimeMillis() - startTimer}ms"
                    )
                } catch (e: Exception) {
                    Log.e("IMDB", "Failed to parse response: ${e.message}")
                }
            }
        })
    }

    fun getTitle(id: String, store: MutableState<MainTitle?>) {
        val startTimer = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url("https://www.imdb.com/title/$id")
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                )
            )
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("IMDB", "Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e(
                        "IMDB",
                        "Failed to execute request: ${response.code}, ${response.message}, url: ${request.url}"
                    )
                    return
                }
                try {
                    val soup = response.body?.string()?.let { org.jsoup.Jsoup.parse(it) }
                    val poster = soup?.select("meta[property=og:image]")?.attr("content")
                    val jsonMetaObj =
                        soup?.select("script[type=application/ld+json]")?.first()?.data()
                            ?.replace("<script type=\"application/ld+json\">", "")
                            ?.replace("</script>", "")
                    val jsonMeta = jsonMetaObj?.let { JSONObject(it) }
                    val title =
                        soup?.select("span.hero__primary-text")?.first()?.text() ?: "N/A"
                    val altTitle = jsonMeta?.optString("alternateName", "") ?: ""
                    val description = jsonMeta?.optString("description", "") ?: ""
                    val rating =
                        jsonMeta?.optJSONObject("aggregateRating")?.optDouble("ratingValue", 0.0)
                            ?: 0.0
                    val viewerClass = jsonMeta?.optString("contentRating", "") ?: ""
                    val duration = jsonMeta?.optString("duration", "") ?: ""
                    val genres = jsonMeta?.optJSONArray("genre")?.let {
                        val temp = mutableListOf<String>()
                        for (i in 0 until it.length()) {
                            temp.add(it.getString(i))
                        }
                        temp.joinToString(", ")
                    } ?: "N/A"
                    val releaseDate = jsonMeta?.optString("datePublished", "") ?: ""
                    val actors = jsonMeta?.optJSONArray("actor")?.let {
                        val temp = mutableListOf<String>()
                        for (i in 0 until it.length()) {
                            temp.add(it.getJSONObject(i).optString("name", ""))
                        }
                        temp.joinToString(", ")
                    } ?: "N/A"
                    val trailer =
                        jsonMeta?.optJSONObject("trailer")?.optString("embedUrl", "") ?: ""

                    val titleCast = soup?.select("div.title-cast__grid")?.first()
                        ?.select("div[data-testid=title-cast-item]")
                    val titleCasts = mutableListOf<Triple<String, String, String>>()
                    titleCast?.forEach {
                        val name = it.select("img").attr("alt")
                        val role = it.select("a[data-testid=cast-item-characters-link]").text()
                        val image = it.select("img").attr("src")
                        titleCasts.add(Triple(name, role, image))
                    }

                    val moreLikeThis = soup?.select("section[data-testid=MoreLikeThis]")?.first()
                        ?.select("div.ipc-poster-card")
                    val moreLikeThisList = mutableListOf<MoreLikeThisResult>()
                    moreLikeThis?.forEach {
                        val mId = it.select("a.ipc-lockup-overlay").attr("href")
                            .substringAfter("title/").substringBefore("/")
                        val mTitle = it.select("img.ipc-image").attr("alt")
                        val mRating = it.select("span.ipc-rating-star--rating").first()?.text()
                            ?.toDouble()
                        val mPoster = it.select("img.ipc-image").attr("src")
                        moreLikeThisList.add(
                            MoreLikeThisResult(
                                imdbId = mId,
                                title = mTitle,
                                rating = mRating ?: 0.0,
                                poster = mPoster
                            )
                        )
                    }

                    val releaseDateLong =
                        soup?.select("li[data-testid=title-details-releasedate]")?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val countryOfOrigin =
                        soup?.select("li[data-testid=title-details-origin]")?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val languages =
                        soup?.select("li[data-testid=title-details-languages]")?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val alsoKnownAs =
                        soup?.select("li[data-testid=title-details-akas]")?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val filmingLocations =
                        soup?.select("li[data-testid=title-details-filminglocations]")
                            ?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val productionCompanies =
                        soup?.select("li[data-testid=title-details-companies]")?.select("div")
                            ?.first()
                            ?.select("a")?.text()

                    val ratingCount = soup?.select("div.sc-eb51e184-3.kgbSIj")?.first()?.text()
                        ?.replace("(", "")?.replace(")", "")?.replace(",", "")?.replace(" ", "")
                    val metaScore = soup?.select("span.metacritic-score-box")?.first()?.text()?.toInt()
                        ?: 0

                    // find text 'primevideo' and get the element
                    val tmBoxWbShoveler = soup?.select("div[data-testid=tm-box-wb-shoveler]")?.text()


                    println("IMDB: RatingCount: $ratingCount MetaScore: $metaScore Title: $tmBoxWbShoveler")

                    val mainTitle = MainTitle(
                        id = id,
                        title = title,
                        poster = poster ?: "",
                        altTitle = altTitle,
                        description = unescapeHtml(description),
                        rating = rating,
                        viewerClass = viewerClass,
                        duration = duration,
                        genres = genres,
                        releaseDate = releaseDate,
                        actors = actors,
                        trailer = trailer,
                        titleCasts = titleCasts,
                        moreLikeThis = moreLikeThisList,
                        releaseDateLong = releaseDateLong ?: "N/A",
                        countryOfOrigin = countryOfOrigin ?: "N/A",
                        languages = languages ?: "N/A",
                        alsoKnownAs = alsoKnownAs ?: "N/A",
                        filmingLocations = filmingLocations ?: "N/A",
                        productionCompanies = productionCompanies ?: "N/A",
                        ratingCount = ratingCount ?: "0",
                        metaScore = metaScore
                    )

                    store.value = mainTitle

                    Log.d("IMDB", "GetTitle - Took ${System.currentTimeMillis() - startTimer}ms")
                } catch (e: Exception) {
                    Log.e("IMDB", "Failed to parse response: ${e.message}")
                }
            }
        })
    }

    fun getTrailerSource(url: String, store: MutableState<Pair<String, String>?>) {
        val startTimer = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url(url)
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                )
            )
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("IMDB", "Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e(
                        "IMDB",
                        "Failed to execute request: ${response.code}, ${response.message}, url: ${request.url}"
                    )
                    return
                }
                try {
                    val soup = response.body?.string()?.let { org.jsoup.Jsoup.parse(it) }
                    val thumb = soup?.select("script[id=__NEXT_DATA__]")?.first()?.data()
                        ?.let { JSONObject(it) }
                        ?.getJSONObject("props")
                        ?.getJSONObject("pageProps")
                        ?.getJSONObject("videoPlaybackData")
                        ?.getJSONObject("video")
                        ?.getJSONObject("thumbnail")
                        ?.getString("url")
                    val trailerSource = soup?.select("script[id=__NEXT_DATA__]")?.first()?.data()
                        ?.let { JSONObject(it) }
                        ?.getJSONObject("props")
                        ?.getJSONObject("pageProps")
                        ?.getJSONObject("videoPlaybackData")
                        ?.getJSONObject("video")
                        ?.getJSONArray("playbackURLs")
                        ?.getJSONObject(0)
                        ?.getString("url")

                    store.value = Pair(thumb ?: "", trailerSource ?: "")
                    Log.d("IMDB", "TrailerSource - Took ${System.currentTimeMillis() - startTimer}ms")
                } catch (e: Exception) {
                    Log.e("IMDB", "Failed to parse response: ${e.message}")
                }
            }
        })
    }
}

fun unescapeHtml(html: String): String {
    val out = StringBuilder(html.length)
    var i = 0
    while (i < html.length) {
        if (html[i] == '&') {
            val start = i
            i++
            while (i < html.length && html[i] != ';') {
                i++
            }
            if (i < html.length) {
                val entity = html.substring(start + 1, i)
                when {
                    entity.startsWith("#x") -> {
                        out.append(entity.substring(2).toInt(16).toChar())
                    }
                    entity.startsWith("#") -> {
                        out.append(entity.substring(1).toInt().toChar())
                    }
                    else -> {
                        when (entity) {
                            "amp" -> out.append('&')
                            "quot" -> out.append('"')
                            "apos" -> out.append('\'')
                            "lt" -> out.append('<')
                            "gt" -> out.append('>')
                            else -> {
                                out.append('&').append(entity).append(';')
                            }
                        }
                    }
                }
            }
        } else {
            out.append(html[i])
        }
        i++
    }
    return out.toString()
}