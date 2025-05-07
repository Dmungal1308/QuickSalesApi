package com.ktor.routes

import com.data.repository.ProductoRepository
import com.ktor.serializers.BigDecimalSerializer
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
/** Request DTO para productos */
data class ProductoRequest(
    val nombre: String,
    val descripcion: String,
    val imagenBase64: String,
    @Serializable(with = BigDecimalSerializer::class)
    val precio: BigDecimal
)

fun Route.productoRoutes() {
    val repo = ProductoRepository()
    authenticate("auth-jwt") {
        route("/productos") {
            get {
                call.respond(repo.getAllProductos())
            }
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val req = call.receive<ProductoRequest>()
                val producto = repo.createProducto(
                    nombre = req.nombre,
                    descripcion = req.descripcion,
                    imagenBase64 = req.imagenBase64,
                    precio = req.precio,
                    idVendedor = userId
                )
                if (producto != null) call.respond(producto) else call.respond(mapOf("error" to "No se pudo crear producto"))
            }
            put("/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val existing = repo.getProductoById(id)
                if (existing == null || existing.idVendedor != userId) {
                    call.respond(mapOf("error" to "No autorizado o producto no existe"))
                    return@put
                }
                val req = call.receive<ProductoRequest>()
                val updated = repo.updateProducto(
                    id = id,
                    nombre = req.nombre,
                    descripcion = req.descripcion,
                    imagenBase64 = req.imagenBase64,
                    precio = req.precio,
                    estado = existing.estado.name
                )
                if (updated != null) call.respond(updated) else call.respond(mapOf("error" to "No se pudo actualizar producto"))
            }
            delete("/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val existing = repo.getProductoById(id)
                if (existing == null || existing.idVendedor != userId) {
                    call.respond(mapOf("error" to "No autorizado o producto no existe"))
                    return@delete
                }
                val deleted = repo.deleteProducto(id)
                call.respond(mapOf("success" to deleted))
            }
            // Endpoint para comprar un producto
            post("/{id}/comprar") {
                val id = call.parameters["id"]!!.toInt()
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val existing = repo.getProductoById(id)
                if (existing == null) {
                    call.respond(mapOf("error" to "Producto no encontrado"))
                    return@post
                }

                // <-- Aquí insertas la comprobación:
                if (existing.idVendedor == userId) {
                    call.respond(mapOf("error" to "No puedes comprar tu propio producto"))
                    return@post
                }

                val comprado = repo.buyProducto(id, userId)
                if (comprado != null) call.respond(comprado) else call.respond(mapOf("error" to "No se pudo comprar producto"))
            }
        }
    }
}