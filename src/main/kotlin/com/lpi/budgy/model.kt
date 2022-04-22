package com.lpi.budgy

import java.time.LocalDate

data class Currency(val id: String, val symbol: String)

data class RiskLevel(val name: String, val symbol: String)
data class Tag(val name: String)

data class AccountMetadata(
    val riskLevel: RiskLevel? = null,
    val tags: Set<Tag> = emptySet()
)

data class Book(
    val institutions: List<Institution>,
    val accounts: List<Account>,
    val riskLevels: List<RiskLevel>,
    val tags: List<Tag>,
    val currencies: Set<Currency>,
    val mainCurrency: Currency
    ) {
    fun accountsIn(institution: Institution): List<Account> = accounts.filter { it.institution == institution }
}

// I think it's not exactly equivalent of a wallet, rather something more formal
data class Institution(val name: String)
// I'd group my assets by wallets and keep institutions as some kind of metadata

data class Account(
    val institution: Institution,
    val name: String,
    val currency: Currency,
    val metadata: AccountMetadata = AccountMetadata()
) {
    fun balance(value: Double) = Balance(this, value)
    fun balance(value: Int) = Balance(this, value.toDouble())

    fun balanceWithLoans(value: Double, loans: List<Double>) = BalanceWithLoans(this, value, loans)
    fun balanceWithLoans(value: Int, loans: List<Int>) =
        BalanceWithLoans(this, value.toDouble(), loans.map { it.toDouble() })
    // XD - and what about value as Int and loans as Doubles? It doesn't scale up well
}

// this "open" stuff is to be refactored
open class Balance(open val account: Account, open val value: Double)

// meant for "house with mortgage(s)" but I don't really like it
// house and mortgage should be two separate accounts, maybe connected on a higher level of abstraction such as
// Wallet - set of accounts
class BalanceWithLoans(account: Account, value: Double, loans: List<Double>) : Balance(account, value - loans.sum())

class Snapshot(val date: LocalDate, val balances: Set<Balance>) {
    constructor(date: String, balances: Set<Balance>) : this(LocalDate.parse(date), balances)

    fun accountBalance(account: Account): Balance? = balances.find { it.account == account }

}