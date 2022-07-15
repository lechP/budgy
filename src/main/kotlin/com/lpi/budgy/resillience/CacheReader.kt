package com.lpi.budgy.resillience

import java.io.File

class CacheReader(val cacheDirectory: String) {

    /**
     * [fetch] - function to be called when there's no cached data
     */
    fun readOrFetch(cacheKey: String, fetch: () -> String): String {
        val cacheFile = File("$cacheDirectory/$cacheKey")
        if(cacheFile.exists()) return cacheFile.readText()

        val text = fetch()
        File(cacheDirectory).mkdirs()
        cacheFile.writeText(text)
        return text
    }
}