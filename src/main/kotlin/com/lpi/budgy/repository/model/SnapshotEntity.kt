package com.lpi.budgy.repository.model

import java.time.LocalDate

data class SnapshotEntity(
    val id: String,
    val date: LocalDate,
    val balances: Set<BalanceEntity>
)