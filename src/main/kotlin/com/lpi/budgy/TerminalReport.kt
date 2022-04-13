package com.lpi.budgy

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.TableBuilder
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import java.time.format.DateTimeFormatter

data class TerminalReportOptions(val displayTotalsByRiskLevel: Boolean = false)

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
            book.institutions.map {
                row {
                    style(TextColors.brightYellow, bold = true)
                    cell(it.name) { columnSpan = 2 + snapshots.size }
                }
                book.accountsIn(it).map { account ->
                    row {
                        borders = Borders.LEFT_RIGHT
                        cell(account.metadata.riskLevel?.symbol ?: "")
                        cell(account.name)
                        snapshots.map { snapshot ->
                            cell(formatBalance(snapshot.accountBalance(account))) { align = TextAlign.RIGHT }
                        }
                    }
                }
            }
        }
    }

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

    private fun formatBalance(balance: Balance?) = balance?.let { formatAmount(it.value) } ?: "-"

    private fun formatAmount(amount: Double) = String.format("%,.0f", amount)

    private fun Snapshot.total() = balances.sumOf { it.value }
    private fun Snapshot.totalForRiskLevel(riskLevel: RiskLevel) =
        balances.filter { it.account.metadata.riskLevel == riskLevel }.sumOf { it.value }

    private fun Snapshot.percentageForRiskLevel(riskLevel: RiskLevel) = totalForRiskLevel(riskLevel) / total()

    private fun formatAmountAndPercentage(amount: Double, percentage: Double): String {
        return String.format("%,.0f (%2.0f%%)", amount, percentage * 100.0)
    }
}