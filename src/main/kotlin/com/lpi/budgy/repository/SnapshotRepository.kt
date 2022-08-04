package com.lpi.budgy.repository

import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.model.*

class SnapshotRepository(
    private val assetRepository: AssetRepository,
) : FileRepository() {

    private val data: List<SnapshotEntity> by lazy {
        readDataFromJson("snapshots")
    }

    fun find(id: String) = data.find { it.id == id }?.toDomain() ?: throw SnapshotNotFound(id)

    fun getAll(): List<Snapshot> = data.map { it.toDomain() }.sortedBy { it.date }

    private fun SnapshotEntity.toDomain(): Snapshot =
        Snapshot(
            date = date,
            balances = balances.map {it.toDomain() }.toSet()
        )

    private fun BalanceEntity.toDomain(): Balance =
        when (this) {
        is MonetaryBalanceEntity -> MonetaryBalance(
            asset = assetRepository.find(assetId),
            value = value
        )
        is StocksBalanceEntity -> StocksBalance(
            asset = assetRepository.find(assetId),
            stocksAmounts = stocksAmounts,
            isCrypto = isCrypto
        )
    }
}


fun main() {
    // TODO replace with integration tests of repos
    val assetRepository = AssetRepository(CurrencyRepository(), InstitutionRepository(), RiskLevelRepository())
    val repo = SnapshotRepository(assetRepository)

    println(repo.getAll())
}