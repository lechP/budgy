package com.lpi.budgy

import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.*

fun main(args: Array<String>) {

    // Currencies
    val currencyRepository = CurrencyRepository()

    //Tags
    val downPayment = Tag("Down Payment") // can be used for down payment
    val property = Tag("Property")


    val institutionRepository = InstitutionRepository()
    val assetRepository = AssetRepository(currencyRepository, institutionRepository, RiskLevelRepository())

    // TODO sort set/list inconsistencies
    val book = Book(
        institutions = institutionRepository.getAll().toSet(),
        assets = assetRepository.getAll(),
        riskLevels = RiskLevelRepository().getAll().toSet(),
        tags = listOf(downPayment, property),
        currencies = currencyRepository.getAll().toSet(),
        mainCurrency = currencyRepository.find("PLN")
    )

    /* TODO
    5a. remove stockApi from StocksBalance - use some service instead
    5b. remove currencyConverter from MonetaryBalance - use some service instead
    7. use KTOR client instead of plain java.net
     */

    Budgy(book).main(args)
}