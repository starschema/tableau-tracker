package com.starschema.brilliant.tableautracker.db

import com.starschema.brilliant.tableautracker.config.config
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Helper functions for database management
 */

object DbUtils {
    internal val db by lazy {
        Database.connect(
            config.property("ktor.database.url").getString(),
            config.property("ktor.database.driver").getString(),
            config.property("ktor.database.user").getString(),
            config.property("ktor.database.password").getString(),
            { conn -> conn.schema = config.property("ktor.database.schema").getString() })
    }

    fun <T> exec(statement: Transaction.() -> T): T = transaction(db, statement = statement)

    fun updateLiquibase() = Liquibase(
        "liquibase/changelog.yaml",
        ClassLoaderResourceAccessor(),
        JdbcConnection(db.connector())
    ).update(null as String?)

}