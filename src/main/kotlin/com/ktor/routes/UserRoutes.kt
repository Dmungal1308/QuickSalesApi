// File: com/ktor/routes/UserRoutes.kt
package com.ktor.routes

import com.data.repository.UsuarioRepository
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    val repo = UsuarioRepository()

    authenticate("auth-jwt") {
        route("/users") {
            // Admin borra usuario por ID
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val rol       = principal.payload.getClaim("rol").asString()
                if (rol != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo admin puede borrar usuarios"))
                    return@delete
                }
                val id = call.parameters["id"]!!.toInt()
                val ok = repo.deleteUsuario(id)
                if (ok) call.respond(mapOf("success" to true))
                else   call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
            }
        }
    }
}
