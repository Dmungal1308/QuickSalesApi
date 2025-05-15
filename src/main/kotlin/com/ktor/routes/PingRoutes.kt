package com.ktor.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Definimos la ruta de ping
fun Route.pingRoute() {
    get("/auth/ping") {
        call.respondText("pong")
    }
}