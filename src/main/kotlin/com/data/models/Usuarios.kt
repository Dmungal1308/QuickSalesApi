package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

// Definición de roles
enum class Role { admin, usuario }

object Usuarios : Table() {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val nombreUsuario = varchar("nombre_usuario", 50).uniqueIndex()
    val contrasena = varchar("contrasena", 255)
    val correo = varchar("correo", 100).uniqueIndex()
    val imagenBase64 = text("imagen_base64").nullable()
    val rol = enumerationByName("rol", 10, Role::class).default(Role.usuario)
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
    val rol: Role
)