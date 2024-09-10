package com.amarnath.cuestream.meta

import android.util.Log
import androidx.compose.runtime.MutableState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

val RottenMeterAPI =
    "https://79frdp12pn-dsn.algolia.net/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.24.0)%3B%20Browser%20(lite)&x-algolia-api-key=175588f6e5f8319b27702e4cc4013561&x-algolia-application-id=79FRDP12PN"

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

data class RottenMeter(
    val critic: Int,
    val audience: Int,
    val sentiment: String
)

data class MainTitle(
    val id: String,
    val title: String,
    val poster: String,
    val altTitle: String,
    val description: String,
    val rating: Double,
    val viewerClass: String,
    val duration: String,
    val genres: String,
    val releaseDate: String,
    val actors: String,
    val trailer: String,
    val titleCasts: List<Triple<String, String, String>>,
    val moreLikeThis: List<MoreLikeThisResult>,
    val releaseDateLong: String,
    val countryOfOrigin: String,
    val languages: String,
    val alsoKnownAs: String,
    val filmingLocations: String,
    val productionCompanies: String,
    val ratingCount: String = "0",
    val metaScore: Int = 0,
    var rottenMeter: RottenMeter = RottenMeter(0, 0, "N/A")
)

class IMDB {
    private val client = okhttp3.OkHttpClient()
    fun search(
        query: String,
        searchResults: MutableList<SearchResult>,
        fastRes: MutableList<AutoCompleteResult>
    ) {
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
                            fastRes[fastRes.indexOf(fastResItem)] = fastResItem.copy(
                                rating = temp[i].rating,
                                plot = temp[i].plot,
                                duration = temp[i].duration
                            )
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
                    val metaScore =
                        soup?.select("span.metacritic-score-box")?.first()?.text()?.toInt()
                            ?: 0

                    // find text 'primevideo' and get the element
                    val tmBoxWbShoveler =
                        soup?.select("div[data-testid=tm-box-wb-shoveler]")?.text()
                    // TODO: 'get available streaming services' from tmBoxWbShoveler

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

//                    val rreq = okhttp3.Request.Builder()
//                        .url(
//                            RottenMeterAPI
//                        )
//                        .headers(
//                            okhttp3.Headers.headersOf(
//                                "User-Agent",
//                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
//                            )
//                        )
//                        .post(
//                            okhttp3.RequestBody.create(
//                                "application/x-www-form-urlencoded".toMediaTypeOrNull(),
//                                "{\"requests\":[{\"indexName\":\"content_rt\",\"query\":\"$title\"}]}"
//                            )
//                        )
//                        .build()
//
//                    val resp = client.newCall(rreq).execute()
//                    try {
//                        val rottenMeter = resp.body?.string()?.let { JSONObject(it) }
//                        val hits = rottenMeter?.getJSONArray("results")?.getJSONObject(0)
//                            ?.getJSONArray("hits")
//
//                        // loop through hits and find the best match (title + year)
//                        var bestMatch = hits?.getJSONObject(0)
//                        for (i in 1 until hits!!.length()) {
//                            val hit = hits.getJSONObject(i)
//                            if (hit.optString("title").contains(title) && hit.optString("releaseYear").contains(releaseDate)) {
//                                bestMatch = hit
//                                break
//                            }
//                        }
//
//                        val tomato = bestMatch?.optJSONObject("rottenTomatoes")
//                        val critic = tomato?.optInt("criticsScore") ?: 0
//                        val audience = tomato?.optInt("audienceScore") ?: 0
//                        val sentiment = tomato?.optString("scoreSentiment") ?: "N/A"
//
//                        mainTitle.rottenMeter = RottenMeter(critic, audience, sentiment)
//                    } catch (e: Exception) {
//                        Log.e("IMDB", "Failed to parse rottenMeter response: ${e.message}")
//                    }
//
//                    store.value = mainTitle

                    val justWatch = JustWatch()
                    println("IMDB: JustWatch search for $title")
                    val a = System.currentTimeMillis()
                    val jw = justWatch.search(title, releaseDate)
                    println("IMDB: JustWatch search took ${System.currentTimeMillis() - a}ms")

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
                    Log.d(
                        "IMDB",
                        "TrailerSource - Took ${System.currentTimeMillis() - startTimer}ms"
                    )
                } catch (e: Exception) {
                    Log.e("IMDB", "Failed to parse response: ${e.message}")
                }
            }
        })
    }
}

