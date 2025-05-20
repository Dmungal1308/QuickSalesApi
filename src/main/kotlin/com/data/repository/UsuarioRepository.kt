// File: com/data/repository/UsuarioRepository.kt
package com.data.repository

import com.data.models.Usuario
import com.data.models.Usuarios
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

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
            it[Usuarios.nombre]        = nombre
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.contrasena]    = contrasena
            it[Usuarios.correo]        = correo
            it[Usuarios.imagenBase64]  = imagenBase64
            it[Usuarios.rol]           = com.data.models.Role.valueOf(rol)
            it[Usuarios.saldo]         = BigDecimal.ZERO
        }
        stmt.resultedValues?.firstOrNull()?.let { row ->
            Usuario(
                id            = row[Usuarios.id],
                nombre        = row[Usuarios.nombre],
                nombreUsuario = row[Usuarios.nombreUsuario],
                contrasena    = row[Usuarios.contrasena],
                correo        = row[Usuarios.correo],
                imagenBase64  = row[Usuarios.imagenBase64],
                rol           = row[Usuarios.rol].toString(),
                saldo         = row[Usuarios.saldo]
            )
        }
    }

    fun findByCorreo(correo: String): Usuario? = transaction {
        Usuarios.select { Usuarios.correo eq correo }
            .map { row ->
                Usuario(
                    id            = row[Usuarios.id],
                    nombre        = row[Usuarios.nombre],
                    nombreUsuario = row[Usuarios.nombreUsuario],
                    contrasena    = row[Usuarios.contrasena],
                    correo        = row[Usuarios.correo],
                    imagenBase64  = row[Usuarios.imagenBase64],
                    rol           = row[Usuarios.rol].toString(),
                    saldo         = row[Usuarios.saldo]
                )
            }
            .singleOrNull()
    }

    fun getById(id: Int): Usuario? = transaction {
        Usuarios.select { Usuarios.id eq id }
            .map { row ->
                Usuario(
                    id            = row[Usuarios.id],
                    nombre        = row[Usuarios.nombre],
                    nombreUsuario = row[Usuarios.nombreUsuario],
                    contrasena    = row[Usuarios.contrasena],
                    correo        = row[Usuarios.correo],
                    imagenBase64  = row[Usuarios.imagenBase64],
                    rol           = row[Usuarios.rol].toString(),
                    saldo         = row[Usuarios.saldo]
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
            it[Usuarios.nombre]        = nombre
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.contrasena]    = contrasena
            it[Usuarios.correo]        = correo
            it[Usuarios.imagenBase64]  = imagenBase64
            it[Usuarios.rol]           = com.data.models.Role.valueOf(rol)
        } > 0
    }

    /** Permite solo cambiar el rol de un usuario */
    fun updateRol(id: Int, rol: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.rol] = com.data.models.Role.valueOf(rol)
        } > 0
    }

    fun deleteUsuario(id: Int): Boolean = transaction {
        Usuarios.deleteWhere { Usuarios.id eq id } > 0
    }

    fun depositar(usuarioId: Int, cantidad: BigDecimal): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq usuarioId }) {
            with(SqlExpressionBuilder) {
                it.update(Usuarios.saldo, Usuarios.saldo + cantidad)
            }
        } > 0
    }

    fun retirar(usuarioId: Int, cantidad: BigDecimal): Boolean = transaction {
        val u = getById(usuarioId) ?: return@transaction false
        if (u.saldo < cantidad) return@transaction false
        Usuarios.update({ Usuarios.id eq usuarioId }) {
            with(SqlExpressionBuilder) {
                it.update(Usuarios.saldo, Usuarios.saldo - cantidad)
            }
        } > 0
    }
    fun updateProfileFields(
        id: Int,
        nombre: String,
        nombreUsuario: String,
        correo: String,
        imagenBase64: String?
    ): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.nombre]        = nombre
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.correo]        = correo
            it[Usuarios.imagenBase64]  = imagenBase64
        } > 0
    }

    /** Cambia únicamente la contraseña */
    fun changePassword(id: Int, newPassword: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.contrasena] = newPassword
        } > 0
    }

    fun getAllUsuarios(): List<Usuario> = transaction {
        Usuarios.selectAll().map { row ->
            Usuario(
                id            = row[Usuarios.id],
                nombre        = row[Usuarios.nombre],
                nombreUsuario = row[Usuarios.nombreUsuario],
                contrasena    = "",                              // no devolvemos pwd
                correo        = row[Usuarios.correo],
                imagenBase64  = row[Usuarios.imagenBase64],
                rol           = row[Usuarios.rol].toString(),
                saldo         = row[Usuarios.saldo]
            )
        }
    }
}
