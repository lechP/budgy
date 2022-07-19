package com.lpi.budgy.repository

import com.lpi.budgy.domain.Currency

class CurrencyRepository: FileRepository() {

    private val data: Set<Currency> by lazy {
        readDataFromJson("currencies")
    }

    fun find(id: String) = data.find { it.id == id }

    fun getAll() = data

}