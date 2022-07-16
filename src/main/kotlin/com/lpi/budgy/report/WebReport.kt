package com.lpi.budgy.report

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.lpi.budgy.domain.Book
import com.lpi.budgy.domain.Snapshot
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
    private val snapshots: List<Snapshot>
) {

    private fun snapshotTotals(): Map<String, Double> = snapshots.associate {
        it.date.toString() to snapshotTotal(it)
    }

    private fun snapshotTotal(snapshot: Snapshot) =
        snapshot.balances.sumOf { balance -> balance.toValue(book.mainCurrency, snapshot.date) }

    private fun chartDataByAccount(): List<List<*>> {
        val accounts = book.institutions.flatMap { book.accountsIn(it) }
        val headers = listOf("Date") + accounts.map { it.name } + listOf("Total")
        val rows = snapshots.map { snapshot ->
            val accountRows = accounts.map {
                snapshot.accountBalance(it)?.toValue(
                    book.mainCurrency, snapshot.date
                )
            }
            listOf(snapshot.date.toString() + "T00:00:00") +
                    accountRows + listOf(snapshotTotal(snapshot))
        }
        return listOf(headers) + rows
    }

    fun display() {
        embeddedServer(Netty, host = "localhost", port = 2207) {
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
                                "chartDataByAccountJson" to jsonMapper().writeValueAsString(chartDataByAccount())
                            )
                        )
                    )
                }
            }
        }.start(wait = true)
    }

}