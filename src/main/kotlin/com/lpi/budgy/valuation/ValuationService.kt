package com.lpi.budgy.valuation

import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.domain.Balance
import com.lpi.budgy.domain.Currency
import com.lpi.budgy.domain.MonetaryBalance
import com.lpi.budgy.domain.StocksBalance
import com.lpi.budgy.stock.StockApi
import java.time.LocalDate

class ValuationService(
    private val currencyConverter: CurrencyConverter,
    private val stockApi: StockApi
) {

    fun value(balance: Balance, currency: Currency, date: LocalDate): Double = when (balance) {
        is MonetaryBalance ->
            currencyConverter.convert(balance.value, balance.asset.currency.id, currency.id, date)
        is StocksBalance ->
            balance.stocksAmounts.map { (symbol, amount) -> stockApi.value(symbol, currency, date) * amount }.sum()
    }
}