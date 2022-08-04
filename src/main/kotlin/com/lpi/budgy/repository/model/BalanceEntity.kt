package com.lpi.budgy.repository.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MonetaryBalanceEntity::class),
    JsonSubTypes.Type(value = StocksBalanceEntity::class),
)
sealed class BalanceEntity {
    abstract val id: String
    abstract val assetId: String
}

data class MonetaryBalanceEntity(
    override val id: String,
    override val assetId: String,
    val value: Double,
): BalanceEntity()

data class StocksBalanceEntity(
    override val id: String,
    override val assetId: String,
    val stocksAmounts: Map<String, Double>,
    val isCrypto: Boolean
): BalanceEntity()
