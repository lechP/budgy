package com.lpi.budgy.repository

import com.lpi.budgy.domain.RiskLevel

class RiskLevelRepository: FileRepository() {

    private val data: Set<RiskLevel> by lazy {
        readDataFromJson("riskLevels")
    }

    fun find(id: String) = data.find { it.id == id }

    fun getAll() = data

}