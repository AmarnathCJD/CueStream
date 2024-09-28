package com.amarnath.cuestream.meta

import com.amarnath.cuestream.titles.ActiveWatchListEntries
import com.amarnath.cuestream.titles.WLEntry

fun SaveWatchListEntry(WL: WLEntry, ctx: android.content.Context) {
    val sharedPref = ctx.getSharedPreferences("watchlistxd", android.content.Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.putString(WL.imdbID, WL.toString())
    editor.apply()
}

fun DeleteWatchListEntry(WL: WLEntry, ctx: android.content.Context) {
    val sharedPref = ctx.getSharedPreferences("watchlistxd", android.content.Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.remove(WL.imdbID)
    editor.apply()
}

fun LoadWatchList(ctx: android.content.Context) {
    val sharedPref = ctx.getSharedPreferences("watchlistxd", android.content.Context.MODE_PRIVATE)
    val entries = sharedPref.all
    val ret = mutableListOf<WLEntry>()
    for (entry in entries) {
        ret.add(WLEntry.fromString(entry.value as String))
    }

    if (ActiveWatchListEntries.isEmpty()) {
        ActiveWatchListEntries.addAll(ret)
    } else {
        for (entry in ret) {
            if (!ActiveWatchListEntries.contains(entry)) {
                ActiveWatchListEntries.add(entry)
            }
        }
    }
}
