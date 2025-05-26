// File: com/ktor/routes/UserRoutes.kt
package com.ktor.routes

import com.data.models.Usuario
import com.data.models.Usuarios
import com.data.repository.UsuarioRepository
import com.ktor.serializers.BigDecimalSerializer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

@Serializable
data class RoleUpdateRequest(val rol: String)

@Serializable
data class AmountRequest(
    @Serializable(with = BigDecimalSerializer::class)
    val cantidad: BigDecimal
)
@Serializable
data class BalanceResponse(
    @Serializable(with = BigDecimalSerializer::class)
    val saldo: BigDecimal
)

fun Route.userRoutes() {
    val repo = UsuarioRepository()

    authenticate("auth-jwt") {
        route("/users") {
            get("/me") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val user = repo.getById(userId)
                if (user != null) call.respond(user)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
            }

            @Serializable
            data class ProfileUpdateRequest(
                val nombre: String,
                val nombreUsuario: String,
                val correo: String,
                val imagenBase64: String? = null
            )
            put("/me") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val req = call.receive<ProfileUpdateRequest>()
                try {
                    val ok = repo.updateProfileFields(
                        id            = userId,
                        nombre        = req.nombre,
                        nombreUsuario = req.nombreUsuario,
                        correo        = req.correo,
                        imagenBase64  = req.imagenBase64
                    )
                    if (!ok) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se pudo actualizar perfil"))
                        return@put
                    }
                    val updated = repo.getById(userId)
                        ?: return@put call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Perfil actualizado pero no se pudo leer"))
                    call.respond(updated)
                } catch (e: Exception) {
                    application.log.error("Error actualizando perfil de usuario $userId", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.localizedMessage ?: "Error desconocido"))
                    )
                }
            }

            @Serializable
            data class PasswordChangeRequest(val newPassword: String)
            put("/me/password") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val req = call.receive<PasswordChangeRequest>()
                val ok = repo.changePassword(userId, req.newPassword)
                if (ok) call.respond(mapOf("success" to true))
                else    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se pudo cambiar contraseña"))
            }
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

            put("/{id}/rol") {
                val principal = call.principal<JWTPrincipal>()!!
                val rolCaller = principal.payload.getClaim("rol").asString()
                if (rolCaller != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo admin puede cambiar roles"))
                    return@put
                }
                val id = call.parameters["id"]!!.toInt()
                val req = call.receive<RoleUpdateRequest>()
                if (req.rol !in listOf("admin", "usuario")) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Rol inválido"))
                    return@put
                }
                val changed = repo.updateRol(id, req.rol)
                if (changed) call.respond(mapOf("success" to true))
                else         call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
            }

            get("/{id}/saldo") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val user = repo.getById(userId)
                if (user != null) {
                    call.respond(BalanceResponse(user.saldo))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                }
            }

            post("/{id}/depositar") {
                val principal = call.principal<JWTPrincipal>()!!
                val rolCaller = principal.payload.getClaim("rol").asString()
                val callerId  = principal.payload.getClaim("userId").asInt()
                val idParam   = call.parameters["id"]!!.toInt()

                if (rolCaller != "admin" && callerId != idParam) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No autorizado"))
                    return@post
                }
                val req = call.receive<AmountRequest>()
                val ok = repo.depositar(idParam, req.cantidad)
                if (ok) call.respond(mapOf("success" to true))
                else    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se pudo depositar"))
            }

            post("/{id}/retirar") {
                val principal = call.principal<JWTPrincipal>()!!
                val rolCaller = principal.payload.getClaim("rol").asString()
                val callerId  = principal.payload.getClaim("userId").asInt()
                val idParam   = call.parameters["id"]!!.toInt()
                if (rolCaller != "admin" && callerId != idParam) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No se pudo retirar"))
                    return@post
                }
                val id = call.parameters["id"]!!.toInt()
                val req = call.receive<AmountRequest>()
                val ok = repo.retirar(id, req.cantidad)
                if (ok) call.respond(mapOf("success" to true))
                else    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Saldo insuficiente o fallo"))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Id inválido"))
                    return@get
                }
                val user = repo.getById(id)
                if (user != null) {
                    call.respond(
                        Usuario(
                            id            = user.id,
                            nombre        = user.nombre,
                            nombreUsuario = user.nombreUsuario,
                            contrasena    = "",
                            correo        = user.correo,
                            imagenBase64  = user.imagenBase64,
                            rol           = user.rol,
                            saldo         = user.saldo
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                }
            }
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val rol = principal.payload.getClaim("rol").asString()
                if (rol != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo admin"))
                    return@get
                }
                val all = repo.getAllUsuarios()
                call.respond(all)
            }

        }
    }
}
