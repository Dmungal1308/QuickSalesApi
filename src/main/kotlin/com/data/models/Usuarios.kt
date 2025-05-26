package com.data.models

import com.ktor.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import java.math.BigDecimal

enum class Role { admin, usuario }

object Usuarios : Table() {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val nombreUsuario = varchar("nombre_usuario", 50).uniqueIndex()
    val contrasena = varchar("contrasena", 255)
    val correo = varchar("correo", 100).uniqueIndex()
    val imagenBase64: Column<String?> =
        registerColumn<String>("imagen_base64", TextColumnType("LONGTEXT")).nullable()
    val rol = enumerationByName("rol", 10, Role::class).default(Role.usuario)
    val saldo = decimal("saldo", 10, 2).default(BigDecimal.ZERO)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Usuario(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val saldo: BigDecimal
)