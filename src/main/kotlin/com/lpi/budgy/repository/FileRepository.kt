package com.lpi.budgy.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

open class FileRepository {

    inline fun <reified T> readDataFromJson(datasetName: String): Set<T> {
        val dataInJson = object{}.javaClass.getResource("/data/$datasetName.json")?.readText()
        return jacksonObjectMapper().readValue(dataInJson, jacksonObjectMapper().typeFactory.constructCollectionType(Set::class.java, T::class.java))
    }

}