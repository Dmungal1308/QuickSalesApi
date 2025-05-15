// File: com/ktor/routes/UserRoutes.kt
package com.ktor.routes

import com.data.repository.UsuarioRepository
import com.ktor.serializers.BigDecimalSerializer
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
/** DTO para actualizar rol de usuario */
data class RoleUpdateRequest(val rol: String)

@Serializable
/** DTO para operaciones de monedero */
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

            // Admin cambia el rol de un usuario
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

            // Admin deposita saldo a un usuario
            post("/{id}/depositar") {
                val principal = call.principal<JWTPrincipal>()!!
                val rolCaller = principal.payload.getClaim("rol").asString()
                val callerId  = principal.payload.getClaim("userId").asInt()
                val idParam   = call.parameters["id"]!!.toInt()

                // permitimos si es admin O si está depositando en su propia cuenta
                if (rolCaller != "admin" && callerId != idParam) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No autorizado"))
                    return@post
                }
                val req = call.receive<AmountRequest>()
                val ok = repo.depositar(idParam, req.cantidad)
                if (ok) call.respond(mapOf("success" to true))
                else    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se pudo depositar"))
            }

            // Admin retira saldo de un usuario
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
        }
    }
}
