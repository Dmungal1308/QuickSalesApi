package com.ktor.routes


import com.data.repository.ChatRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CrearSesionRequest(val idProducto: Int, val idVendedor: Int, val idComprador: Int)

@Serializable
data class EnviarMensajeRequest(val texto: String)

fun Route.chatRoutes() {
    val repo = ChatRepository()

    authenticate("auth-jwt") {
        route("/chats") {
            post("/sesion") {
                val req = call.receive<CrearSesionRequest>()
                val sesion = repo.obtenerOcrearSesion(
                    req.idProducto, req.idVendedor, req.idComprador
                )
                call.respond(sesion)
            }

            get("/{sesionId}/mensajes") {
                val sesionId = call.parameters["sesionId"]!!.toInt()
                val mensajes = repo.obtenerMensajes(sesionId)
                call.respond(mensajes)
            }

            post("/{sesionId}/mensajes") {
                val sesionId = call.parameters["sesionId"]!!.toInt()
                val req = call.receive<EnviarMensajeRequest>()
                val principal = call.principal<JWTPrincipal>()!!
                val remitenteId = principal.payload.getClaim("userId").asInt()
                val mensaje = repo.guardarMensaje(sesionId, remitenteId, req.texto)
                call.respond(HttpStatusCode.Created, mensaje)
            }

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val sessions = repo.obtenerSesionesPorUsuario(userId)
                call.respond(sessions)
            }
        }
    }
}
