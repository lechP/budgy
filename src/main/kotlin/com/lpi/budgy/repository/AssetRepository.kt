package com.lpi.budgy.repository

import com.lpi.budgy.domain.*
import com.lpi.budgy.repository.model.AccountEntity
import com.lpi.budgy.repository.model.AssetEntity
import com.lpi.budgy.repository.model.AssetMetadataEntity
import com.lpi.budgy.repository.model.PropertyEntity

class AssetRepository(
    private val currencyRepository: CurrencyRepository,
    private val institutionRepository: InstitutionRepository,
    private val riskLevelRepository: RiskLevelRepository
) : FileRepository() {

    private val data: Set<AssetEntity> by lazy {
        readDataFromJson("assets")
    }

    fun find(id: String) = data.find { it.id == id }?.toDomain() ?: throw AssetNotFound(id)

    fun getAll(): Set<Asset> = data.map { it.toDomain() }.toSet()

    private fun AssetEntity.toDomain(): Asset = when (this) {
        is AccountEntity -> Account(
            id = id,
            name = name,
            currency = currencyRepository.find(currencyId),
            institution = institutionRepository.find(institutionId),
            metadata = metadata.toDomain()
        )
        is PropertyEntity -> Property(
            id = id,
            name = name,
            currency = currencyRepository.find(currencyId),
            metadata = metadata.toDomain()
        )
    }

    fun AssetMetadataEntity.toDomain() = AssetMetadata(
        riskLevel = riskLevelId?.let { riskLevelRepository.find(it) },
        tags = tags.map { Tag(it) }.toSet()
    )

}