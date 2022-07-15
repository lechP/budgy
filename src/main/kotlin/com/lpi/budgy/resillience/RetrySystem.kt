package com.lpi.budgy.resillience

import com.lpi.budgy.stock.ApiRateException
import java.time.LocalDateTime

class RetrySystem(
    private val apiKeys: List<String>,
    private val delayMs: Long = 10_000,
    private val maxAttempts: Int = 10
) {
    fun <T> retry(fn: (apiKey: String) -> T): T {
        repeat(maxAttempts) {
            repeat(apiKeys.count()) {
                try {
                    return fn(nextApiKey())
                } catch (e: ApiRateException) {
                    println("API rate limit reached for API key #$nextKeyIndex at ${LocalDateTime.now()}")
                }
            }
            println("API rate limit reached for all API keys, waiting ${delayMs}ms before retrying...")
            Thread.sleep(delayMs)
        }
        throw RuntimeException("None of the keys worked")
    }

    private var nextKeyIndex = 0

    private fun nextApiKey(): String {
        val key = apiKeys[nextKeyIndex]
        nextKeyIndex = (nextKeyIndex + 1) % apiKeys.size
        return key
    }
}