package com.lpi.budgy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.lpi.budgy.config.Config
import com.lpi.budgy.currency.CurrencyConverter
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
        DI.global.addConfig {
            bindSingleton { Config() }
            bindSingleton { CurrencyConverter() }
        }

        val options = TerminalReportOptions(
            displayTotalsByRiskLevel = displayTotalsByRiskLevel,
            displayTags = displayTags,
            filterByTag = filterByTag
        )
        TerminalReport(book, snapshots, options).displayAsTable()
    }
}