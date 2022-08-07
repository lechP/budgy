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
import com.lpi.budgy.report.TerminalReport
import com.lpi.budgy.report.TerminalReportOptions
import com.lpi.budgy.report.WebReport
import com.lpi.budgy.repository.*
import com.lpi.budgy.stock.AlphaVantageApi
import com.lpi.budgy.stock.StockApi
import com.lpi.budgy.valuation.ValuationService
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.conf.global
import org.kodein.di.instance

class Budgy(
    private val book: Book
) : CliktCommand() {

    private val displayTotalsByRiskLevel by option(help = "Display totals by risk level").flag()
    private val displayTags by option(help = "Display tags for each account").flag()
    private val filterByTag by option(help = "Filter by tag").choice(*book.tags.map { it.name }.toTypedArray())
        .convert { tagName -> book.tags.first { it.name == tagName } }
    private val web by option(help = "Start a web server").flag()

    override fun run() {

        val options = TerminalReportOptions(
            displayTotalsByRiskLevel = displayTotalsByRiskLevel,
            displayTags = displayTags,
            filterByTag = filterByTag
        )

        DI.global.addConfig {
            // TODO read more about DI, how those singletons might be improved?
            bindSingleton { Config() }
            bindSingleton { CurrencyConverter(instance()) }
            bindSingleton { CacheReader(".cache") }

            bindSingleton { CurrencyRepository() }
            bindSingleton { InstitutionRepository() }
            bindSingleton { RiskLevelRepository() }

            bindSingleton { AssetRepository(instance(), instance(), instance()) }
            bindSingleton { SnapshotRepository(instance()) }

            bindSingleton<StockApi> { AlphaVantageApi(instance(), instance(), instance(), instance()) }
            bindSingleton { ValuationService(instance(), instance()) }

            bindSingleton { WebReport(book, instance(), instance()) }
            bindSingleton { TerminalReport(book, instance(), instance(), options) }
        }



        if (web) {
            val webReport: WebReport by DI.global.instance()
            webReport.display()
        } else {
            val terminalReport: TerminalReport by DI.global.instance()
            terminalReport.displayAsTable()
        }
    }

}