package com.ktor.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pingRoute() {
    get("/auth/ping") {
        call.respondText("pong")
    }
}