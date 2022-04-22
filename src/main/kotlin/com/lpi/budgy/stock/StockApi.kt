package com.lpi.budgy.stock

import com.lpi.budgy.Currency
import java.time.LocalDate

interface StockApi {
    fun value(symbol: String, currency: Currency, date: LocalDate): Double
}