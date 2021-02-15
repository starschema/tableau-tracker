package com.starschema.brilliant.tableautracker.test

import com.opentable.db.postgres.embedded.LiquibasePreparer
import com.opentable.db.postgres.junit.EmbeddedPostgresRules
import com.starschema.brilliant.tableautracker.config.config
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.main
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkStatic
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTimeZone
import org.junit.AfterClass
import org.junit.ClassRule
import java.io.File
import java.util.*


abstract class DatabaseTestBase(val defaultUrl: String) {
    fun testRequest(
        httpMethod: HttpMethod,
        url: String = defaultUrl,
        setup: TestApplicationRequest.() -> Unit = {},
        test: TestApplicationCall.() -> Unit
    ) {
        if (useMockDatabase) {
            withMockDb {
                withTestApplication({
                    main()
                }) { handleRequest(httpMethod, url, setup).apply(test) }
            }
        } else {
            withTestApplication({ main() }) { handleRequest(httpMethod, url, setup).apply(test) }
        }
    }

    fun bodyFromFile(fileName: String, args: Map<String, String> = emptyMap()): TestApplicationRequest.() -> Unit = {
        addHeader("Content-Type", "application/json")
        var content = File("resources${File.separator}test${File.separator}$fileName").readLines().joinToString("")
        args.forEach { (key, value) ->
            content = content.replace("\\$\\{$key\\}".toRegex(), value)
        }
        setBody(content)
    }


    companion object {
        val useMockDatabase = config.property("ktor.test.mockDatabase").getString().toBoolean()
        @JvmField
        @ClassRule
        val mockDb =
            if (useMockDatabase) EmbeddedPostgresRules.preparedDatabase(
                LiquibasePreparer.forClasspathLocation("liquibase/changelog.yaml")
            ) else null

        fun withMockDb(statement: Transaction.() -> Unit) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            DateTimeZone.setDefault(DateTimeZone.UTC)

            val connection = mockDb!!.testDatabase.connection
            val database = Database.connect({ connection })

            val transactionIsolation = connection.metaData.defaultTransactionIsolation
            transaction(transactionIsolation, 1, db = database) {
                statement()
            }
        }

        fun loadConfig() {
            if (useMockDatabase) {
                val connectionUrl =
                    "jdbc:postgresql://localhost:${mockDb!!.connectionInfo.port}/${mockDb!!.connectionInfo.dbName}"
                mockkStatic("com.starschema.brilliant.tableautracker.config.ConfigKt")
                every { config.property("ktor.deployment.ui").getString() } returns "basic"
                every { config.property("ktor.database.url").getString() } returns connectionUrl
                every { config.property("ktor.database.driver").getString() } returns "org.postgresql.Driver"
                every { config.property("ktor.database.user").getString() } returns mockDb!!.connectionInfo.user
                every { config.property("ktor.database.password").getString() } returns ""
                every { config.property("ktor.database.schema").getString() } returns "public"
                every { config.property("ktor.test.mockDatabase").getString() } returns "true"
            } else {
                DbUtils.updateLiquibase()
            }

        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            mockDb?.testDatabase?.connection?.close()
        }
    }
}
