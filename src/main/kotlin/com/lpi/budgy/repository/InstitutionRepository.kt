package com.lpi.budgy.repository

import com.lpi.budgy.domain.Institution
import com.lpi.budgy.domain.InstitutionNotFound

class InstitutionRepository: FileRepository() {

    private val data: Set<Institution> by lazy {
        readDataFromJson("institutions")
    }

    fun find(id: String): Institution = data.find { it.id == id } ?: throw InstitutionNotFound(id)

    fun getAll() = data

}