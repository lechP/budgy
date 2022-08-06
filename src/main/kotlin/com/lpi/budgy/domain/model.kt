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
    val assets: Set<Asset>,
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
    abstract val id: String
    abstract val name: String
    abstract val currency: Currency
    abstract val metadata: AssetMetadata
}

data class Property(
    override val name: String,
    override val currency: Currency,
    override val metadata: AssetMetadata = AssetMetadata(),
    override val id: String = name,
): Asset() {
    fun balance(value: Int) = MonetaryBalance(this, value.toDouble())
}

data class Account(
    val institution: Institution,
    override val name: String,
    override val currency: Currency, // TODO Currency doesnt make sense for StocksBalances where each stock may have its own currency
    override val metadata: AssetMetadata = AssetMetadata(),
    override val id: String = name, // TODO make mandatory
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

sealed class Balance(open val asset: Asset) {
    //always called with book.mainCurrency and snapshot.date
    abstract fun toValue(currency: Currency, date: LocalDate): Double
}

// asset isn't really needed, only currency.id
data class MonetaryBalance(override val asset: Asset, val value: Double) : Balance(asset) {
    private val currencyConverter: CurrencyConverter by DI.global.instance()

    override fun toValue(currency: Currency, date: LocalDate): Double =
        currencyConverter.convert(value, asset.currency.id, currency.id, date)

}

// hmm... InvestmentBalance? What about investment funds, ETFs etc?
// TODO split into SharesBalance, CryptoBalance and maybe InvestmentBalance
// but consider a case when you have stocks and cryptos within single balance (revolut for example)
// stocksAmounts should be modelled with more details

data class StocksBalance(
    override val asset: Asset,
    val stocksAmounts: Map<String, Double>,
    val isCrypto: Boolean // OMG
) : Balance(asset) {
    private val stockApi: StockApi by DI.global.instance()

    override fun toValue(currency: Currency, date: LocalDate): Double =
        stocksAmounts.map { (symbol, amount) -> stockApi.value(symbol, currency, date) * amount }.sum()

}


data class Snapshot(val date: LocalDate, val balances: Set<Balance>) {
    fun assetBalance(asset: Asset): Balance? = balances.find { it.asset == asset }

}