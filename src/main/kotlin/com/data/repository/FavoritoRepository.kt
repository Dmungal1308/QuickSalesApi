package com.data.repository

import com.data.models.Favorito
import com.data.models.Favoritos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FavoritoRepository {
    fun addFavorito(idUsuario: Int, idProducto: Int): Favorito? = transaction {
        Favoritos.insert {
            it[Favoritos.idUsuario] = idUsuario
            it[Favoritos.idProducto] = idProducto
        }
        Favoritos
            .select { (Favoritos.idUsuario eq idUsuario) and (Favoritos.idProducto eq idProducto) }
            .map { row ->
                Favorito(
                    id           = row[Favoritos.id],
                    idUsuario    = row[Favoritos.idUsuario],
                    idProducto   = row[Favoritos.idProducto],
                    fechaAgregado= row[Favoritos.fechaAgregado]
                )
            }
            .singleOrNull()
    }




    fun getFavoritosByUsuario(idUsuario: Int): List<Favorito> = transaction {
        Favoritos.select { Favoritos.idUsuario eq idUsuario }.map { row ->
            Favorito(
                id = row[Favoritos.id],
                idUsuario = row[Favoritos.idUsuario],
                idProducto = row[Favoritos.idProducto],
                fechaAgregado = row[Favoritos.fechaAgregado]
            )
        }
    }

    fun removeFavorito(idUsuario: Int, idProducto: Int): Boolean = transaction {
        Favoritos.deleteWhere { Favoritos.idUsuario eq idUsuario and (Favoritos.idProducto eq idProducto) } > 0
    }
}