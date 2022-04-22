package com.lpi.budgy

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.TableBuilder
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TerminalReportOptions(
    val displayTotalsByRiskLevel: Boolean = false,
    val displayTags: Boolean = false,
    val filterByTag: Tag? = null,
)

class TerminalReport(
    private val book: Book,
    private val snapshots: List<Snapshot>,
    private val options: TerminalReportOptions = TerminalReportOptions()
) {
    private val dateFormat = DateTimeFormatter.ofPattern("MMM dd, u")

    private val table = table {
        column(0) { width = ColumnWidth.Fixed(4) }
        borderStyle = BorderStyle.SQUARE_DOUBLE_SECTION_SEPARATOR
        headerRow()
        accountsRows()
        if (options.displayTotalsByRiskLevel) {
            totalByRiskLevel()
        }
        footerRow()
    }

    private fun TableBuilder.headerRow() {
        header {
            row {
                cell("") { columnSpan = 2 }
                snapshots.map {
                    cell(it.date.format(dateFormat))
                }
            }
        }
    }

    private fun TableBuilder.accountsRows() {
        body {
            book.institutions.filter { book.accountsIn(it).applyTagFilter().isNotEmpty() }.map {
                row {
                    style(TextColors.brightYellow, bold = true)
                    cell(it.name) { columnSpan = 2 + snapshots.size }
                }
                book.accountsIn(it).applyTagFilter().map { account ->
                    row {
                        borders = Borders.LEFT_RIGHT
                        cell(account.metadata.riskLevel?.symbol ?: "")
                        cell(account.label())
                        snapshots.map { snapshot ->
                            cell(formatBalance(snapshot.accountBalance(account), snapshot.date)) { align = TextAlign.RIGHT }
                        }
                    }
                }
            }
        }
    }

    private fun List<Account>.applyTagFilter() =
        filter { account -> account.matchesTagFilter() }

    private fun Account.matchesTagFilter() =
        options.filterByTag == null || metadata.tags.contains(options.filterByTag)

    private fun Account.label() = name + tagsIfDisplayable()

    private fun Account.tagsIfDisplayable() =
        TextColors.gray(" (${metadata.tags.joinToString(", ") { it.name }})")
            .takeIf { options.displayTags && metadata.tags.isNotEmpty() } ?: ""

    private fun TableBuilder.totalByRiskLevel() {
        body {
            row { cell("TOTAL BY RISK LEVEL") { columnSpan = Integer.MAX_VALUE } }
            book.riskLevels.forEach { riskLevel ->
                row {
                    borders = Borders.LEFT_RIGHT
                    cell("${riskLevel.symbol} ${riskLevel.name}") { columnSpan = 2 }
                    snapshots.forEach { snapshot ->
                        cell(
                            formatAmountAndPercentage(
                                snapshot.totalForRiskLevel(riskLevel),
                                snapshot.percentageForRiskLevel(riskLevel)
                            )
                        ) { align = TextAlign.RIGHT }
                    }
                }
            }
        }
    }

    private fun TableBuilder.footerRow() {
        footer {
            row {
                style(TextColors.yellow, bold = true)
                cell("TOTAL") { columnSpan = 2 }
                snapshots.map {
                    cell(formatAmount(it.total())) { align = TextAlign.RIGHT }
                }
            }
        }
    }

    fun displayAsTable() {
        Terminal().println(table)
    }

    // TODO balance might be displayed in original and main currency
    private fun formatBalance(balance: Balance?, date: LocalDate) = balance?.let { formatAmount(it.convertValueTo(book.mainCurrency, date)) } ?: "-"

    private fun formatAmount(amount: Double) = String.format("%,.0f", amount)

    private fun Snapshot.total() = balances.filter { it.account.matchesTagFilter() }.sumOf { it.convertValueTo(book.mainCurrency, date) }
    private fun Snapshot.totalForRiskLevel(riskLevel: RiskLevel) =
        balances.filter {
            it.account.metadata.riskLevel == riskLevel && it.account.matchesTagFilter()
        }.sumOf { it.convertValueTo(book.mainCurrency, date) }

    private fun Snapshot.percentageForRiskLevel(riskLevel: RiskLevel) = totalForRiskLevel(riskLevel) / total()

    private fun formatAmountAndPercentage(amount: Double, percentage: Double): String {
        return String.format("%,.0f (%2.0f%%)", amount, percentage * 100.0)
    }

    fun Balance.convertValueTo(currency: Currency, date: LocalDate) =
        if (account.currency == currency) value else value * 1.1111 // fake exchange rate, tbd
}