data class JustWatchSearchResult(
    val id: String,
    val title: String,
    val year: String,
    val fullPath: String
)

data class JustWatchTitle(
    val id: String,
    val title: String,
    val year: String,
    val poster: String,
    val offers: List<OTT>,
    val jwRating: Double,
    val tomatoMeter: Int,
    val runtime: Int,
    val originalTitle: String,
    val ageCertification: String,
    val imdbId: String,
    val totalSeasonCount: Int,
    val seasons: List<JWSeason>,
    val backDrop: String,
    val clips: List<Clip>,
    val directors: List<String>,
)

data class JWSeason(
    val episodeCount: Int,
    val seasonName: String,
    val poster: String,
    val releaseYear: Int,
)

data class Clip(
    val title: String,
    val url: String
)

data class OTT(
    val name: String,
    val url: String,
)

class JustWatch {
    private val client = okhttp3.OkHttpClient()
    fun search(query: String, yearInp: String = "", imdbId: String = ""): JustWatchTitle? {
        val startTimer = System.currentTimeMillis()
        val country = "IT" // will be dynamic
        val language = "it"

        var requestBody =
            "{\"operationName\":\"GetSuggestedTitles\",\"variables\":{\"country\":\"$country\",\"language\":\"$language\",\"first\":4,\"filter\":{\"searchQuery\":\"$query\",\"includeTitlesWithoutUrl\":true}},\"query\":\"query GetSuggestedTitles(\$country: Country!, \$language: Language!, \$first: Int!, \$filter: TitleFilter) {\\n  popularTitles(country: \$country, first: \$first, filter: \$filter) {\\n    edges {\\n      node {\\n        ...SuggestedTitle\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment SuggestedTitle on MovieOrShow {\\n  id\\n  objectType\\n  objectId\\n  content(country: \$country, language: \$language) {\\n    fullPath\\n    title\\n    originalReleaseYear\\n    posterUrl\\n    fullPath\\n    __typename\\n  }\\n  watchNowOffer(country: \$country, platform: WEB) {\\n    id\\n    standardWebURL\\n    package {\\n      id\\n      packageId\\n      __typename\\n    }\\n    __typename\\n  }\\n  offers(country: \$country, platform: WEB) {\\n    monetizationType\\n    presentationType\\n    standardWebURL\\n    package {\\n      id\\n      packageId\\n      __typename\\n    }\\n    id\\n    __typename\\n  }\\n  __typename\\n}\\n\"}"

        var request = okhttp3.Request.Builder()
            .url("https://apis.justwatch.com/graphql")
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Android 11.0.0; JustWatch; 2.8.0; 0",
                    "Content-Type",
                    "application/json"
                )
            )
            .post(
                requestBody
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()

        var resp = client.newCall(request).execute()
        val searchResults = mutableListOf<JustWatchSearchResult>()

        try {
            val js = resp.body?.string()?.let { JSONObject(it) }
            val popularTitles = js?.getJSONObject("data")?.getJSONObject("popularTitles")
                ?.getJSONArray("edges")
            for (i in 0 until popularTitles!!.length()) {
                val node = popularTitles.getJSONObject(i).getJSONObject("node")
                val content = node.getJSONObject("content")
                val id = node.optString("id")
                val title = content.optString("title")
                val year = content.optString("originalReleaseYear")
                val fullPath = content.optString("fullPath")
                searchResults.add(JustWatchSearchResult(id, title, year, fullPath))
            }
        } catch (e: Exception) {
            Log.e("IMDB-JustWatch", "Failed to parse response: ${e.message}")
        }

        var mostMatching: JustWatchSearchResult? = null
        var currentMatch = 0.0
        val yearFromDate = getYearFromDate(yearInp)

        for (i in 0 until searchResults.size) {
            val match = stringsMatchPercentage(query, searchResults[i].title)

            if (match > currentMatch) {
                if (yearFromDate != 0 && yearFromDate.toString() != searchResults[i].year) {
                    continue
                }
                currentMatch = match
                mostMatching = searchResults[i]
            }
        }

        if (mostMatching != null) {
            Log.d("IMDB-JustWatch", "Found most matching title: ${mostMatching.title}")
        } else {
            Log.d("IMDB-JustWatch", "No matching title found")
            return null
        }

        requestBody =
            "{\"operationName\":\"GetNodeTitleDetails\",\"variables\":{\"platform\":\"WEB\",\"fullPath\":\"/\",\"entityId\":\"${mostMatching.id}\",\"language\":\"$language\",\"country\":\"$country\",\"episodeMaxLimit\":20,\"allowSponsoredRecommendations\":{\"pageType\":\"VIEW_TITLE_DETAIL\",\"placement\":\"DETAIL_PAGE\",\"language\":\"$language\",\"country\":\"$country\",\"applicationContext\":{\"appID\":\"3.8.2-webapp#5ab08d0\",\"platform\":\"webapp\",\"version\":\"3.8.2\",\"build\":\"5ab08d0\",\"isTestBuild\":false},\"appId\":\"3.8.2-webapp#5ab08d0\",\"platform\":\"WEB\",\"supportedFormats\":[\"IMAGE\",\"VIDEO\"],\"supportedObjectTypes\":[\"MOVIE\",\"SHOW\",\"GENERIC_TITLE_LIST\",\"SHOW_SEASON\"]}},\"query\":\"query GetNodeTitleDetails(\$entityId: ID!, \$country: Country!, \$language: Language!, \$episodeMaxLimit: Int, \$platform: Platform! = WEB, \$allowSponsoredRecommendations: SponsoredRecommendationsInput, \$format: ImageFormat, \$backdropProfile: BackdropProfile, \$streamingChartsFilter: StreamingChartsFilter) {\\n  node(id: \$entityId) {\\n    ... on Url {\\n      metaDescription\\n      metaKeywords\\n      metaRobots\\n      metaTitle\\n      heading1\\n      heading2\\n      htmlContent\\n      __typename\\n    }\\n    ...TitleDetails\\n    __typename\\n  }\\n}\\n\\nfragment TitleDetails on Node {\\n  id\\n  __typename\\n  ... on MovieOrShowOrSeason {\\n    plexPlayerOffers: offers(\\n      country: \$country\\n      platform: \$platform\\n      filter: {packages: [\\\"pxp\\\"]}\\n    ) {\\n      id\\n      standardWebURL\\n      package {\\n        id\\n        packageId\\n        clearName\\n        technicalName\\n        shortName\\n        __typename\\n      }\\n      __typename\\n    }\\n    maxOfferUpdatedAt(country: \$country, platform: WEB)\\n    appleOffers: offers(\\n      country: \$country\\n      platform: \$platform\\n      filter: {packages: [\\\"atp\\\", \\\"itu\\\"]}\\n    ) {\\n      ...TitleOffer\\n      __typename\\n    }\\n    disneyOffersCount: offerCount(\\n      country: \$country\\n      platform: \$platform\\n      filter: {packages: [\\\"dnp\\\"]}\\n    )\\n    starOffersCount: offerCount(\\n      country: \$country\\n      platform: \$platform\\n      filter: {packages: [\\\"srp\\\"]}\\n    )\\n    objectType\\n    objectId\\n    offerCount(country: \$country, platform: \$platform)\\n    uniqueOfferCount: offerCount(\\n      country: \$country\\n      platform: \$platform\\n      filter: {bestOnly: true}\\n    )\\n    offers(country: \$country, platform: \$platform) {\\n      monetizationType\\n      elementCount\\n      package {\\n        id\\n        packageId\\n        clearName\\n        __typename\\n      }\\n      __typename\\n    }\\n    watchNowOffer(country: \$country, platform: \$platform) {\\n      id\\n      standardWebURL\\n      __typename\\n    }\\n    promotedBundles(country: \$country, platform: \$platform) {\\n      promotionUrl\\n      __typename\\n    }\\n    availableTo(country: \$country, platform: \$platform) {\\n      availableCountDown(country: \$country)\\n      availableToDate\\n      package {\\n        id\\n        shortName\\n        __typename\\n      }\\n      __typename\\n    }\\n    fallBackClips: content(country: \$country, language: \\\"en\\\") {\\n      clips {\\n        ...TrailerClips\\n        __typename\\n      }\\n      videobusterClips: clips(providers: [VIDEOBUSTER]) {\\n        ...TrailerClips\\n        __typename\\n      }\\n      dailymotionClips: clips(providers: [DAILYMOTION]) {\\n        ...TrailerClips\\n        __typename\\n      }\\n      __typename\\n    }\\n    content(country: \$country, language: \$language) {\\n      backdrops {\\n        backdropUrl\\n        __typename\\n      }\\n      fullBackdrops: backdrops(profile: S1920, format: JPG) {\\n        backdropUrl\\n        __typename\\n      }\\n      clips {\\n        ...TrailerClips\\n        __typename\\n      }\\n      videobusterClips: clips(providers: [VIDEOBUSTER]) {\\n        ...TrailerClips\\n        __typename\\n      }\\n      dailymotionClips: clips(providers: [DAILYMOTION]) {\\n        ...TrailerClips\\n        __typename\\n      }\\n      externalIds {\\n        imdbId\\n        __typename\\n      }\\n      fullPath\\n      posterUrl\\n      fullPosterUrl: posterUrl(profile: S718, format: JPG)\\n      runtime\\n      isReleased\\n      scoring {\\n        imdbScore\\n        imdbVotes\\n        tmdbPopularity\\n        tmdbScore\\n        jwRating\\n        tomatoMeter\\n        certifiedFresh\\n        __typename\\n      }\\n      shortDescription\\n      title\\n      originalReleaseYear\\n      originalReleaseDate\\n      upcomingReleases(releaseTypes: DIGITAL) {\\n        releaseCountDown(country: \$country)\\n        releaseDate\\n        label\\n        package {\\n          id\\n          packageId\\n          shortName\\n          clearName\\n          icon(profile: S100)\\n          hasRectangularIcon(country: \$country, platform: WEB)\\n          __typename\\n        }\\n        __typename\\n      }\\n      genres {\\n        shortName\\n        translation(language: \$language)\\n        __typename\\n      }\\n      subgenres {\\n        content(country: \$country, language: \$language) {\\n          shortName\\n          name\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on MovieOrShowOrSeasonContent {\\n        subgenres {\\n          content(country: \$country, language: \$language) {\\n            url: moviesUrl {\\n              fullPath\\n              __typename\\n            }\\n            __typename\\n          }\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on MovieOrShowContent {\\n        originalTitle\\n        ageCertification\\n        credits {\\n          role\\n          name\\n          characterName\\n          personId\\n          __typename\\n        }\\n        interactions {\\n          dislikelistAdditions\\n          likelistAdditions\\n          votesNumber\\n          __typename\\n        }\\n        productionCountries\\n        __typename\\n      }\\n      ... on SeasonContent {\\n        seasonNumber\\n        interactions {\\n          dislikelistAdditions\\n          likelistAdditions\\n          votesNumber\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    popularityRank(country: \$country) {\\n      rank\\n      trend\\n      trendDifference\\n      __typename\\n    }\\n    streamingCharts(country: \$country, filter: \$streamingChartsFilter) {\\n      edges {\\n        streamingChartInfo {\\n          rank\\n          trend\\n          trendDifference\\n          updatedAt\\n          daysInTop10\\n          daysInTop100\\n          daysInTop1000\\n          daysInTop3\\n          topRank\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  ... on MovieOrShowOrSeason {\\n    likelistEntry {\\n      createdAt\\n      __typename\\n    }\\n    dislikelistEntry {\\n      createdAt\\n      __typename\\n    }\\n    __typename\\n  }\\n  ... on MovieOrShow {\\n    watchlistEntryV2 {\\n      createdAt\\n      __typename\\n    }\\n    customlistEntries {\\n      createdAt\\n      genericTitleList {\\n        id\\n        __typename\\n      }\\n      __typename\\n    }\\n    similarTitlesV2(\\n      country: \$country\\n      allowSponsoredRecommendations: \$allowSponsoredRecommendations\\n    ) {\\n      sponsoredAd {\\n        ...SponsoredAd\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  ... on Movie {\\n    permanentAudiences\\n    seenlistEntry {\\n      createdAt\\n      __typename\\n    }\\n    __typename\\n  }\\n  ... on Show {\\n    permanentAudiences\\n    totalSeasonCount\\n    seenState(country: \$country) {\\n      progress\\n      seenEpisodeCount\\n      __typename\\n    }\\n    tvShowTrackingEntry {\\n      createdAt\\n      __typename\\n    }\\n    seasons(sortDirection: DESC) {\\n      id\\n      objectId\\n      objectType\\n      totalEpisodeCount\\n      availableTo(country: \$country, platform: \$platform) {\\n        availableToDate\\n        availableCountDown(country: \$country)\\n        package {\\n          id\\n          shortName\\n          __typename\\n        }\\n        __typename\\n      }\\n      content(country: \$country, language: \$language) {\\n        posterUrl\\n        seasonNumber\\n        fullPath\\n        title\\n        upcomingReleases(releaseTypes: DIGITAL) {\\n          releaseDate\\n          releaseCountDown(country: \$country)\\n          package {\\n            id\\n            shortName\\n            __typename\\n          }\\n          __typename\\n        }\\n        isReleased\\n        originalReleaseYear\\n        __typename\\n      }\\n      show {\\n        id\\n        objectId\\n        objectType\\n        watchlistEntryV2 {\\n          createdAt\\n          __typename\\n        }\\n        content(country: \$country, language: \$language) {\\n          title\\n          __typename\\n        }\\n        __typename\\n      }\\n      fallBackClips: content(country: \$country, language: \\\"en\\\") {\\n        clips {\\n          ...TrailerClips\\n          __typename\\n        }\\n        videobusterClips: clips(providers: [VIDEOBUSTER]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        dailymotionClips: clips(providers: [DAILYMOTION]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    recentEpisodes: episodes(\\n      sortDirection: DESC\\n      limit: 3\\n      releasedInCountry: \$country\\n    ) {\\n      ...Episode\\n      __typename\\n    }\\n    __typename\\n  }\\n  ... on Season {\\n    totalEpisodeCount\\n    episodes(limit: \$episodeMaxLimit) {\\n      ...Episode\\n      __typename\\n    }\\n    show {\\n      id\\n      objectId\\n      objectType\\n      totalSeasonCount\\n      customlistEntries {\\n        createdAt\\n        genericTitleList {\\n          id\\n          __typename\\n        }\\n        __typename\\n      }\\n      tvShowTrackingEntry {\\n        createdAt\\n        __typename\\n      }\\n      fallBackClips: content(country: \$country, language: \\\"en\\\") {\\n        clips {\\n          ...TrailerClips\\n          __typename\\n        }\\n        videobusterClips: clips(providers: [VIDEOBUSTER]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        dailymotionClips: clips(providers: [DAILYMOTION]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        __typename\\n      }\\n      content(country: \$country, language: \$language) {\\n        title\\n        ageCertification\\n        fullPath\\n        genres {\\n          shortName\\n          __typename\\n        }\\n        credits {\\n          role\\n          name\\n          characterName\\n          personId\\n          __typename\\n        }\\n        productionCountries\\n        externalIds {\\n          imdbId\\n          __typename\\n        }\\n        upcomingReleases(releaseTypes: DIGITAL) {\\n          releaseDate\\n          __typename\\n        }\\n        backdrops {\\n          backdropUrl\\n          __typename\\n        }\\n        posterUrl\\n        isReleased\\n        videobusterClips: clips(providers: [VIDEOBUSTER]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        dailymotionClips: clips(providers: [DAILYMOTION]) {\\n          ...TrailerClips\\n          __typename\\n        }\\n        __typename\\n      }\\n      seenState(country: \$country) {\\n        progress\\n        __typename\\n      }\\n      watchlistEntryV2 {\\n        createdAt\\n        __typename\\n      }\\n      dislikelistEntry {\\n        createdAt\\n        __typename\\n      }\\n      likelistEntry {\\n        createdAt\\n        __typename\\n      }\\n      similarTitlesV2(\\n        country: \$country\\n        allowSponsoredRecommendations: \$allowSponsoredRecommendations\\n      ) {\\n        sponsoredAd {\\n          ...SponsoredAd\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    seenState(country: \$country) {\\n      progress\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment TitleOffer on Offer {\\n  id\\n  presentationType\\n  monetizationType\\n  retailPrice(language: \$language)\\n  retailPriceValue\\n  currency\\n  lastChangeRetailPriceValue\\n  type\\n  package {\\n    id\\n    packageId\\n    clearName\\n    technicalName\\n    icon(profile: S100)\\n    planOffers(country: \$country, platform: WEB) {\\n      title\\n      retailPrice(language: \$language)\\n      isTrial\\n      durationDays\\n      retailPriceValue\\n      children {\\n        title\\n        retailPrice(language: \$language)\\n        isTrial\\n        durationDays\\n        retailPriceValue\\n        __typename\\n      }\\n      __typename\\n    }\\n    hasRectangularIcon(country: \$country, platform: WEB)\\n    __typename\\n  }\\n  standardWebURL\\n  elementCount\\n  availableTo\\n  deeplinkRoku: deeplinkURL(platform: ROKU_OS)\\n  subtitleLanguages\\n  videoTechnology\\n  audioTechnology\\n  audioLanguages(language: \$language)\\n  __typename\\n}\\n\\nfragment TrailerClips on Clip {\\n  sourceUrl\\n  externalId\\n  provider\\n  name\\n  __typename\\n}\\n\\nfragment SponsoredAd on SponsoredRecommendationAd {\\n  bidId\\n  holdoutGroup\\n  campaign {\\n    name\\n    externalTrackers {\\n      type\\n      data\\n      __typename\\n    }\\n    hideRatings\\n    hideDetailPageButton\\n    promotionalImageUrl\\n    promotionalVideo {\\n      url\\n      __typename\\n    }\\n    promotionalTitle\\n    promotionalText\\n    promotionalProviderLogo\\n    watchNowLabel\\n    watchNowOffer {\\n      standardWebURL\\n      presentationType\\n      monetizationType\\n      package {\\n        id\\n        packageId\\n        shortName\\n        clearName\\n        icon\\n        __typename\\n      }\\n      __typename\\n    }\\n    nodeOverrides {\\n      nodeId\\n      promotionalImageUrl\\n      watchNowOffer {\\n        standardWebURL\\n        __typename\\n      }\\n      __typename\\n    }\\n    node {\\n      nodeId: id\\n      __typename\\n      ... on MovieOrShowOrSeason {\\n        content(country: \$country, language: \$language) {\\n          fullPath\\n          posterUrl\\n          title\\n          originalReleaseYear\\n          scoring {\\n            imdbScore\\n            __typename\\n          }\\n          externalIds {\\n            imdbId\\n            __typename\\n          }\\n          backdrops(format: \$format, profile: \$backdropProfile) {\\n            backdropUrl\\n            __typename\\n          }\\n          isReleased\\n          __typename\\n        }\\n        objectId\\n        objectType\\n        offers(country: \$country, platform: \$platform) {\\n          monetizationType\\n          presentationType\\n          package {\\n            id\\n            packageId\\n            __typename\\n          }\\n          id\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on MovieOrShow {\\n        watchlistEntryV2 {\\n          createdAt\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on Show {\\n        seenState(country: \$country) {\\n          seenEpisodeCount\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on Season {\\n        content(country: \$country, language: \$language) {\\n          seasonNumber\\n          __typename\\n        }\\n        show {\\n          __typename\\n          id\\n          content(country: \$country, language: \$language) {\\n            originalTitle\\n            __typename\\n          }\\n          watchlistEntryV2 {\\n            createdAt\\n            __typename\\n          }\\n        }\\n        __typename\\n      }\\n      ... on GenericTitleList {\\n        followedlistEntry {\\n          createdAt\\n          name\\n          __typename\\n        }\\n        id\\n        type\\n        content(country: \$country, language: \$language) {\\n          name\\n          visibility\\n          __typename\\n        }\\n        titles(country: \$country, first: 40) {\\n          totalCount\\n          edges {\\n            cursor\\n            node: nodeV2 {\\n              content(country: \$country, language: \$language) {\\n                fullPath\\n                posterUrl\\n                title\\n                originalReleaseYear\\n                scoring {\\n                  imdbScore\\n                  __typename\\n                }\\n                isReleased\\n                __typename\\n              }\\n              id\\n              objectId\\n              objectType\\n              __typename\\n            }\\n            __typename\\n          }\\n          __typename\\n        }\\n        __typename\\n      }\\n    }\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment Episode on Episode {\\n  id\\n  objectId\\n  objectType\\n  seenlistEntry {\\n    createdAt\\n    __typename\\n  }\\n  content(country: \$country, language: \$language) {\\n    title\\n    shortDescription\\n    episodeNumber\\n    seasonNumber\\n    isReleased\\n    runtime\\n    upcomingReleases {\\n      releaseDate\\n      label\\n      package {\\n        id\\n        packageId\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  __typename\\n}\\n\"}"
        request = okhttp3.Request.Builder()
            .url("https://apis.justwatch.com/graphql")
            .headers(
                okhttp3.Headers.headersOf(
                    "User-Agent",
                    "Android 11.0.0; JustWatch; 2.8.0; 0",
                    "Content-Type",
                    "application/json"
                )
            )
            .post(
                requestBody
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()

        resp = client.newCall(request).execute()
        val js = resp.body?.string()?.let { JSONObject(it) }
        val urlV2 = js?.getJSONObject("data")
        val node = urlV2?.getJSONObject("node")

        try {
            val content = node?.getJSONObject("content") ?: return null

            val offers = node.optJSONArray("offers")
            val jwRating = content.optJSONObject("scoring")?.optDouble("jwRating") ?: 0.0
            val tomatoMeter = content.optJSONObject("scoring")?.optInt("tomatoMeter") ?: 0
            val runtime = content.optInt("runtime")
            val originalTitle = content.optString("originalTitle")
            val ageCertification = content.optString("ageCertification")
            val imdbID = content.getJSONObject("externalIds").optString("imdbId")
            val totalSeasonCount = node.optInt("totalSeasonCount")
            val seasons = mutableListOf<JWSeason>()
            val backDrop =
                content.getJSONArray("backdrops").getJSONObject(0).optString("backdropUrl")
            val clips = mutableListOf<Clip>()
            val directors = mutableListOf("N/A")
            for (i in 0 until content.getJSONArray("credits").length()) {
                val credit = content.getJSONArray("credits").getJSONObject(i)
                if (credit.optString("role") == "DIRECTOR") {
                    directors[0] = credit.optString("name")
                    break
                }
            }

            val clipsArr = node.optJSONObject("fallBackClips")?.optJSONArray("clips")
            for (i in 0 until clipsArr!!.length()) {
                val clip = clipsArr.getJSONObject(i)
                val title = clip.optString("name")
                val url = clip.optString("sourceUrl")
                clips.add(Clip(title, url))
            }

            val seasonsArr = node.getJSONArray("seasons")
            for (i in 0 until seasonsArr.length()) {
                val season = seasonsArr.getJSONObject(i)
                val episodeCount = season.optInt("totalEpisodeCount")
                val seasonName = season.optJSONObject("content")?.optString("title")
                val releaseYear =
                    season.optJSONObject("content")?.optInt("originalReleaseYear") ?: 0
                val poster = season.optJSONObject("content")?.optString("posterUrl")
                seasons.add(
                    JWSeason(
                        episodeCount,
                        seasonName ?: "N/A",
                        poster ?: "N/A",
                        releaseYear
                    )
                )
            }

            val title = JustWatchTitle(
                id = node.optString("id"),
                title = content.optString("title"),
                year = content.optString("originalReleaseYear"),
                poster = content.optString("posterUrl"),
                offers = offers?.let { of ->
                    val list = mutableListOf<OTT>()
                    for (i in 0 until of.length()) {
                        val offer = of.getJSONObject(i)
                        val name = offer.optJSONObject("package")?.optString("clearName")
                        val url = ""
                        list.add(OTT(name ?: "N/A", url))
                    }
                    list
                } ?: emptyList(),
                jwRating = jwRating,
                tomatoMeter = tomatoMeter,
                runtime = runtime,
                originalTitle = originalTitle,
                ageCertification = ageCertification,
                imdbId = imdbID,
                totalSeasonCount = totalSeasonCount,
                seasons = seasons,
                backDrop = backDrop,
                clips = clips,
                directors = directors
            )

            if (imdbId.isNotEmpty()) {
                return if (imdbId == imdbID) {
                    title
                } else {
                    null
                }
            } else {
                return title
            }
        } catch (e: Exception) {
            val line = e.stackTrace[0].lineNumber
            Log.e("IMDB-JustWatch", "Failed to parse response: ${e.message} at line $line")
        }

        Log.d("IMDB-JustWatch", "Search - Took ${System.currentTimeMillis() - startTimer}ms")
        return null
    }
}

