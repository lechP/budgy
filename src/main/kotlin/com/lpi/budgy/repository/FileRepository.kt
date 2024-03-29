package com.lpi.budgy.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

open class FileRepository {

    inline fun <reified T> readDataFromJson(datasetName: String): List<T> {
        val dataInJson = object{}.javaClass.getResource("/data/$datasetName.json")?.readText()
        return jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .readValue(dataInJson, jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, T::class.java))
    }

}