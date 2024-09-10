package com.amarnath.cuestream.meta

import java.util.Locale

fun parseDurationAndViewerClass(meta: org.jsoup.select.Elements): Pair<String, String> {
    if (meta.size == 1) {
        return Pair("N/A", "N/A")
    }
    if (meta.size == 2) {
        val metadata = meta[1].text()
        return if (metadata.contains("h") || metadata.contains("m")) {
            Pair(metadata, "N/A")
        } else {
            Pair("N/A", metadata)
        }
    }
    val metadata = meta[1].text()
    val metadata2 = meta[2].text()

    return if (metadata.contains("h") || metadata.contains("m")) {
        Pair(metadata, metadata2)
    } else {
        Pair(metadata2, metadata)
    }
}

fun tOrN(value: String): String {
    return value.ifEmpty {
        "N/A"
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

fun stringsMatchPercentage(s1: String, s2: String): Double {
    val longer = if (s1.length > s2.length) s1 else s2
    val shorter = if (s1.length > s2.length) s2 else s1
    val longerLength = longer.length
    if (longerLength == 0) {
        return 1.0
    }
    return (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
}

fun editDistance(string1: String, string2: String): Int {
    var s1 = string1
    var s2 = string2
    s1 = s1.lowercase(Locale.ROOT)
    s2 = s2.lowercase(Locale.ROOT)

    val costs = IntArray(s2.length + 1)
    for (i in 0..s1.length) {
        var lastValue = i
        for (j in 0..s2.length) {
            if (i == 0) {
                costs[j] = j
            } else {
                if (j > 0) {
                    var newValue = costs[j - 1]
                    if (s1[i - 1] != s2[j - 1]) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1
                    }
                    costs[j - 1] = lastValue
                    lastValue = newValue
                }
            }
        }
        if (i > 0) {
            costs[s2.length] = lastValue
        }
    }
    return costs[s2.length]
}

fun getYearFromDate(date: String): Int {
    val cnt = date.split("-")
    for (i in cnt.indices) {
        if (cnt[i].length == 4) {
            return cnt[i].toInt()
        }
    }
    return 0
}