package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import java.math.BigDecimal
import com.ktor.serializers.BigDecimalSerializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.TextColumnType

enum class Estado { `en venta`, reservado, comprado }

object Productos : Table() {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val descripcion = text("descripcion")
    val imagenBase64: Column<String?> =
        registerColumn<String>("imagen_base64", TextColumnType("LONGTEXT")).nullable()
    val precio = decimal("precio", 10, 2)
    val estado = enumerationByName("estado", 12, Estado::class).default(Estado.`en venta`)
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
