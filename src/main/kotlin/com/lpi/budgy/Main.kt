package com.lpi.budgy

import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.AssetRepository
import com.lpi.budgy.repository.CurrencyRepository
import com.lpi.budgy.repository.InstitutionRepository
import com.lpi.budgy.repository.RiskLevelRepository

fun main(args: Array<String>) {

    // Currencies
    val currencyRepository = CurrencyRepository()

    //Tags
    val downPayment = Tag("Down Payment") // can be used for down payment
    val property = Tag("Property")


    val institutionRepository = InstitutionRepository()
    val assetRepository = AssetRepository(currencyRepository, institutionRepository, RiskLevelRepository())
    val checkingAccount = assetRepository.find("acc-checking") as Account
    val savingsAccount = assetRepository.find("acc-savings") as Account
    val savingsEurAccount = assetRepository.find("acc-savings-eur") as Account

    val sharesAccount = assetRepository.find("acc-shares") as Account
    val cryptoAccount = assetRepository.find("acc-cryptos") as Account

    val home = assetRepository.find("prop-house") as Property
    val mortgageLoan = assetRepository.find("acc-mortgage") as Account
    val car = assetRepository.find("prop-car") as Property
    val carLoan = assetRepository.find("acc-car-loan") as Account

    val book = Book(
        institutions = institutionRepository.getAll(),
        assets = assetRepository.getAll(),
        riskLevels = RiskLevelRepository().getAll(),
        tags = listOf(downPayment, property),
        currencies = currencyRepository.getAll(),
        mainCurrency = currencyRepository.find("PLN")
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
                home.balance(150_000),
                mortgageLoan.monetaryBalance(-140_000),
                car.balance(15_000),
                carLoan.monetaryBalance(-7_000),
            )
        ), Snapshot(
            date = "2022-04-01",
            balances = setOf(
                checkingAccount.monetaryBalance(600),
                savingsAccount.monetaryBalance(2500),
                sharesAccount.stocksBalance(mapOf("TSLA" to 10.0, "AAPL" to 12.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.12, "ETH" to 1.8)),
                savingsEurAccount.monetaryBalance(120),
                home.balance(150_000),
                mortgageLoan.monetaryBalance(-138_500),
                car.balance(15_000),
                carLoan.monetaryBalance(-6_000),
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
                home.balance(150_000),
                mortgageLoan.monetaryBalance(-137_000),
                car.balance(15_000),
                carLoan.monetaryBalance(-5_000),
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
                home.balance(150_000),
                mortgageLoan.monetaryBalance(-135_500),
                car.balance(15_000),
                carLoan.monetaryBalance(-4_000),
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
                home.balance(150_000),
                mortgageLoan.monetaryBalance(-134_000),
                car.balance(15_000),
                carLoan.monetaryBalance(-3_000),
            )
        )
    )

    Budgy(book, snapshots).main(args)
}