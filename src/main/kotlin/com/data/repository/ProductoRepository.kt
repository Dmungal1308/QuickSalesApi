package com.data.repository

import com.data.models.Producto
import com.data.models.Productos
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ProductoRepository {
    fun createProducto(
        nombre: String,
        descripcion: String,
        imagenBase64: String?,
        precio: java.math.BigDecimal,
        idVendedor: Int
    ): Producto? = transaction {
        val stmt = Productos.insert {
            it[Productos.nombre] = nombre
            it[Productos.descripcion] = descripcion
            it[Productos.imagenBase64] = imagenBase64
            it[Productos.precio] = precio
            it[Productos.idVendedor] = idVendedor
        }
        stmt.resultedValues?.firstOrNull()?.let { row ->
            Producto(
                id = row[Productos.id],
                nombre = row[Productos.nombre],
                descripcion = row[Productos.descripcion],
                imagenBase64 = row[Productos.imagenBase64],
                precio = row[Productos.precio],
                estado = row[Productos.estado],
                idVendedor = row[Productos.idVendedor],
                idComprador = row[Productos.idComprador]
            )
        }
    }

    fun getAllProductos(): List<Producto> = transaction {
        Productos.selectAll().map { row ->
            Producto(
                id = row[Productos.id],
                nombre = row[Productos.nombre],
                descripcion = row[Productos.descripcion],
                imagenBase64 = row[Productos.imagenBase64],
                precio = row[Productos.precio],
                estado = row[Productos.estado],
                idVendedor = row[Productos.idVendedor],
                idComprador = row[Productos.idComprador]
            )
        }
    }

    fun getProductoById(id: Int): Producto? = transaction {
        Productos.select { Productos.id eq id }.map { row ->
            Producto(
                id = row[Productos.id],
                nombre = row[Productos.nombre],
                descripcion = row[Productos.descripcion],
                imagenBase64 = row[Productos.imagenBase64],
                precio = row[Productos.precio],
                estado = row[Productos.estado],
                idVendedor = row[Productos.idVendedor],
                idComprador = row[Productos.idComprador]
            )
        }.singleOrNull()
    }

    fun updateProducto(
        id: Int,
        nombre: String,
        descripcion: String,
        imagenBase64: String?,
        precio: java.math.BigDecimal,
        estado: String
    ): Producto? = transaction {
        val updated = Productos.update({ Productos.id eq id }) {
            it[Productos.nombre] = nombre
            it[Productos.descripcion] = descripcion
            it[Productos.imagenBase64] = imagenBase64
            it[Productos.precio] = precio
            it[Productos.estado] = com.data.models.Estado.valueOf(estado)
        }
        if (updated > 0) getProductoById(id) else null
    }

    fun deleteProducto(id: Int): Boolean = transaction {
        Productos.deleteWhere { Productos.id eq id } > 0
    }

    fun buyProducto(id: Int, compradorId: Int): Producto? = transaction {
        val updated = Productos.update({ Productos.id eq id }) {
            it[Productos.estado] = com.data.models.Estado.comprado
            it[Productos.idComprador] = compradorId
        }
        if (updated > 0) getProductoById(id) else null
    }
}