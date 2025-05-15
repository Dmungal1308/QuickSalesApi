package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import java.math.BigDecimal
import com.ktor.serializers.BigDecimalSerializer

// Enumeración de estados
enum class Estado { `en venta`, reservado, comprado }

object Productos : Table() {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val descripcion = text("descripcion")
    val imagenBase64 = text("imagen_base64").nullable()
    val precio = decimal("precio", 10, 2)
    val estado = enumerationByName("estado", 12, Estado::class).default(Estado.`en venta`)
    // Usar reference/optReference para columnas de llave foránea
    val idVendedor = reference("id_vendedor", Usuarios.id, onDelete = ReferenceOption.CASCADE)
    val idComprador = optReference("id_comprador", Usuarios.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagenBase64: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val precio: BigDecimal,
    val estado: Estado,
    val idVendedor: Int,
    val idComprador: Int? = null
)
