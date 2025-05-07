 package com.ktor

import com.data.DatabaseFactory
import com.ktor.security.configureSecurity
import io.ktor.server.engine.*
import io.ktor.server.netty.*


fun main() {
    DatabaseFactory.init()

    embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        configureSecurity()
        configureRouting()
        configureSerialization()
    }.start(wait = true)

}
