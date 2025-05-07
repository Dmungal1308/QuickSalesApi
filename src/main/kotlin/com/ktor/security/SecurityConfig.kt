package com.ktor.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.data.repository.UsuarioRepository

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "quicksales"
            verifier(
                JWT.require(Algorithm.HMAC256("mi_secreto"))
                    .withAudience("quicksalesAudience")
                    .withIssuer("quicksalesIssuer")
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                if (userId != null && UsuarioRepository().getById(userId) != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
