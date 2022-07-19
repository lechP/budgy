package com.lpi.budgy.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lpi.budgy.domain.Currency

class CurrencyRepository {

    private val data: Set<Currency> by lazy {
        val currencyJson = object{}.javaClass.getResource("/data/currencies.json")?.readText()
        jacksonObjectMapper().readValue(currencyJson, jacksonObjectMapper().typeFactory.constructCollectionType(Set::class.java, Currency::class.java))
    }

    fun find(id: String) = data.find { it.id == id }

    fun getAll() = data

}