package com.ktor.routes

import com.data.models.Usuario
import com.data.repository.UsuarioRepository
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

@Serializable
data class RegisterRequest(
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null
)

@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: Usuario
)

fun Route.authRoutes() {
    val repo = UsuarioRepository()

    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (repo.findByCorreo(req.correo) != null) {
                call.respond(mapOf("error" to "Usuario ya existe"))
                return@post
            }
            val user = repo.createUsuario(
                req.nombre, req.nombreUsuario, req.contrasena,
                req.correo, req.imagenBase64, req.rol ?: "usuario"
            )
            if (user != null) call.respond(user)
            else              call.respond(mapOf("error" to "Error en registro"))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = repo.findByCorreo(req.correo)
            if (user == null || user.contrasena != req.contrasena) {
                call.respond(mapOf("error" to "Credenciales inválidas"))
                return@post
            }

            val token = JWT.create()
                .withAudience("quicksalesAudience")
                .withIssuer("quicksalesIssuer")
                .withClaim("userId", user.id)
                .withClaim("rol", user.rol.toString())
                .withExpiresAt(Date(System.currentTimeMillis() + 36_000_00))
                .sign(Algorithm.HMAC256("mi_secreto"))

            call.respond(LoginResponse(token, user))
        }
    }
}
