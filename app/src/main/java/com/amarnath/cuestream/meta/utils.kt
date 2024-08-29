package com.amarnath.cuestream.meta

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