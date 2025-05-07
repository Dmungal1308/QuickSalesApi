package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import com.ktor.serializers.LocalDateTimeSerializer

object Favoritos : Table() {
    val id         = integer("id").autoIncrement()
    val idUsuario  = reference("id_usuario", Usuarios.id, onDelete = ReferenceOption.CASCADE)
    val idProducto = reference("id_producto", Productos.id, onDelete = ReferenceOption.CASCADE)
    val fechaAgregado = datetime("fecha_agregado")
        .defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Favorito(
    val id: Int,
    val idUsuario: Int,
    val idProducto: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaAgregado: LocalDateTime
)
