package com.lpi.budgy.config

import java.io.FileInputStream
import java.util.*

class Config {

    private val properties: Properties by lazy {
        val props = Properties()
        props.load(FileInputStream("application.properties"))
        props
    }

    val currencyGetGeoApiKey: String by lazy {
        properties.getProperty("currencyGetGeoApi.apiKey")
    }

    val alphaVantageApiKeys: List<String> by lazy {
        properties.getProperty("alphaVantage.apiKeys").split(Regex(",\\s*"))
    }
}