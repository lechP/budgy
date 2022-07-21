package com.lpi.budgy.domain

import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.stock.StockApi
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import java.time.LocalDate

data class Currency(val id: String, val symbol: String)

data class RiskLevel(val name: String, val symbol: String, val id: String)
data class Tag(val name: String)

data class AssetMetadata(
    val riskLevel: RiskLevel? = null,
    val tags: Set<Tag> = emptySet()
)

data class Book(
    val institutions: Set<Institution>,
    val assets: List<Asset>,
    val riskLevels: Set<RiskLevel>,
    val tags: List<Tag>,
    val currencies: Set<Currency>,
    val mainCurrency: Currency
) {
    fun accountsIn(institution: Institution): List<Account> = assets.filterIsInstance<Account>().filter { it.institution == institution }
    fun properties(): List<Property> = assets.filterIsInstance<Property>()
}

data class Institution(
    val id: String,
    val name: String
    )
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
): Asset() {
    fun balance(value: Int) = MonetaryBalance(this, value.toDouble())
}

data class Account(
    val institution: Institution,
    override val name: String,
    override val currency: Currency,
    override val metadata: AssetMetadata = AssetMetadata()
): Asset() {
    fun monetaryBalance(value: Double) = MonetaryBalance(this, value)
    fun monetaryBalance(value: Int) = MonetaryBalance(this, value.toDouble())

    fun stocksBalance(stockAmounts: Map<String, Double>): StocksBalance {
        return StocksBalance(this, stockAmounts, false)
    }
    fun cryptosBalance(cryptoAmounts: Map<String, Double>): StocksBalance {
        return StocksBalance(this, cryptoAmounts, true)
    }
}

abstract class Balance(open val asset: Asset) {
    abstract fun toValue(currency: Currency, date: LocalDate): Double
}

// this "open" stuff is to be refactored
// asset isn't really needed, only currency.id
open class MonetaryBalance(override val asset: Asset, open val value: Double) : Balance(asset) {
    private val currencyConverter: CurrencyConverter by DI.global.instance()

    override fun toValue(currency: Currency, date: LocalDate): Double =
        currencyConverter.convert(value, asset.currency.id, currency.id, date)

}

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

    fun assetBalance(asset: Asset): Balance? = balances.find { it.asset == asset }

}