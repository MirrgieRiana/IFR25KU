#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2")
@file:DependsOn("io.ktor:ktor-server-core-jvm:2.2.4")
@file:DependsOn("io.ktor:ktor-server-netty-jvm:2.2.4")

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

val siteDir = File("site/build/site")
require(siteDir.isDirectory) { "Site directory not found: ${siteDir.canonicalPath}" }

println("Serving ${siteDir.canonicalPath} at http://localhost:4000/IFR25KU/")

embeddedServer(Netty, host = "0.0.0.0", port = 4000) {
    routing {
        get("/IFR25KU/") {
            val file = siteDir.resolve("index.html")
            if (file.isFile) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/IFR25KU/{path...}") {
            val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val target = if (path.isEmpty()) siteDir else siteDir.resolve(path)
            val file = if (target.isDirectory) target.resolve("index.html") else target
            if (file.isFile) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}.start(wait = true)
