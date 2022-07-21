package com.lpi.budgy.repository.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = PropertyEntity::class),
    JsonSubTypes.Type(value = AccountEntity::class),
)
sealed class AssetEntity {
    abstract val id: String
    abstract val name: String
    abstract val currencyId: String
    abstract val metadata: AssetMetadataEntity
}

data class PropertyEntity(
    override val id: String,
    override val name: String,
    override val currencyId: String,
    override val metadata: AssetMetadataEntity
): AssetEntity()

data class AccountEntity(
    override val id: String,
    override val name: String,
    override val currencyId: String,
    override val metadata: AssetMetadataEntity,
    val institutionId: String,
): AssetEntity()

data class AssetMetadataEntity(
    val riskLevelId: String?,
    val tags: Set<String> = emptySet()
)