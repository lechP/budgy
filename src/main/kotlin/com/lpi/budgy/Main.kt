package com.lpi.budgy


fun main() {
    val theBank = Institution("Some Kind of Bank")
    val checkingAccount = Account(theBank,"Checking")
    val savingsAccount = Account(theBank, "Savings")


    val houses = Institution("Houses")
    val cars = Institution("Cars")
    val home = Account(houses, "Home") // but home belongs to me and loan is provided by separate institution!!
    val car = Account(cars, "Car")

    val book = Book(listOf(theBank, houses, cars), listOf(checkingAccount, savingsAccount, home, car))


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

    TerminalReport(book, snapshots).display()
}