package com.lpi.budgy

import com.lpi.budgy.domain.*


fun main(args: Array<String>) {

    // Currencies
    val usd = Currency("USD", "$")
    val eur = Currency("EUR", "\u20AC")
    val pln = Currency("PLN", "zł")
    val currencies = setOf(usd, eur, pln)

    // Risk levels
    val cash = RiskLevel("Cash", "\uD83D\uDFE2") // green circle
    val realEstate = RiskLevel("Real Estate", "\uD83D\uDFE1") // yellow circle
    val lowRisk = RiskLevel("Low Risk", "\uD83D\uDFE0") // orange circle
    val highRisk = RiskLevel("High Risk", "\uD83D\uDD34") // red circle
    val riskLevels = listOf(cash, realEstate, lowRisk, highRisk)

    //Tags
    val downPayment = Tag("Down Payment") // can be used for down payment
    val property = Tag("Property")

    val theBank = Institution("Some Bank")
    val checkingAccount = Account(theBank, "Checking", pln, AccountMetadata(cash))
    val savingsAccount = Account(theBank, "Savings", pln, AccountMetadata(lowRisk, setOf(downPayment)))
    val savingsEurAccount = Account(theBank, "Savings EUR", eur, AccountMetadata(cash))

    val stockBroker = Institution("Stock broker")
    val sharesAccount = Account(stockBroker, "Shares", usd, AccountMetadata(highRisk))
    val cryptoAccount = Account(stockBroker, "Crypto account", usd, AccountMetadata(highRisk))

    val houses = Institution("Houses")
    val cars = Institution("Cars")
    val home = Account(
        houses,
        "Home",
        pln,
        AccountMetadata(realEstate, setOf(property))
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
                sharesAccount.stocksBalance(mapOf("TSLA" to 10.0, "AAPL" to 100.0, "MSFT" to 20.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.1, "ETH" to 1.5)),
                home.balanceWithLoans(150_000, listOf(140_000)),
                car.balanceWithLoans(15_000, listOf(7_000))
            )
        ), Snapshot(
            date = "2022-04-01",
            balances = setOf(
                checkingAccount.monetaryBalance(600),
                savingsAccount.monetaryBalance(2500),
                sharesAccount.stocksBalance(mapOf("TSLA" to 10.0, "AAPL" to 100.0, "MSFT" to 30.0)),
                cryptoAccount.cryptosBalance(mapOf("BTC" to 0.12, "ETH" to 1.8)),
                savingsEurAccount.monetaryBalance(120),
            )
        )
    )

    Budgy(book, snapshots).main(args)
}