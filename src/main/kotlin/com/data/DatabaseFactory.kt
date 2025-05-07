package com.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.data.models.Usuarios
import com.data.models.Productos
import com.data.models.Favoritos

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://localhost:3306/quicksales"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = "quicksales"
            password = "quicksales"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(Usuarios, Productos, Favoritos)
        }
    }
}