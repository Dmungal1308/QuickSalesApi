package com.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.ktor.routes.authRoutes
import com.ktor.routes.productoRoutes
import com.ktor.routes.favoritoRoutes
import com.ktor.routes.userRoutes

fun Application.configureRouting() {
    routing {
        authRoutes()
        productoRoutes()
        favoritoRoutes()
        userRoutes()
    }
}