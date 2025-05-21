package com.data.repository

import com.data.models.ChatMessage
import com.data.models.ChatSession
import com.data.models.MensajesChat
import com.data.models.SesionesChat
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ChatRepository {
    fun obtenerOcrearSesion(
        productoId: Int,
        vendedorId: Int,
        compradorId: Int
    ): ChatSession = transaction {
        val existing = SesionesChat.select {
            (SesionesChat.producto eq productoId) and
                    (SesionesChat.vendedor eq vendedorId) and
                    (SesionesChat.comprador eq compradorId)
        }.firstOrNull()

        val row = existing ?: SesionesChat.insertAndGetId {
            it[producto]  = productoId
            it[vendedor]  = vendedorId
            it[comprador] = compradorId
            it[creado]    = LocalDateTime.now()
        }.let { id ->
            SesionesChat.select { SesionesChat.id eq id.value }.single()
        }

        ChatSession(
            idSesion      = row[SesionesChat.id].value,
            idProducto    = row[SesionesChat.producto],
            idVendedor    = row[SesionesChat.vendedor],
            idComprador   = row[SesionesChat.comprador],
            fechaCreacion = row[SesionesChat.creado].toString()
        )
    }

    fun guardarMensaje(
        sesionId: Int,
        remitenteId: Int,
        texto: String
    ): ChatMessage = transaction {
        val generatedId = MensajesChat.insertAndGetId {
            it[MensajesChat.sesion]    = sesionId
            it[MensajesChat.remitente] = remitenteId
            it[MensajesChat.texto]     = texto
            it[MensajesChat.enviado]   = LocalDateTime.now()
        }.value

        val row = MensajesChat.select { MensajesChat.id eq generatedId }.single()
        ChatMessage(
            idMensaje   = row[MensajesChat.id].value,
            idSesion    = row[MensajesChat.sesion],
            idRemitente = row[MensajesChat.remitente],
            texto       = row[MensajesChat.texto],
            fechaEnvio  = row[MensajesChat.enviado].toString()
        )
    }

    fun obtenerMensajes(sesionId: Int): List<ChatMessage> = transaction {
        MensajesChat.select { MensajesChat.sesion eq sesionId }
            .orderBy(MensajesChat.enviado to SortOrder.ASC)
            .map { row ->
                ChatMessage(
                    idMensaje   = row[MensajesChat.id].value,
                    idSesion    = row[MensajesChat.sesion],
                    idRemitente = row[MensajesChat.remitente],
                    texto       = row[MensajesChat.texto],
                    fechaEnvio  = row[MensajesChat.enviado].toString()
                )
            }
    }
    fun obtenerSesionesPorUsuario(userId: Int): List<ChatSession> = transaction {
        SesionesChat
            .select { (SesionesChat.vendedor eq userId) or (SesionesChat.comprador eq userId) }
            .map { row ->
                ChatSession(
                    idSesion      = row[SesionesChat.id].value,
                    idProducto    = row[SesionesChat.producto],
                    idVendedor    = row[SesionesChat.vendedor],
                    idComprador   = row[SesionesChat.comprador],
                    fechaCreacion = row[SesionesChat.creado].toString()
                )
            }
    }
}