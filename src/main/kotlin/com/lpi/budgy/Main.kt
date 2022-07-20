package com.lpi.budgy

import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.CurrencyRepository
import com.lpi.budgy.repository.RiskLevelRepository

fun Set<Currency>.find(id: String) = first { it.id == id }
fun Set<RiskLevel>.find(id: String) = first { it.id == id }
fun main(args: Array<String>) {

    // Currencies
    val currencies = CurrencyRepository().getAll()
    val usd = currencies.find("USD")
    val eur = currencies.find("EUR")
    val pln = currencies.find("PLN")

    // Risk levels
    val riskLevels = RiskLevelRepository().getAll()
    val cash = riskLevels.find("cash")
    val realEstate = riskLevels.find("real_estate")
    val lowRisk = riskLevels.find("low_risk")
    val highRisk = riskLevels.find("high_risk")

    //Tags
    val downPayment = Tag("Down Payment") // can be used for down payment
    val property = Tag("Property")

    val theBank = Institution("Some Bank")
    val checkingAccount = Account(theBank, "Checking", pln, AssetMetadata(cash))
    val savingsAccount = Account(theBank, "Savings", pln, AssetMetadata(lowRisk, setOf(downPayment)))
    val savingsEurAccount = Account(theBank, "Savings EUR", eur, AssetMetadata(cash))

    val stockBroker = Institution("Stock broker")
    val sharesAccount = Account(stockBroker, "Shares", usd, AssetMetadata(highRisk))
    val cryptoAccount = Account(stockBroker, "Crypto account", usd, AssetMetadata(highRisk))

    val houses = Institution("Houses")
    val cars = Institution("Cars")
    // TODO split home and car into Property & Account with negative value
    val home = Account(
        houses,
        "Home",
        pln,
        AssetMetadata(realEstate, setOf(property))
    ) // but home belongs to me and loan is provided by separate institution!!
    val car = Account(cars, "Car", pln)

    val book = Book(
        institutions = listOf(theBank, houses, cars, stockBroker),
        accounts = listOf(checkingAccount, savingsAccount, savingsEurAccount, sharesAccount, cryptoAccount, home, car),
        riskLevels = riskLevels,
        tags = listOf(downPayment, property),
        currencies = currencies,
        mainCurrency = pln
    )

    val snapshots = listOf(
        Snapshot(
            date = "2022-03-01",
            balances = setOf(
                checkingAccount.monetaryBalance(500),
                savingsAccount.monetaryBalance(2000),
                savingsEurAccount.monetaryBalance(100),
                sharesAccount.stocksBalance(mapOf("TSLA" to 10.0, "AAPL" to 11.0, "MSFT" to 20.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.1, "ETH" to 1.5)),
                home.balanceWithLoans(150_000, listOf(140_000)),
                car.balanceWithLoans(15_000, listOf(7_000))
            )
        ), Snapshot(
            date = "2022-04-01",
            balances = setOf(
                checkingAccount.monetaryBalance(600),
                savingsAccount.monetaryBalance(2500),
                sharesAccount.stocksBalance(mapOf("TSLA" to 10.0, "AAPL" to 12.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.12, "ETH" to 1.8)),
                savingsEurAccount.monetaryBalance(120),
                home.balanceWithLoans(150_000, listOf(138_500)),
                car.balanceWithLoans(15_000, listOf(6_000))
            )
        ),
        Snapshot(
            date = "2022-05-01",
            balances = setOf(
                checkingAccount.monetaryBalance(1500),
                savingsAccount.monetaryBalance(2000),
                savingsEurAccount.monetaryBalance(100),
                sharesAccount.stocksBalance(mapOf("TSLA" to 12.0, "AAPL" to 5.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.15, "ETH" to 5.0)),
                home.balanceWithLoans(150_000, listOf(137_000)),
                car.balanceWithLoans(15_000, listOf(5_000))
            )
        ),
        Snapshot(
            date = "2022-06-01",
            balances = setOf(
                checkingAccount.monetaryBalance(900),
                savingsAccount.monetaryBalance(2000),
                savingsEurAccount.monetaryBalance(200),
                sharesAccount.stocksBalance(mapOf("TSLA" to 13.0, "AAPL" to 5.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.17, "ETH" to 5.0)),
                home.balanceWithLoans(150_000, listOf(135_500)),
                car.balanceWithLoans(15_000, listOf(4_000))
            )
        ),
        Snapshot(
            date = "2022-07-01",
            balances = setOf(
                checkingAccount.monetaryBalance(1100),
                savingsAccount.monetaryBalance(2000),
                savingsEurAccount.monetaryBalance(250),
                sharesAccount.stocksBalance(mapOf("TSLA" to 14.0, "AAPL" to 5.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.18, "ETH" to 5.0)),
                home.balanceWithLoans(150_000, listOf(134_000)),
                car.balanceWithLoans(15_000, listOf(3_000))
            )
        )
    )

    Budgy(book, snapshots).main(args)
}