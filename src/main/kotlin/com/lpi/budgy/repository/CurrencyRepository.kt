package com.lpi.budgy.repository

import com.lpi.budgy.domain.Currency
import com.lpi.budgy.domain.CurrencyNotFound

class CurrencyRepository: FileRepository() {

    private val data: List<Currency> by lazy {
        readDataFromJson("currencies")
    }

    fun find(id: String) = data.find { it.id == id } ?: throw CurrencyNotFound(id)

    fun getAll() = data

}