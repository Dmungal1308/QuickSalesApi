package com.data.repository

import com.data.models.Usuario
import com.data.models.Usuarios
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UsuarioRepository {

    fun createUsuario(
        nombre: String,
        nombreUsuario: String,
        contrasena: String,
        correo: String,
        imagenBase64: String?,
        rol: String
    ): Usuario? = transaction {
        val stmt = Usuarios.insert {
            it[Usuarios.nombre] = nombre
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.contrasena] = contrasena
            it[Usuarios.correo] = correo
            it[Usuarios.imagenBase64] = imagenBase64
            it[Usuarios.rol] = com.data.models.Role.valueOf(rol)
        }
        stmt.resultedValues?.firstOrNull()?.let { row ->
            Usuario(
                id = row[Usuarios.id],
                nombre = row[Usuarios.nombre],
                nombreUsuario = row[Usuarios.nombreUsuario],
                contrasena = row[Usuarios.contrasena],
                correo = row[Usuarios.correo],
                imagenBase64 = row[Usuarios.imagenBase64],
                rol = row[Usuarios.rol]
            )
        }
    }

    fun findByCorreo(correo: String): Usuario? = transaction {
        Usuarios.select { Usuarios.correo eq correo }
            .map { row ->
                Usuario(
                    id = row[Usuarios.id],
                    nombre = row[Usuarios.nombre],
                    nombreUsuario = row[Usuarios.nombreUsuario],
                    contrasena = row[Usuarios.contrasena],
                    correo = row[Usuarios.correo],
                    imagenBase64 = row[Usuarios.imagenBase64],
                    rol = row[Usuarios.rol]
                )
            }
            .singleOrNull()
    }

    fun getById(id: Int): Usuario? = transaction {
        Usuarios.select { Usuarios.id eq id }
            .map { row ->
                Usuario(
                    id = row[Usuarios.id],
                    nombre = row[Usuarios.nombre],
                    nombreUsuario = row[Usuarios.nombreUsuario],
                    contrasena = row[Usuarios.contrasena],
                    correo = row[Usuarios.correo],
                    imagenBase64 = row[Usuarios.imagenBase64],
                    rol = row[Usuarios.rol]
                )
            }
            .singleOrNull()
    }

    fun updateUsuario(
        id: Int,
        nombre: String,
        nombreUsuario: String,
        contrasena: String,
        correo: String,
        imagenBase64: String?,
        rol: String
    ): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.nombre] = nombre
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.contrasena] = contrasena
            it[Usuarios.correo] = correo
            it[Usuarios.imagenBase64] = imagenBase64
            it[Usuarios.rol] = com.data.models.Role.valueOf(rol)
        } > 0
    }

    fun deleteUsuario(id: Int): Boolean = transaction {
        Usuarios.deleteWhere { Usuarios.id eq id } > 0
    }
}
