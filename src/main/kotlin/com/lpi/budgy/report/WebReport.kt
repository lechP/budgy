package com.lpi.budgy.report

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.lpi.budgy.domain.Book
import com.lpi.budgy.domain.Snapshot
import com.lpi.budgy.repository.SnapshotRepository
import com.lpi.budgy.valuation.ValuationService
import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.freemarker.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class WebReport(
    private val book: Book,
    snapshotRepository: SnapshotRepository,
    private val valuationService: ValuationService,
) {

    private val snapshots = snapshotRepository.getAll()
    private fun snapshotTotals(): Map<String, Double> = snapshots.associate {
        it.date.toString() to snapshotTotal(it)
    }

    private fun snapshotTotal(snapshot: Snapshot) =
        snapshot.balances.sumOf { balance -> valuationService.value(balance, book.mainCurrency, snapshot.date) }

    private fun chartDataByAsset(): List<List<*>> {
        val headers = listOf("Date") + book.assets.map { it.name } + listOf("Total")
        val rows = snapshots.map { snapshot ->
            val assetRows = book.assets.map {
                snapshot.assetBalance(it)?.let { balance ->
                    valuationService.value(balance, book.mainCurrency, snapshot.date)
                }
            }
            listOf(snapshot.date.toString() + "T00:00:00") +
                    assetRows + listOf(snapshotTotal(snapshot))
        }
        return listOf(headers) + rows
    }

    fun display() {
        embeddedServer(Netty, host = "localhost", port = 2137) {
            install(FreeMarker) {
                templateLoader = ClassTemplateLoader(
                    this::class.java.classLoader, "templates"
                )
                outputFormat = HTMLOutputFormat.INSTANCE
            }

            routing {
                get("/") {
                    call.respond(
                        FreeMarkerContent(
                            "report.ftl", mapOf(
                                "title" to "Budgy",
                                "book" to book,
                                "snapshots" to snapshots,
                                "snapshotTotals" to snapshotTotals(),
                                "valuationService" to valuationService,
                                "chartDataByAccountJson" to jsonMapper().writeValueAsString(chartDataByAsset())
                            )
                        )
                    )
                }
            }
        }.start(wait = true)
    }

}