package com.lpi.budgy.currency

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lpi.budgy.config.Config
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

class CurrencyConverter(private val config: Config) {

    private val baseUrl = "https://api.getgeoapi.com/v2/currency/historical/"

    private val ratesCache: MutableMap<ExchangeRateKey, Double> = mutableMapOf()

    fun convert(amount: Double, from: String, to: String, date: LocalDate): Double {
        if(from == to) return amount
        return rateForCurrencyAtDate(date, from, to) * amount
    }

    private fun rateForCurrencyAtDate(date: LocalDate, from: String, to: String): Double {
        val key = ExchangeRateKey(date, from, to)

        val cachedRate = ratesCache[key]
        if(cachedRate != null) {
            return cachedRate
        }

        val url = "$baseUrl$date?api_key=${config.currencyGetGeoApiKey}&from=$from&to=$to"
        val request = HttpRequest.newBuilder(URI.create(url)).build()
        val client = HttpClient.newBuilder().build()
        val json = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        val response = jacksonObjectMapper().readValue(json, CurrencyApiResponse::class.java)
        val rate = response.rates[to]!!.rate
        ratesCache[key] = rate
        return rate
    }


    private data class ExchangeRateKey(
        private val date: LocalDate,
        private val from: String,
        private val to: String
    )

}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class CurrencyApiResponse(
    val rates: Map<String, CurrencyApiRate>
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class CurrencyApiRate(val rate: Double)