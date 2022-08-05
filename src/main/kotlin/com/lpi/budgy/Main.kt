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
    4. upgrade dependency injection reduce main to Budgy(book).main(args)
    5. remove stockApi from StocksBalance - use some service instead
    6. get rid of DI.global.instance
     */

    val snapshotRepository = SnapshotRepository(assetRepository)
    val snapshots = snapshotRepository.getAll()

    Budgy(book, snapshots).main(args)
}