package com.lpi.budgy.repository

import com.lpi.budgy.domain.RiskLevel
import com.lpi.budgy.domain.RiskLevelNotFound

class RiskLevelRepository: FileRepository() {

    private val data: Set<RiskLevel> by lazy {
        readDataFromJson("riskLevels")
    }

    fun find(id: String): RiskLevel = data.find { it.id == id } ?: throw RiskLevelNotFound(id)

    fun getAll() = data

}