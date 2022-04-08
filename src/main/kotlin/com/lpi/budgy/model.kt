package com.lpi.budgy

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

class TerminalReport(val book: Book, val snapshots: List<Snapshot>) {
    fun display() {
        val dateFormat = DateTimeFormatter.ofPattern("MMM dd, u")
        snapshots.forEach { snapshot ->
            println(snapshot.date.format(dateFormat))
            book.institutions.forEach { institution ->
                println("* ${institution.name}")
                book.accountsIn(institution).forEach { account ->
                    println("   - ${account.name}: ${formatBalance(snapshot.accountBalance(account))}")
                }
            }
            println("TOTAL: ${formatAmount(snapshot.total())}\n")
        }
    }

    private fun formatBalance(balance: Balance?) = balance?.let { formatAmount(it.value) } ?: "-"

    private fun formatAmount(amount: Double) = String.format("%,.0f", amount)

    private fun Snapshot.total() = balances.sumOf { it.value }
}