package com.lpi.budgy.report

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.*
import com.github.ajalt.mordant.terminal.Terminal
import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.SnapshotRepository
import com.lpi.budgy.valuation.ValuationService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TerminalReportOptions(
    val displayTotalsByRiskLevel: Boolean = false,
    val displayTags: Boolean = false,
    val filterByTag: Tag? = null,
)

class TerminalReport(
    private val book: Book,
    snapshotRepository: SnapshotRepository,
    private val valuationService: ValuationService,
    private val options: TerminalReportOptions = TerminalReportOptions()
) {

    private val snapshots = snapshotRepository.getAll()

    private val dateFormat = DateTimeFormatter.ofPattern("MMM dd, u")

    private val table = table {
        column(0) { width = ColumnWidth.Fixed(4) }
        column(1) { width = ColumnWidth.Fixed(book.assets.map { it.label().length }.max() + 2) }
        borderStyle = BorderStyle.SQUARE_DOUBLE_SECTION_SEPARATOR
        headerRow()
        assetsRows()
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

    private fun TableBuilder.assetsRows() {
        body {
            book.institutions.filter { book.accountsIn(it).applyTagFilter().isNotEmpty() }.map {
                assetGroupRows(it.name, book.accountsIn(it))
            }
            assetGroupRows("Property", book.properties())
        }
    }

    private fun SectionBuilder.assetGroupRows(groupName: String, assets: List<Asset>) {
        row {
            style(TextColors.brightYellow, bold = true)
            cell(groupName) { columnSpan = 2 + snapshots.size }
        }
        assets.applyTagFilter().map { account ->
            row {
                borders = Borders.LEFT_RIGHT
                cell(account.metadata.riskLevel?.symbol ?: "")
                cell(account.label())
                snapshots.map { snapshot ->
                    cell(formatBalance(snapshot.assetBalance(account), snapshot.date)) {
                        align = TextAlign.RIGHT
                    }
                }
            }
        }
    }

    private fun List<Asset>.applyTagFilter() = filter { it.matchesTagFilter() }

    private fun Asset.matchesTagFilter() =
        options.filterByTag == null || metadata.tags.contains(options.filterByTag)

    private fun Asset.label() = name + tagsIfDisplayable()

    private fun Asset.tagsIfDisplayable() =
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

    // balance might be displayed in original and main currency
    private fun formatBalance(balance: Balance?, date: LocalDate) =
        balance?.let {
            formatAmount(
                valuationService.value(it, book.mainCurrency, date)
            )
        } ?: "-"

    private fun formatAmount(amount: Double) = String.format("%,.0f", amount)

    private fun Snapshot.total() =
        balances.filter { it.asset.matchesTagFilter() }.sumOf { valuationService.value(it, book.mainCurrency, date) }

    private fun Snapshot.totalForRiskLevel(riskLevel: RiskLevel) =
        balances.filter {
            it.asset.metadata.riskLevel == riskLevel && it.asset.matchesTagFilter()
        }.sumOf { valuationService.value(it, book.mainCurrency, date) }

    private fun Snapshot.percentageForRiskLevel(riskLevel: RiskLevel) = totalForRiskLevel(riskLevel) / total()

    private fun formatAmountAndPercentage(amount: Double, percentage: Double): String {
        return String.format("%,.0f (%2.0f%%)", amount, percentage * 100.0)
    }

}