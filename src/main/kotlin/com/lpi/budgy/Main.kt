package com.lpi.budgy


fun main(args: Array<String>) {
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
    val checkingAccount = Account(theBank,"Checking", AccountMetadata(cash))
    val savingsAccount = Account(theBank, "Savings", AccountMetadata(lowRisk, setOf(downPayment)))


    val houses = Institution("Houses")
    val cars = Institution("Cars")
    val home = Account(houses, "Home", AccountMetadata(realEstate, setOf(property))) // but home belongs to me and loan is provided by separate institution!!
    val car = Account(cars, "Car")

    val book = Book(listOf(theBank, houses, cars), listOf(checkingAccount, savingsAccount, home, car), riskLevels, listOf(downPayment, property))


    val snapshots = listOf(
        Snapshot(
            date = "2022-01-01",
            balances = setOf(
                checkingAccount.balance(500),
                savingsAccount.balance(2000),
                home.balanceWithLoans(150_000, listOf(140_000)),
                car.balanceWithLoans(15_000, listOf(7_000))
            )
        ), Snapshot(
            date = "2022-02-01",
            balances = setOf(
                checkingAccount.balance(600), savingsAccount.balance( 2500)
            )
        )
    )

    Budgy(book, snapshots).main(args)
}