package com.lpi.budgy.stock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lpi.budgy.domain.Currency
import com.lpi.budgy.config.Config
import com.lpi.budgy.resillience.CacheReader
import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.repository.SnapshotRepository
import com.lpi.budgy.resillience.RetrySystem
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

interface StockApi {
    fun value(symbol: String, currency: Currency, date: LocalDate): Double
}

class AlphaVantageApi(
    config: Config,
    snapshotRepository: SnapshotRepository,
    private val currencyConverter: CurrencyConverter,
    private val cacheReader: CacheReader
) : StockApi {

    //https://marketstack.com/ - alternative API, which covers WSE

    private val retrySystem = RetrySystem(config.alphaVantageApiKeys)

    override fun value(symbol: String, currency: Currency, date: LocalDate): Double {
        val price = valueInDefaultCurrency(symbol, date)
        return currencyConverter.convert(
            price, symbolCurrencies[symbol]!!, currency.id, date
        )
    }

    private val symbolCurrencies: MutableMap<String, String> = mutableMapOf()
    private val quotes: MutableMap<String, MutableMap<LocalDate, Double>> = mutableMapOf()

    init {
        for (stock in snapshotRepository.getAllStockSymbols()) {
            saveStockCurrency(stock)
            saveDailyQuoteForStock(stock)
        }
        for (crypto in snapshotRepository.getAllCryptoSymbols()) {
            saveCryptoCurrency(crypto)
            saveDailyQuoteForCrypto(crypto)
        }
    }

    private fun valueInDefaultCurrency(symbol: String, date: LocalDate): Double {
        val symbolQuotes = quotes[symbol] ?: throw RuntimeException("No quote for symbol $symbol")
        val minDate = symbolQuotes.keys.minOrNull() ?: throw RuntimeException("No dates in quote for symbol $symbol")
        if (minDate > date) throw RuntimeException("No quote for date $date")
        var prevDate = date
        while (true) {
            val dayQuote = symbolQuotes[prevDate]
            if (dayQuote != null) {
                symbolQuotes[date] = dayQuote
                return dayQuote
            }
            prevDate = prevDate.minusDays(1)
        }
    }

    private fun jsonAtUrl(url: String, cacheKey: String): JsonNode {
        val apiCall = { apiKey: String ->
            val client = HttpClient.newBuilder().build()
            val urlWithApiKey = url.replace("API_KEY", apiKey)
            val request = HttpRequest.newBuilder().uri(URI.create(urlWithApiKey)).build()
            val json = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
            if (json.contains("Thank you for using Alpha Vantage"))
                throw ApiRateException()
            json
        }

        val json = cacheReader.readOrFetch(cacheKey) {
            retrySystem.retry(apiCall)
        }
        return jacksonObjectMapper().readTree(json)
    }

    private fun saveDailyQuoteForStock(symbol: String) {
        val url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=$symbol&apikey=API_KEY"
        val result = jsonAtUrl(url, "stock-$symbol-${LocalDate.now()}")
        val timeSeries = result.get("Time Series (Daily)")
        val symbolQuotes: MutableMap<LocalDate, Double> = mutableMapOf()
        for (date in timeSeries.fieldNames()) {
            symbolQuotes[LocalDate.parse(date)] = timeSeries.get(date).get("4. close").asDouble()
            quotes[symbol] = symbolQuotes
        }
    }

    private fun saveDailyQuoteForCrypto(symbol: String) {
        val url =
            "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&market=USD&symbol=$symbol&apikey=API_KEY"
        val result = jsonAtUrl(url, "crypto-$symbol-${LocalDate.now()}")
        val timeSeries = result.get("Time Series (Digital Currency Daily)")
        val symbolQuotes: MutableMap<LocalDate, Double> = mutableMapOf()
        for (date in timeSeries.fieldNames()) {
            symbolQuotes[LocalDate.parse(date)] = timeSeries.get(date).get("4b. close (USD)").asDouble()
        }
        quotes[symbol] = symbolQuotes
    }

    private fun saveStockCurrency(symbol: String) {
        val url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=$symbol&apikey=API_KEY"
        val result = jsonAtUrl(url, "overview-$symbol")
        symbolCurrencies[symbol] = result.get("Currency").asText()
    }

    // This method is here for consistency only, // as crypto prices are returned in USD:
    private fun saveCryptoCurrency(symbol: String) {
        symbolCurrencies[symbol] = "USD"
    }

}

class ApiRateException : RuntimeException("API Rate Exception")