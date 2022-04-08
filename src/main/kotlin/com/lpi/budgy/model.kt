package com.lpi.budgy

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Book(val institutions: List<Institution>, val accounts: List<Account>) {
    fun accountsIn(institution: Institution): List<Account> = accounts.filter { it.institution == institution }
}

// I think it's not exactly equivalent of a wallet, rather something more formal
class Institution(val name: String)

// I'd group my assets by wallets and keep institutions as some kind of metadata

class Account(val institution: Institution, val name: String) {
    fun balance(value: Double) = Balance(this, value)
    fun balance(value: Int) = Balance(this, value.toDouble())

    fun balanceWithLoans(value: Double, loans: List<Double>) = BalanceWithLoans(this, value, loans)
    fun balanceWithLoans(value: Int, loans: List<Int>) =
        BalanceWithLoans(this, value.toDouble(), loans.map { it.toDouble() })
    // XD - and what about value as Int and loans as Doubles? It doesn't scale up well
}

open class Balance(val account: Account, val value: Double)

// meant for "house with mortgage(s)" but I don't really like it
// house and mortgage should be two separate accounts, maybe connected on a higher level of abstraction such as
// Wallet - set of accounts
class BalanceWithLoans(account: Account, value: Double, loans: List<Double>) : Balance(account, value - loans.sum())

class Snapshot(val date: LocalDate, val balances: Set<Balance>) {
    constructor(date: String, balances: Set<Balance>) : this(LocalDate.parse(date), balances)

    fun accountBalance(account: Account): Balance? = balances.find { it.account == account }

}

class TerminalReport(private val book: Book, private val snapshots: List<Snapshot>) {
    private val dateFormat = DateTimeFormatter.ofPattern("MMM dd, u")

    val table = table {
        column(0) { width = ColumnWidth.Fixed(2) }
        borderStyle = BorderStyle.SQUARE_DOUBLE_SECTION_SEPARATOR
        header {
            row {
                cell("") { columnSpan = 2 }
                snapshots.map {
                    cell(it.date.format(dateFormat))
                }
            }
        }

        body {
            book.institutions.map {
                row {
                    style(TextColors.brightYellow, bold = true)
                    cell(it.name) { columnSpan = 2 + snapshots.size }
                }
                book.accountsIn(it).map { account ->
                    row {
                        borders = Borders.LEFT_RIGHT
                        cell("")
                        cell(account.name)
                        snapshots.map { snapshot ->
                            cell(formatBalance(snapshot.accountBalance(account))) { align = TextAlign.RIGHT }
                        }
                    }
                }
            }
        }

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
}