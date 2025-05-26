package com.data.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object SesionesChat : IntIdTable("sesion_chat") {
    val producto  = integer("id_producto")
        .references(Productos.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val vendedor  = integer("id_vendedor")
        .references(Usuarios.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val comprador = integer("id_comprador")
        .references(Usuarios.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val creado    = datetime("fecha_creacion")
}

@kotlinx.serialization.Serializable
data class ChatSession(
    val idSesion: Int,
    val idProducto: Int,
    val idVendedor: Int,
    val idComprador: Int,
    val fechaCreacion: String
)