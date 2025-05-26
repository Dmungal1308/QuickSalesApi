package com.data.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object MensajesChat : IntIdTable("mensaje_chat") {
    val sesion    = integer("id_sesion")
        .references(SesionesChat.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val remitente = integer("id_remitente")
        .references(Usuarios.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val texto     = text("texto")
    val enviado   = datetime("fecha_envio")
}

@kotlinx.serialization.Serializable
data class ChatMessage(
    val idMensaje: Int,
    val idSesion: Int,
    val idRemitente: Int,
    val texto: String,
    val fechaEnvio: String
)