package com.lpi.budgy.report

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
    val book: Book,
    val snapshots: List<Snapshot>
) {

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
                    call.respond(FreeMarkerContent(
                        "report.ftl", mapOf("title" to "Budgy")
                    ))
                }
            }
        }.start(wait = true)
    }

}