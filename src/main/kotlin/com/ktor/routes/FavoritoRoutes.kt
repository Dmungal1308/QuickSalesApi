package com.ktor.routes

import com.data.repository.FavoritoRepository
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class FavoritoRequest(val idProducto: Int)

fun Route.favoritoRoutes() {
    val repo = FavoritoRepository()
    authenticate("auth-jwt") {
        route("/favoritos") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                call.respond(repo.getFavoritosByUsuario(userId))
            }
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val req = call.receive<FavoritoRequest>()
                val fav = repo.addFavorito(userId, req.idProducto)
                if (fav != null) call.respond(fav) else call.respond(mapOf("error" to "No se pudo agregar favorito"))
            }
            delete("/{idProducto}") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val prodId = call.parameters["idProducto"]!!.toInt()
                val removed = repo.removeFavorito(userId, prodId)
                call.respond(mapOf("success" to removed))
            }
        }
    }
}
