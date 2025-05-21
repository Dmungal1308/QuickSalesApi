package com.ktor.routes

import com.data.repository.ProductoRepository
import com.data.repository.UsuarioRepository
import com.ktor.serializers.BigDecimalSerializer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

@Serializable
/** Request DTO para productos */
data class ProductoRequest(
    val nombre: String,
    val descripcion: String,
    // Pon nullable y un valor por defecto
    val imagenBase64: String? = null,
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
            get("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                    return@get
                }
                val producto = repo.getProductoById(id)
                if (producto != null) {
                    call.respond(producto)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Producto no encontrado"))
                }
            }
            delete("/{id}") {
                val id        = call.parameters["id"]!!.toInt()
                val principal = call.principal<JWTPrincipal>()!!
                val userId    = principal.payload.getClaim("userId").asInt()
                val rol       = principal.payload.getClaim("rol").asString()
                val existing  = repo.getProductoById(id)

                if (existing == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Producto no existe"))
                    return@delete
                }

                // Autorización: o soy owner o soy admin
                if (existing.idVendedor != userId && rol != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No autorizado"))
                    return@delete
                }

                val deleted = repo.deleteProducto(id)
                call.respond(mapOf("success" to deleted))
            }

            // Endpoint para comprar un producto
            post("/{id}/comprar") {
                val idProducto = call.parameters["id"]!!.toInt()
                val userId     = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val userRepo   = UsuarioRepository()
                val producto   = repo.getProductoById(idProducto)

                if (producto == null) {
                    call.respond(mapOf("error" to "Producto no encontrado")); return@post
                }
                if (producto.idVendedor == userId) {
                    call.respond(mapOf("error" to "No puedes comprar tu propio producto")); return@post
                }
                // 3.1) Validar saldo
                val comprador = userRepo.getById(userId)!!
                if (comprador.saldo < producto.precio) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Saldo insuficiente"))
                    return@post
                }

                // 3.2) Hacer todo en una transacción
                val exitoso = transaction {
                    val ok1 = userRepo.retirar(userId, producto.precio)
                    val ok2 = userRepo.depositar(producto.idVendedor, producto.precio)
                    val ok3 = repo.buyProducto(idProducto, userId) != null
                    ok1 && ok2 && ok3
                }

                if (exitoso) {
                    call.respond(repo.getProductoById(idProducto)!!)
                } else {
                    call.respond(mapOf("error" to "Error al procesar la compra"))
                }
            }
        }
    }
}