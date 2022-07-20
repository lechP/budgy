package com.lpi.budgy.domain

import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.stock.StockApi
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import java.time.LocalDate

data class Currency(val id: String, val symbol: String)

data class RiskLevel(val name: String, val symbol: String, val id: String = name)
data class Tag(val name: String)

data class AssetMetadata(
    val riskLevel: RiskLevel? = null,
    val tags: Set<Tag> = emptySet()
)

data class Book(
    val institutions: List<Institution>,
    val accounts: List<Account>,
    val riskLevels: Set<RiskLevel>,
    val tags: List<Tag>,
    val currencies: Set<Currency>,
    val mainCurrency: Currency
) {
    fun accountsIn(institution: Institution): List<Account> = accounts.filter { it.institution == institution }
}

// I think it's not exactly equivalent of a wallet, rather something more formal
data class Institution(val name: String)
// I'd group my assets by wallets and keep institutions as some kind of metadata

sealed class Asset {
    abstract val name: String
    abstract val currency: Currency
    abstract val metadata: AssetMetadata
}

data class Property(
    override val name: String,
    override val currency: Currency,
    override val metadata: AssetMetadata = AssetMetadata()
): Asset()

data class Account(
    val institution: Institution,
    override val name: String,
    override val currency: Currency,
    override val metadata: AssetMetadata = AssetMetadata()
): Asset() {
    fun monetaryBalance(value: Double) = MonetaryBalance(this, value)
    fun monetaryBalance(value: Int) = MonetaryBalance(this, value.toDouble())

    fun balanceWithLoans(value: Double, loans: List<Double>) = MonetaryBalanceWithLoans(this, value, loans)
    fun balanceWithLoans(value: Int, loans: List<Int>) =
        MonetaryBalanceWithLoans(this, value.toDouble(), loans.map { it.toDouble() })
    // XD - and what about value as Int and loans as Doubles? It doesn't scale up well

    fun stocksBalance(stockAmounts: Map<String, Double>): StocksBalance {
        return StocksBalance(this, stockAmounts, false)
    }
    fun cryptosBalance(cryptoAmounts: Map<String, Double>): StocksBalance {
        return StocksBalance(this, cryptoAmounts, true)
    }
}

abstract class Balance(open val account: Account) {
    abstract fun toValue(currency: Currency, date: LocalDate): Double
}

// this "open" stuff is to be refactored
open class MonetaryBalance(override val account: Account, open val value: Double) : Balance(account) {
    private val currencyConverter: CurrencyConverter by DI.global.instance()

    override fun toValue(currency: Currency, date: LocalDate): Double =
        currencyConverter.convert(value, account.currency.id, currency.id, date)

}

// meant for "house with mortgage(s)" but I don't really like it
// house and mortgage should be two separate accounts, maybe connected on a higher level of abstraction such as
// Wallet - set of accounts
class MonetaryBalanceWithLoans(account: Account, value: Double, loans: List<Double>) :
    MonetaryBalance(account, value - loans.sum())

// hmm... InvestmentBalance? What about investment funds, ETFs etc?
class StocksBalance(
    account: Account,
    val stocksAmounts: Map<String, Double>,
    val isCrypto: Boolean // OMG
) : Balance(account) {
    private val stockApi: StockApi by DI.global.instance()

    override fun toValue(currency: Currency, date: LocalDate): Double =
        stocksAmounts.map { (symbol, amount) -> stockApi.value(symbol, currency, date) * amount }.sum()

}


class Snapshot(val date: LocalDate, val balances: Set<Balance>) {
    constructor(date: String, balances: Set<Balance>) : this(LocalDate.parse(date), balances)

    fun accountBalance(account: Account): Balance? = balances.find { it.account == account }

}