package com.lpi.budgy.report

import com.lpi.budgy.domain.Book
import com.lpi.budgy.domain.Snapshot
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class WebReport(
    val book: Book,
    val snapshots: List<Snapshot>
) {

    fun display() {
        embeddedServer(Netty, host = "localhost", port = 2207) {
            routing {
                get("/") {
                    call.respondText("Hello, Budgy!")
                }
            }
        }.start(wait = true)
    }

}