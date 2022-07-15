package com.lpi.budgy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.lpi.budgy.resillience.CacheReader
import com.lpi.budgy.config.Config
import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.domain.Book
import com.lpi.budgy.domain.Snapshot
import com.lpi.budgy.domain.StocksBalance
import com.lpi.budgy.report.TerminalReport
import com.lpi.budgy.report.TerminalReportOptions
import com.lpi.budgy.stock.AlphaVantageApi
import com.lpi.budgy.stock.StockApi
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.conf.global

class Budgy(
    private val book: Book,
    private val snapshots: List<Snapshot>
) : CliktCommand() {

    private val displayTotalsByRiskLevel by option(help = "Display totals by risk level").flag()
    private val displayTags by option(help = "Display tags for each account").flag()
    private val filterByTag by option(help = "Filter by tag").choice(*book.tags.map { it.name }.toTypedArray())
        .convert { tagName -> book.tags.first { it.name == tagName } }

    override fun run() {
        val (stocks, cryptos) = snapshots.stockAndCryptoSymbols()

        DI.global.addConfig {
            bindSingleton { Config() }
            bindSingleton { CurrencyConverter() }
            bindSingleton<StockApi> { AlphaVantageApi(stocks, cryptos) }
            bindSingleton { CacheReader(".cache") }
        }

        val options = TerminalReportOptions(
            displayTotalsByRiskLevel = displayTotalsByRiskLevel,
            displayTags = displayTags,
            filterByTag = filterByTag
        )
        TerminalReport(book, snapshots, options).displayAsTable()
    }

    // ohh refactor me (start with splitting StocksBalance into SharesBalance and CryptosBalance
    private fun List<Snapshot>.stockAndCryptoSymbols(): Pair<Set<String>, Set<String>> {
        val stocks = mutableSetOf<String>()
        val cryptos = mutableSetOf<String>()
        forEach { snapshot ->
            for (balance in snapshot.balances) {
                if (balance is StocksBalance) {
                    val symbols = balance.stocksAmounts.keys
                    if (balance.isCrypto) {
                        cryptos.addAll(symbols)
                    } else {
                        stocks.addAll(symbols)
                    }
                }
            }
        }
        return Pair(stocks, cryptos)
    }
}