package com.starschema.brilliant.tableautracker.exception

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.microsoft.sqlserver.jdbc.SQLServerException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException
import javax.security.auth.message.AuthException

enum class Error(private val regex: String, private val messageTemplate: String) {
    DUPLICATE_ERROR_MSSQL("The duplicate key value is \\((.+)\\)", "Already exists: {param}"),
    DUPLICATE_ERROR_EXPOSED("Detail: Key .+\\=\\((.+)\\) already exists", "Already exists: {param}"),
    REQUIRED_PARAM_ERROR("Instantiation of .* value failed for JSON property ([a-zA-Z]+) due to missing", "Parameter {param} is required!");

    fun match(message: String): String = regex.toRegex(RegexOption.IGNORE_CASE).find(message)?.let {
        messageTemplate.replace("{param}", it.groupValues.last())
    } ?: message
}

fun String.isError(err: Error) = err.match(this)

fun exceptionHandler(): StatusPages.Configuration.() -> Unit  = {
    exception<SQLServerException> { cause ->
        call.respond(HttpStatusCode.BadRequest, Error.DUPLICATE_ERROR_MSSQL.match(cause.message ?: "Unknown database error occurred!"))
    }
    exception<ExposedSQLException> { cause ->
        val message = cause.message?.isError(Error.DUPLICATE_ERROR_MSSQL)?.isError(Error.DUPLICATE_ERROR_EXPOSED) ?: "Unknown database error occurred!"
        call.respond(HttpStatusCode.BadRequest, message)
    }
    exception<MissingKotlinParameterException> { cause ->
        call.respond(HttpStatusCode.BadRequest, Error.REQUIRED_PARAM_ERROR.match(cause.message ?: "Unknown request error occurred!"))
    }
    exception<IllegalArgumentException> { cause ->
        val message = cause.message ?: "Unknown error occurred!"
        call.respond(HttpStatusCode.BadRequest, message)
    }
    exception<AuthException> { cause ->
        val message = cause.message ?: cause.cause?.message ?: "Unauthorized!"
        call.respond(HttpStatusCode.Unauthorized, message)
    }
    exception<Throwable> { cause ->
        cause.message?.let { call.respond(HttpStatusCode.InternalServerError, it) } ?:
        call.respond(HttpStatusCode.InternalServerError, "Unknown error occurred! Please contact your server administrator!")
        throw cause
    }
}