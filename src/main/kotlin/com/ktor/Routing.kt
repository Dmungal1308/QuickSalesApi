package com.ktor

import com.ktor.routes.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()
        pingRoute()
        productoRoutes()
        favoritoRoutes()
        userRoutes()
    }
}