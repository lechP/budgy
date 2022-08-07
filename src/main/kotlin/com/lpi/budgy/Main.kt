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
    7. use KTOR client instead of plain java.net
    8. add offline mode for CurrencyConverter and StockApi
    9. use same convention for CurrencyConverter and StockApi (example: CurrencyClient and StockClient)
     */

    Budgy(book).main(args)
}