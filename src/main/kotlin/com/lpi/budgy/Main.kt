package com.lpi.budgy


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
        institutions = listOf(theBank, houses, cars),
        accounts = listOf(checkingAccount, savingsAccount, savingsEurAccount, home, car),
        riskLevels = riskLevels,
        tags = listOf(downPayment, property),
        currencies = currencies,
        mainCurrency = pln
    )

    val snapshots = listOf(
        Snapshot(
            date = "2022-01-01",
            balances = setOf(
                checkingAccount.balance(500),
                savingsAccount.balance(2000),
                savingsEurAccount.balance(100),
                home.balanceWithLoans(150_000, listOf(140_000)),
                car.balanceWithLoans(15_000, listOf(7_000))
            )
        ), Snapshot(
            date = "2022-02-01",
            balances = setOf(
                checkingAccount.balance(600),
                savingsAccount.balance(2500),
                savingsEurAccount.balance(120),
            )
        )
    )

    Budgy(book, snapshots).main(args)
}